package io.goorm.board.service;

import io.goorm.board.entity.Post;
import io.goorm.board.exception.PostNotFoundException;
import io.goorm.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service  // Spring Service Bean으로 등록
@RequiredArgsConstructor  // Lombok: final 필드에 대한 생성자 자동 생성
public class PostService {

    private final PostRepository postRepository;  // 의존성 주입

    // 전체 게시글 조회
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // ID로 게시글 조회
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    // 게시글 저장
    @Transactional  // 쓰기 작업은 별도 트랜잭션
    public Post save(Post post) {
        return postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public Post update(Long id, Post updatePost) {
        Post post = findById(id);
        post.setTitle(updatePost.getTitle());
        post.setContent(updatePost.getContent());
        post.setImagePath(updatePost.getImagePath());
        // author는 수정하지 않음 (기존 작성자 유지)
        return post;  // @Transactional에 의해 자동으로 UPDATE 쿼리 실행
    }

    // 게시글 삭제
    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    // 조회수 증가
    @Transactional
    public Post incrementViewCount(Long id) {
        Post post = findById(id);
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    // 검색 기능
    public List<Post> searchPosts(String keyword, String searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        
        keyword = keyword.trim();
        
        switch (searchType) {
            case "title":
                return postRepository.findByTitleContainingIgnoreCase(keyword);
            case "content":
                return postRepository.findByContentContainingIgnoreCase(keyword);
            case "author":
                return postRepository.findByAuthorNicknameContaining(keyword);
            case "all":
            default:
                return postRepository.findByTitleOrContentContaining(keyword);
        }
    }
}
