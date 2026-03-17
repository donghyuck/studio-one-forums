## Summary
- `PUT /api/v1/forums/{forumSlug}/settings` 호출에서 `description`이 `null`일 때 `ForumJdbcRepositoryAdapter.update`에서 `NullPointerException`이 발생한다.
- 예외 지점: `Map.of(...)` (JDK immutable map은 key/value 모두 `null` 미허용)
- 영향: 게시판 설정 수정 API가 500으로 실패

## Security Impact
- 직접적인 보안 취약점은 아니나, 관리 기능 가용성 저하(운영 장애) 이슈
- 인증/인가 우회 영향 없음

## Reproduction
1. 기존 포럼에 대해 설정 수정 API 호출
2. 요청 바디에 `description: null` 포함(또는 nullable 정책에 따라 값 미입력)
3. 서버 로그에서 아래 예외 확인
- `java.lang.NullPointerException`
- `at java.util.Map.of(Map.java:1540)`
- `at ForumJdbcRepositoryAdapter.update(ForumJdbcRepositoryAdapter.java:265)`

## Root Cause
- `ForumJdbcRepositoryAdapter.update`가 SQL 파라미터를 `Map.of(...)`로 생성
- `description`은 도메인 정책상 nullable인데, `Map.of`는 null value를 허용하지 않음

## Proposed Fix
- `Map.of(...)`를 `MapSqlParameterSource`(또는 null 허용 mutable map)로 교체
- `description` null을 JDBC named parameter로 정상 바인딩

## Scope
- 대상 파일: `src/main/java/studio/one/application/forums/persistence/jdbc/ForumJdbcRepositoryAdapter.java`
- 영향 API: `PUT /api/v1/forums/{forumSlug}/settings`

## Validation
- [x] `./gradlew -q compileJava`
- [ ] `description = null` 요청에 대해 200 응답 및 DB 반영 확인(통합 테스트/수동 테스트)
- [ ] 기존 `description != null` 시나리오 회귀 확인

## Deployment Notes
- DB 스키마 변경 없음
- 애플리케이션 재배포만 필요
- 운영 반영 전, 동일 패턴(`Map.of` + nullable 필드) 사용 구간 점검 권장
