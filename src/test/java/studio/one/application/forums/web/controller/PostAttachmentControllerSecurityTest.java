package studio.one.application.forums.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class PostAttachmentControllerSecurityTest {

    @Test
    void thumbnailEndpointRequiresReadAttachmentPermission() throws Exception {
        Method method = PostAttachmentController.class.getMethod(
            "thumbnail",
            String.class,
            Long.class,
            Long.class,
            Long.class,
            int.class,
            String.class
        );

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("@forumAuthz.canPost(#postId, 'READ_ATTACHMENT')");
    }

    @Test
    void deleteEndpointRequiresEditPostPermission() throws Exception {
        Method method = PostAttachmentController.class.getMethod(
            "delete",
            String.class,
            Long.class,
            Long.class,
            Long.class
        );

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("@forumAuthz.canPost(#postId, 'EDIT_POST')");
    }
}
