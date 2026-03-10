# API Error Contract

## Attachment upload and thumbnail errors

이 모듈은 첨부파일 관련 보안 검증 실패 시 아래 메시지를 `IllegalArgumentException` 메시지로 사용한다.

## Canonical messages
- `file is required`
- `file too large`
- `unsupported file extension`
- `unsupported content type`
- `file signature does not match content type`
- `upload rate limit exceeded`
- `thumbnail rate limit exceeded`
- `thumbnail size must be between 64 and 512`
- `unsupported thumbnail format`

## Client handling rules
- 사용자는 위 문자열을 그대로 노출하지 말고 로컬라이즈된 사용자 메시지로 매핑한다.
- `rate limit exceeded` 계열은 자동 재시도보다 대기 안내를 우선한다.
- `signature does not match content type` 는 보안 차단 메시지로 분류한다.
- `unsupported` 계열은 허용 파일 형식 안내와 함께 노출한다.

## Server-side rule
- 새 예외 문구를 추가할 때는 이 문서를 함께 갱신한다.
