package io.goorm.board.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends RuntimeException {
    
    private final Long id;
    
    public PostNotFoundException(Long id) {
        super("Post not found with id: " + id);
        this.id = id;
    }
    
    public PostNotFoundException(String message) {
        super(message);
        this.id = null;
    }
    
    public Long getId() {
        return id;
    }
}