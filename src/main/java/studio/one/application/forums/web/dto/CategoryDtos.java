package studio.one.application.forums.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Forums 웹 API DTO.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class CategoryDtos {
    @Getter
    @Setter
    public static class CreateCategoryRequest {
        private String name;
        private String description;
        private int position;
    }

    @Getter
    @Setter
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private int position;
    }
}
