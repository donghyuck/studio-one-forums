package studio.one.application.forums.web.controller;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import studio.one.application.attachment.domain.model.Attachment;
import studio.one.application.forums.config.ForumWebProperties;
import studio.one.application.forums.service.post.ForumPostAttachmentService;
import studio.one.application.forums.web.dto.PostAttachmentDtos;
import studio.one.platform.web.dto.ApiResponse;
/**
 * Forums 게시글 첨부파일 REST 컨트롤러.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}/{forumSlug}/topics/{topicId}/posts/{postId}/attachments")
public class PostAttachmentController {
    private final ForumPostAttachmentService attachmentService;
    private final ForumWebProperties webProperties;

    public PostAttachmentController(ForumPostAttachmentService attachmentService,
            ForumWebProperties webProperties) {
        this.attachmentService = attachmentService;
        this.webProperties = webProperties;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@forumAuthz.canPost(#postId, 'UPLOAD_ATTACHMENT')")
    public ResponseEntity<ApiResponse<PostAttachmentDtos.AttachmentResponse>> upload(@PathVariable String forumSlug,
            @PathVariable Long topicId,
            @PathVariable Long postId,
            @RequestPart("file") MultipartFile file) {
        Attachment saved = attachmentService.upload(postId, file);

        return ResponseEntity.ok(ApiResponse.ok(toDto(saved, forumSlug, topicId, postId)));
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canPost(#postId, 'READ_ATTACHMENT')")
    public ResponseEntity<ApiResponse<List<PostAttachmentDtos.AttachmentResponse>>> list(@PathVariable String forumSlug,
            @PathVariable Long topicId,
            @PathVariable Long postId) {
        List<PostAttachmentDtos.AttachmentResponse> responses = attachmentService.list(postId).stream()
                .map(att -> toDto(att, forumSlug, topicId, postId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'READ_ATTACHMENT')")
    public ResponseEntity<ApiResponse<PostAttachmentDtos.AttachmentResponse>> get(@PathVariable String forumSlug,
            @PathVariable Long topicId,
            @PathVariable Long postId,
            @PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.get(postId, attachmentId);
        return ResponseEntity.ok(ApiResponse.ok(toDto(attachment, forumSlug, topicId, postId)));
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'READ_ATTACHMENT')")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String forumSlug,
            @PathVariable Long topicId,
            @PathVariable Long postId,
            @PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.get(postId, attachmentId);
        InputStream inputStream = attachmentService.openStream(postId, attachmentId);
        StreamingResponseBody body = outputStream -> {
            try (InputStream in = inputStream) {
                in.transferTo(outputStream);
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveMediaType(attachment.getContentType()));
        headers.setContentLength(attachment.getSize());
        if (attachment.getName() != null && !attachment.getName().isBlank()) {
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename(attachment.getName())
                    .build();
            headers.setContentDisposition(disposition);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'UPLOAD_ATTACHMENT')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String forumSlug,
            @PathVariable Long topicId,
            @PathVariable Long postId,
            @PathVariable Long attachmentId) {
        attachmentService.delete(postId, attachmentId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private PostAttachmentDtos.AttachmentResponse toDto(Attachment attachment,
            String forumSlug,
            Long topicId,
            Long postId) {
        PostAttachmentDtos.AttachmentResponse dto = new PostAttachmentDtos.AttachmentResponse();
        dto.setAttachmentId(attachment.getAttachmentId());
        dto.setName(attachment.getName());
        dto.setContentType(attachment.getContentType());
        dto.setSize(attachment.getSize());
        dto.setCreatedBy(attachment.getCreatedBy());
        dto.setCreatedAt(attachmentService.toOffsetDateTime(attachment.getCreatedAt()));
        dto.setDownloadUrl(buildDownloadUrl(forumSlug, topicId, postId, attachment));
        return dto;
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String buildDownloadUrl(String forumSlug, Long topicId, Long postId, Attachment attachment) {
        String normalizedBase = normalizeBasePath(webProperties.getBasePath());
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(normalizedBase)
                .pathSegment(
                        forumSlug,
                        "topics",
                        String.valueOf(topicId),
                        "posts",
                        String.valueOf(postId),
                        "attachments",
                        String.valueOf(attachment.getAttachmentId()),
                        "download")
                .build()
                .toUriString();
    }

    private String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            basePath = "/api/forums";
        }
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }
}
