# Security Follow-ups

이 저장소는 포럼 모듈이며, 전역 Spring Security 설정은 포함하지 않는다. 아래 항목은 상위 애플리케이션에서 반드시 별도 확인해야 한다.

## 점검 필요 항목
- `SecurityFilterChain` 에서 포럼 API 경로가 인증 대상인지 확인
- JWT 검증에서 서명 알고리즘 고정, 만료시간(`exp`), 발급자(`iss`), 대상(`aud`) 검증 여부 확인
- anonymous / remember-me / session 정책이 포럼 API와 충돌하지 않는지 확인
- `CORS` 허용 origin/method/header/credentials 범위 최소화 확인
- 세션/쿠키 인증을 함께 쓰는 경우 `CSRF` 보호가 유지되는지 확인
- 인증 실패/인가 실패가 200 응답으로 래핑되지 않는지 확인
- 운영 로그/모니터링 시스템에서 `security_event=attachment_*`, `security_event=thumbnail_*` 패턴을 수집하는지 확인
- 과도한 업로드/썸네일 요청에 대해 WAF 또는 API gateway 레벨 rate limiting 과 중복 적용 여부 확인

## 권장 검증 시나리오
- 인증 없이 `/api/forums/**` 접근 시 기대한 401/403 이 반환되는지 확인
- 변조된 JWT, 만료된 JWT, 다른 `iss/aud` JWT 가 거부되는지 확인
- 브라우저 cross-origin 요청에서 허용되지 않은 origin 이 차단되는지 확인
- 쿠키 기반 요청에서 state-changing API 에 CSRF 토큰이 강제되는지 확인

## 탐지 규칙 초안
- 1분 내 동일 `forumSlug/topicId/postId` 에서 `attachment_upload_rejected reason=rate_limit` 다건 발생 시 경보
- 1분 내 동일 `attachmentId` 에서 `thumbnail_rejected reason=rate_limit` 다건 발생 시 경보
- `attachment_upload_rejected reason=signature_mismatch` 발생 시 보안 이벤트로 분류
