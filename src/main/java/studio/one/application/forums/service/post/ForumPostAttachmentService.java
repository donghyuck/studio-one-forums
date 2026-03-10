package studio.one.application.forums.service.post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import studio.one.application.attachment.domain.model.Attachment;
import studio.one.application.attachment.service.AttachmentService;
import studio.one.application.attachment.thumbnail.ThumbnailData;
import studio.one.application.attachment.thumbnail.ThumbnailService;
import studio.one.application.forums.config.ForumAttachmentProperties;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.service.support.ForumResourceGuard;
import studio.one.application.forums.service.support.SimpleRateLimiter;
import studio.one.platform.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostAttachmentService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "pdf", "txt");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "application/pdf",
            "text/plain");

    private final AttachmentService attachmentService;
    private final PostRepository postRepository;
    private final ForumAttachmentProperties attachmentProperties;
    private final ObjectProvider<ThumbnailService> thumbnailServiceProvider;
    private final ForumResourceGuard forumResourceGuard;
    private final SimpleRateLimiter rateLimiter;

    public Attachment upload(String forumSlug, long topicId, long postId, MultipartFile file) {
        forumResourceGuard.requirePostInTopic(forumSlug, topicId, postId);
        enforceUploadRateLimit(forumSlug, topicId, postId);
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        if (file.getSize() > attachmentProperties.getMaxUploadSizeBytes()) {
            log.warn("security_event=attachment_upload_rejected reason=file_too_large forumSlug={} topicId={} postId={} size={}",
                forumSlug, topicId, postId, file.getSize());
            throw new IllegalArgumentException("file too large");
        }
        int objectType = resolveObjectType();
        String name = sanitizeFilename(file.getOriginalFilename());
        String contentType = normalizeContentType(file.getContentType());
        validateAttachmentMetadata(name, contentType, file);
        int size = Math.toIntExact(file.getSize()); 
        try (InputStream inputStream = file.getInputStream()) {
            Attachment saved = attachmentService.createAttachment(objectType, postId, name, contentType, inputStream, size);
            log.info("security_event=attachment_uploaded forumSlug={} topicId={} postId={} attachmentId={} contentType={} size={}",
                forumSlug, topicId, postId, saved.getAttachmentId(), contentType, size);
            return saved;
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to read file", ex);
        }
    }

    public List<Attachment> list(String forumSlug, long topicId, long postId) {
        forumResourceGuard.requirePostInTopic(forumSlug, topicId, postId);
        return attachmentService.getAttachments(resolveObjectType(), postId);
    }

    public Attachment get(String forumSlug, long topicId, long postId, long attachmentId) {
        forumResourceGuard.requirePostInTopic(forumSlug, topicId, postId);
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        ensureBelongsToPost(postId, attachment);
        return attachment;
    }

    public InputStream openStream(String forumSlug, long topicId, long postId, long attachmentId) {
        Attachment attachment = get(forumSlug, topicId, postId, attachmentId);
        try {
            return attachmentService.getInputStream(attachment);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to open attachment stream", ex);
        }
    }

    public java.util.Optional<ThumbnailData> getThumbnail(String forumSlug, long topicId, long postId, long attachmentId,
                                                          int size, String format) {
        enforceThumbnailRateLimit(forumSlug, topicId, postId, attachmentId);
        Attachment attachment = get(forumSlug, topicId, postId, attachmentId);
        ThumbnailService thumbnailService = thumbnailServiceProvider.getIfAvailable();
        if (thumbnailService == null) {
            return java.util.Optional.empty();
        }
        return thumbnailService.getOrCreate(attachment, size, format);
    }

    public void delete(String forumSlug, long topicId, long postId, long attachmentId) {
        Attachment attachment = get(forumSlug, topicId, postId, attachmentId);
        attachmentService.removeAttachment(attachment);
        log.info("security_event=attachment_deleted forumSlug={} topicId={} postId={} attachmentId={}",
            forumSlug, topicId, postId, attachmentId);
    }

    public OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atOffset(ZoneOffset.UTC);
    }

    public boolean isEnabled() {
        return attachmentProperties.getObjectType() > 0;
    }

    public void deleteAll(String forumSlug, long topicId, long postId) {
        if (!isEnabled()) {
            return;
        }
        forumResourceGuard.requirePostInTopic(forumSlug, topicId, postId);
        int objectType = resolveObjectType();
        attachmentService.getAttachments(objectType, postId)
                .forEach(attachmentService::removeAttachment);
    }

    private void ensureBelongsToPost(long postId, Attachment attachment) {
        if (attachment.getObjectType() != resolveObjectType() || attachment.getObjectId() != postId) {
            throw NotFoundException.of("PostAttachment", attachment.getAttachmentId());
        }
    }

    private Post requirePost(long postId) {
        return postRepository.findById(postId).orElseThrow(() -> PostNotFoundException.byId(postId));
    }

    private int resolveObjectType() {
        int objectType = attachmentProperties.getObjectType();
        if (objectType <= 0) {
            throw new IllegalStateException("forums.attachments.objectType must be set to forum_post objectType");
        }
        return objectType;
    }

    private String sanitizeFilename(String originalFilename) {
        String candidate = originalFilename == null ? "file" : originalFilename;
        String sanitized = Paths.get(candidate).getFileName().toString().trim();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("file name is required");
        }
        return sanitized;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType.toLowerCase(Locale.ROOT);
    }

    private void validateAttachmentMetadata(String filename, String contentType, MultipartFile file) {
        String extension = org.springframework.util.StringUtils.getFilenameExtension(filename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            log.warn("security_event=attachment_upload_rejected reason=extension filename={} contentType={}", filename, contentType);
            throw new IllegalArgumentException("unsupported file extension");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("security_event=attachment_upload_rejected reason=content_type filename={} contentType={}", filename, contentType);
            throw new IllegalArgumentException("unsupported content type");
        }
        if (!matchesFileSignature(contentType, file)) {
            log.warn("security_event=attachment_upload_rejected reason=signature_mismatch filename={} contentType={}",
                filename, contentType);
            throw new IllegalArgumentException("file signature does not match content type");
        }
    }

    private boolean matchesFileSignature(String contentType, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(16);
            return switch (contentType) {
                case "image/png" -> header.length >= 8
                    && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
                case "image/jpeg" -> header.length >= 3
                    && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
                case "image/gif" -> header.length >= 6
                    && header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
                case "application/pdf" -> header.length >= 4
                    && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
                case "text/plain" -> isLikelyText(header);
                default -> false;
            };
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to inspect file signature", ex);
        }
    }

    private boolean isLikelyText(byte[] header) {
        for (byte value : header) {
            int current = Byte.toUnsignedInt(value);
            if (current == 0) {
                return false;
            }
            if (current < 0x09) {
                return false;
            }
        }
        return true;
    }

    private void enforceUploadRateLimit(String forumSlug, long topicId, long postId) {
        boolean allowed = rateLimiter.tryAcquire(
            "upload:" + forumSlug + ":" + topicId + ":" + postId,
            attachmentProperties.getUploadRequestsPerMinute(),
            Duration.ofMinutes(1)
        );
        if (!allowed) {
            log.warn("security_event=attachment_upload_rejected reason=rate_limit forumSlug={} topicId={} postId={}",
                forumSlug, topicId, postId);
            throw new IllegalArgumentException("upload rate limit exceeded");
        }
    }

    private void enforceThumbnailRateLimit(String forumSlug, long topicId, long postId, long attachmentId) {
        boolean allowed = rateLimiter.tryAcquire(
            "thumbnail:" + forumSlug + ":" + topicId + ":" + postId + ":" + attachmentId,
            attachmentProperties.getThumbnailRequestsPerMinute(),
            Duration.ofMinutes(1)
        );
        if (!allowed) {
            log.warn("security_event=thumbnail_rejected reason=rate_limit forumSlug={} topicId={} postId={} attachmentId={}",
                forumSlug, topicId, postId, attachmentId);
            throw new IllegalArgumentException("thumbnail rate limit exceeded");
        }
    }
}
