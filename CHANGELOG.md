# Changelog

## 2026-03-09

### 변경됨
- AI 개발 정책 파일(`AI_DEVELOPMENT_POLICY.md`, `CONTRIBUTING.md`, `SKILL.md`, `.gitmessage-ai-assisted.txt`)을 저장소 루트에 추가했다.
- 첨부파일 썸네일 엔드포인트에 `READ_ATTACHMENT` 인가를 추가했다.
- 첨부파일 업로드에 파일 크기, 확장자, MIME type 검증과 파일명 정규화를 추가했다.
- `forumSlug`, `topicId`, `postId`, `attachmentId` 경로 정합성을 서비스 계층에서 강제하도록 가드를 추가했다.
- 첨부파일 삭제 권한을 `UPLOAD_ATTACHMENT`에서 `EDIT_POST` 기준으로 조정했다.
- 게시글/토픽/첨부파일 변경 시 보안 감사용 애플리케이션 로그를 추가했다.
- 토픽/게시글 조회 쿼리에서 동적 SQL 템플릿 `${...}` 의존을 제거하고 화이트리스트 기반 문자열 조합으로 전환했다.
- 상위 애플리케이션 보안 설정 후속 점검 문서를 추가했다.

### 검증
- `./gradlew test`
