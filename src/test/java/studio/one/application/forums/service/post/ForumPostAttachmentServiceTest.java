package studio.one.application.forums.service.post;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;
import studio.one.application.attachment.service.AttachmentService;
import studio.one.application.forums.config.ForumAttachmentProperties;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.service.support.ForumResourceGuard;
import studio.one.application.forums.service.support.SimpleRateLimiter;

class ForumPostAttachmentServiceTest {

    @Test
    void rejectsUnsupportedFileExtension() {
        ForumPostAttachmentService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "payload.svg", "image/svg+xml", "x".getBytes());

        assertThatThrownBy(() -> service.upload("general", 10L, 1L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("unsupported file extension");
    }

    @Test
    void rejectsOversizedFile() {
        ForumPostAttachmentService service = service();
        byte[] payload = new byte[10 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "payload.png", "image/png", payload);

        assertThatThrownBy(() -> service.upload("general", 10L, 1L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("file too large");
    }

    @Test
    void rejectsSignatureMismatch() {
        ForumPostAttachmentService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "payload.png", "image/png", "not-a-png".getBytes());

        assertThatThrownBy(() -> service.upload("general", 10L, 1L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("file signature does not match content type");
    }

    @Test
    void rejectsUploadWhenRateLimitExceeded() {
        AttachmentService attachmentService = mock(AttachmentService.class);
        ForumResourceGuard resourceGuard = mock(ForumResourceGuard.class);
        SimpleRateLimiter limiter = mock(SimpleRateLimiter.class);
        when(limiter.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.any())).thenReturn(false);

        ForumPostAttachmentService service = service(attachmentService, resourceGuard, limiter);
        MockMultipartFile file = new MockMultipartFile("file", "payload.txt", "text/plain", "plain-text".getBytes());

        assertThatThrownBy(() -> service.upload("general", 10L, 1L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("upload rate limit exceeded");
    }

    private ForumPostAttachmentService service() {
        return service(mock(AttachmentService.class), mock(ForumResourceGuard.class), mock(SimpleRateLimiter.class));
    }

    private ForumPostAttachmentService service(AttachmentService attachmentService,
                                               ForumResourceGuard resourceGuard,
                                               SimpleRateLimiter limiter) {
        ForumAttachmentProperties properties = new ForumAttachmentProperties();
        properties.setObjectType(100);
        when(limiter.tryAcquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.any())).thenReturn(true);
        return new ForumPostAttachmentService(
            attachmentService,
            new SinglePostRepository(),
            properties,
            new EmptyThumbnailProvider(),
            resourceGuard,
            limiter
        );
    }

    private static class SinglePostRepository implements PostRepository {
        @Override
        public Post save(Post post) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Post> findById(Long postId) {
            return Optional.of(new Post(
                postId,
                10L,
                "content",
                1L,
                "user",
                OffsetDateTime.now(),
                1L,
                "user",
                OffsetDateTime.now(),
                null,
                null,
                null,
                null,
                0L
            ));
        }

        @Override
        public List<Post> findByTopicId(Long topicId) {
            return List.of();
        }
    }

    private static class EmptyThumbnailProvider implements ObjectProvider<studio.one.application.attachment.thumbnail.ThumbnailService> {
        @Override
        public studio.one.application.attachment.thumbnail.ThumbnailService getObject(Object... args) {
            return null;
        }

        @Override
        public studio.one.application.attachment.thumbnail.ThumbnailService getObject() {
            return null;
        }

        @Override
        public studio.one.application.attachment.thumbnail.ThumbnailService getIfAvailable() {
            return null;
        }

        @Override
        public studio.one.application.attachment.thumbnail.ThumbnailService getIfUnique() {
            return null;
        }

        @Override
        public java.util.stream.Stream<studio.one.application.attachment.thumbnail.ThumbnailService> stream() {
            return java.util.stream.Stream.empty();
        }

        @Override
        public java.util.stream.Stream<studio.one.application.attachment.thumbnail.ThumbnailService> orderedStream() {
            return java.util.stream.Stream.empty();
        }
    }
}
