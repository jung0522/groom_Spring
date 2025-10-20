package io.goorm.board.repository;

import io.goorm.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 제목으로 검색
    List<Post> findByTitleContainingIgnoreCase(String title);
    
    // 내용으로 검색
    List<Post> findByContentContainingIgnoreCase(String content);
    
    // 제목 또는 내용으로 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> findByTitleOrContentContaining(@Param("keyword") String keyword);
    
    // 작성자 닉네임으로 검색
    @Query("SELECT p FROM Post p JOIN p.author a WHERE a.nickname LIKE %:nickname%")
    List<Post> findByAuthorNicknameContaining(@Param("nickname") String nickname);
}
