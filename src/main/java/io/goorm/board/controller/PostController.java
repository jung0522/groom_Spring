package io.goorm.board.controller;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.exception.AccessDeniedException;
import io.goorm.board.service.FileUploadService;
import io.goorm.board.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileUploadService fileUploadService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/posts")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "searchType", defaultValue = "all") String searchType,
                       Model model) {
        List<Post> posts;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.searchPosts(keyword, searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchType", searchType);
        } else {
            posts = postService.findAll();
        }
        
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/posts/{id}")
    public String show(@PathVariable Long id, Model model) {
        Post post = postService.incrementViewCount(id);
        model.addAttribute("post", post);
        return "post/show";
    }

    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute Post post,
                         BindingResult bindingResult,
                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");

        if (bindingResult.hasErrors()) {
            return "post/form";
        }

        try {
            // 이미지 파일 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = fileUploadService.uploadFile(imageFile);
                post.setImagePath(imagePath);
            }

            post.setAuthor(user);
            postService.save(post);
            redirectAttributes.addFlashAttribute("message", "flash.post.created");
            return "redirect:/posts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
            return "post/form";
        }
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Post post = postService.findById(id);

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        model.addAttribute("post", post);
        return "post/form";
    }

    @PostMapping("/posts/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Post post,
                         BindingResult bindingResult,
                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                         @RequestParam(value = "removeImage", required = false) String removeImage,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        Post existingPost = postService.findById(id);

        if (!existingPost.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            post.setId(id);
            return "post/form";
        }

        try {
            // 기존 이미지 삭제 처리
            if ("true".equals(removeImage) && existingPost.getImagePath() != null) {
                fileUploadService.deleteFile(existingPost.getImagePath());
                post.setImagePath(null);
            }

            // 새 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                // 기존 이미지가 있다면 삭제
                if (existingPost.getImagePath() != null) {
                    fileUploadService.deleteFile(existingPost.getImagePath());
                }
                String imagePath = fileUploadService.uploadFile(imageFile);
                post.setImagePath(imagePath);
            } else if (!"true".equals(removeImage)) {
                // 이미지 변경이 없으면 기존 이미지 유지
                post.setImagePath(existingPost.getImagePath());
            }

            postService.update(id, post);
            redirectAttributes.addFlashAttribute("message", "flash.post.updated");
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
            post.setId(id);
            return "post/form";
        }
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        Post existingPost = postService.findById(id);

        if (!existingPost.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        // 게시글 삭제 전에 이미지 파일도 삭제
        if (existingPost.getImagePath() != null) {
            fileUploadService.deleteFile(existingPost.getImagePath());
        }

        postService.delete(id);
        redirectAttributes.addFlashAttribute("message", "flash.post.deleted");
        return "redirect:/posts";
    }

    @GetMapping("/posts/error-test")
    public String testError() {
        throw new RuntimeException("This is a test error for demonstration");
    }

}
