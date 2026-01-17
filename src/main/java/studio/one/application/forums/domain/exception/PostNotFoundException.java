package studio.one.application.forums.domain.exception;

import studio.one.platform.exception.NotFoundException;

public class PostNotFoundException extends NotFoundException {
    private static final String BY_ID = "POST_BY_ID";

    public PostNotFoundException(Long postId) {
        super(BY_ID, "Post Not Found", postId);
    }

    public static PostNotFoundException byId(Long postId) {
        return new PostNotFoundException(postId);
    }
}
