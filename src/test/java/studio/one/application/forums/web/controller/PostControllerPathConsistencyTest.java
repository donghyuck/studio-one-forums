package studio.one.application.forums.web.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import studio.one.application.forums.service.post.PostQueryService;

class PostControllerPathConsistencyTest {

    @Test
    void rejectsMismatchedForumSlugAndTopicId() throws Exception {
        PostQueryService postQueryService = org.mockito.Mockito.mock(PostQueryService.class);
        when(postQueryService.listPosts(eq("general"), eq(10L), org.mockito.ArgumentMatchers.any(PageRequest.class), eq(false), eq(false)))
            .thenThrow(new IllegalArgumentException("resource mismatch"));

        PostController controller = new PostController(postQueryService, org.mockito.Mockito.mock(studio.one.application.forums.service.post.PostCommandService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .setControllerAdvice(new TestAdvice())
            .build();

        mvc.perform(get("/api/forums/general/topics/10/posts"))
            .andExpect(status().isBadRequest());
    }

    @RestControllerAdvice
    static class TestAdvice {
        @ExceptionHandler(IllegalArgumentException.class)
        ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
