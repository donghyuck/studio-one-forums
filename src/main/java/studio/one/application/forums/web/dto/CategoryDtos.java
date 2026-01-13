package studio.one.application.forums.web.dto;

public class CategoryDtos {
    public static class CreateCategoryRequest {
        public String name;
        public String description;
        public int position;
    }

    public static class CategoryResponse {
        public Long id;
        public String name;
        public String description;
        public int position;
    }
}
