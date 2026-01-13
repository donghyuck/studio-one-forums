package studio.one.application.forums.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ForumSlug {
    private static final Pattern PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    private final String value;

    private ForumSlug(String value) {
        this.value = value;
    }

    public static ForumSlug of(String value) {
        Objects.requireNonNull(value, "forumSlug");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid forumSlug: " + value);
        }
        return new ForumSlug(value);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumSlug)) return false;
        ForumSlug forumSlug = (ForumSlug) o;
        return value.equals(forumSlug.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
