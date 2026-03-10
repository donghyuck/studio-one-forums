# Release Readiness

## Decision
- 현재 포럼 모듈은 보안 패치 기준으로 `release candidate` 상태로 본다.

## Basis
- 첨부파일 썸네일 무인가 접근이 차단됨
- 경로 정합성 검증이 서비스 계층에서 강제됨
- 업로드 확장자, MIME type, 파일 시그니처, 크기 제한이 적용됨
- 업로드/썸네일 abuse 완화용 애플리케이션 레벨 rate limiting 이 적용됨
- topic/post 조회 SQL 에서 unsafe `${...}` 조합이 제거됨
- 테스트가 존재하고 `./gradlew test` 통과 상태임

## Residual risks
- 전역 Spring Security/JWT/CORS/CSRF 설정은 상위 애플리케이션 범위다
- 인메모리 rate limiter 는 단일 인스턴스 기준이며, 멀티 인스턴스 환경에서는 gateway/WAF 와 함께 운영해야 한다
- 허용 파일 형식 정책은 현재 보수적 기본값이며, 제품 요구사항 변경 시 재검토가 필요하다

## Recommended next release checks
- 운영 환경에서 `studio.features.forums.attachments.*` 값 재확인
- 중앙 로그 시스템에서 `security_event=*` 수집 확인
- 클라이언트 오류 메시지 매핑 확인
