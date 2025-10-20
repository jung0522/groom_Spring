package io.goorm.board.controller;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupDto signupDto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {

        if (result.hasErrors()) {
            return "auth/signup";
        }

        try {
            userService.signup(signupDto);
            String message = messageSource.getMessage("flash.user.created", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/";
        } catch (Exception e) {
            result.reject("signup.failed", e.getMessage());
            return "auth/signup";
        }
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginDto loginDto,
                        BindingResult result,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Locale locale) {

        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            User user = userService.authenticate(loginDto);

            // 세션에 사용자 정보 저장
            session.setAttribute("user", user);

            String message = messageSource.getMessage("flash.login.success", null, "로그인되었습니다.", locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/";
        } catch (Exception e) {
            result.reject("login.failed", e.getMessage());
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {

        session.invalidate();

        String message = messageSource.getMessage("flash.logout.success", null, "로그아웃되었습니다.", locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profileForm(HttpSession session, Model model) {
        // interceptor가 이미 로그인 여부 체크
        User user = (User) session.getAttribute("user");

        // 최신 사용자 정보 조회
        User currentUser = userService.findById(user.getId());

        ProfileUpdateDto profileUpdateDto = new ProfileUpdateDto();
        profileUpdateDto.setNickname(currentUser.getNickname());

        model.addAttribute("profileUpdateDto", profileUpdateDto);
        model.addAttribute("currentUser", currentUser);

        return "auth/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileUpdateDto profileUpdateDto,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                Locale locale) {

        User user = (User) session.getAttribute("user");

        if (result.hasErrors()) {
            User currentUser = userService.findById(user.getId());
            model.addAttribute("currentUser", currentUser);
            return "auth/profile";
        }

        try {
            User updatedUser = userService.updateProfile(user.getId(), profileUpdateDto);
            session.setAttribute("user", updatedUser);

            String message = messageSource.getMessage("flash.profile.updated", null, "프로필이 수정되었습니다.", locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/auth/profile";
        } catch (Exception e) {
            result.reject("profile.update.failed", e.getMessage());
            User currentUser = userService.findById(user.getId());
            model.addAttribute("currentUser", currentUser);
            return "auth/profile";
        }
    }
}
