package studio.one.application.forums.service.post;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import studio.one.application.attachment.domain.model.Attachment;
import studio.one.application.attachment.service.AttachmentService;
import studio.one.application.forums.config.ForumAttachmentProperties;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.platform.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostAttachmentService {

    private final AttachmentService attachmentService;
    private final PostRepository postRepository;
    private final ForumAttachmentProperties attachmentProperties;

    public Attachment upload(long postId, MultipartFile file) {

        requirePost(postId);
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        int objectType = resolveObjectType();
        String name = file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        int size = Math.toIntExact(file.getSize()); 
        try (InputStream inputStream = file.getInputStream()) {
            return attachmentService.createAttachment(objectType, postId, name, contentType, inputStream, size);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to read file", ex);
        }
    }

    public List<Attachment> list(long postId) {
        requirePost(postId);
        return attachmentService.getAttachments(resolveObjectType(), postId);
    }

    public Attachment get(long postId, long attachmentId) {
        requirePost(postId);
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        ensureBelongsToPost(postId, attachment);
        return attachment;
    }

    public InputStream openStream(long postId, long attachmentId) {
        Attachment attachment = get(postId, attachmentId);
        try {
            return attachmentService.getInputStream(attachment);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to open attachment stream", ex);
        }
    }

    public void delete(long postId, long attachmentId) {
        Attachment attachment = get(postId, attachmentId);
        attachmentService.removeAttachment(attachment);
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

    public void deleteAll(long postId) {
        if (!isEnabled()) {
            return;
        }
        requirePost(postId);
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
}
