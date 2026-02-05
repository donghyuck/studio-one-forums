package studio.one.application.forums.domain.acl;

import java.util.Locale;

public enum PermissionAction {
    READ_BOARD,
    READ_TOPIC_LIST,
    READ_TOPIC_CONTENT,
    READ_ATTACHMENT,
    CREATE_TOPIC,
    REPLY_POST,
    UPLOAD_ATTACHMENT,
    EDIT_TOPIC,
    DELETE_TOPIC,
    EDIT_POST,
    DELETE_POST,
    PIN_TOPIC,
    LOCK_TOPIC,
    HIDE_POST,
    MODERATE,
    MANAGE_BOARD;

    public String description() {
        return switch (this) {
            case READ_BOARD -> "포럼 목록/상세 조회";
            case READ_TOPIC_LIST -> "토픽 목록 조회";
            case READ_TOPIC_CONTENT -> "토픽/댓글 내용 보기";
            case READ_ATTACHMENT -> "첨부파일 보기/다운로드";
            case CREATE_TOPIC -> "토픽 생성";
            case REPLY_POST -> "토픽에 댓글 작성";
            case UPLOAD_ATTACHMENT -> "첨부파일 업로드/삭제";
            case EDIT_TOPIC -> "토픽 수정";
            case DELETE_TOPIC -> "토픽 삭제";
            case EDIT_POST -> "작성한 댓글 수정";
            case DELETE_POST -> "작성한 댓글 삭제";
            case PIN_TOPIC -> "토픽 고정";
            case LOCK_TOPIC -> "토픽 잠금";
            case HIDE_POST -> "댓글 숨김";
            case MODERATE -> "운영자 모드";
            case MANAGE_BOARD -> "포럼 관리자 기능";
        };
    }
    public static PermissionAction from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("action is required (available: " + availableValues() + ")");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return PermissionAction.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unknown action '" + value + "' (available: " + availableValues() + ")", ex);
        }
    }

    private static String availableValues() {
        StringBuilder builder = new StringBuilder();
        for (PermissionAction action : PermissionAction.values()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(action.name());
        }
        return builder.toString();
    }
}
