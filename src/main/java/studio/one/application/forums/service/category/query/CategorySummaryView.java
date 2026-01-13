package studio.one.application.forums.service.category.query;

public class CategorySummaryView {
    private final Long id;
    private final String name;
    private final String description;
    private final int position;

    public CategorySummaryView(Long id, String name, String description, int position) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }
}
