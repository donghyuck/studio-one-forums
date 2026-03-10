# AI Development Policy

## 목적
- AI 보조 개발의 품질, 추적 가능성, 책임 범위를 표준화한다.

## 적용 범위
- 본 저장소의 코드, 스크립트, 문서, 설정 변경 전반.
- AI로 작성/수정된 모든 변경은 본 정책의 적용 대상이다.

## 필수 원칙
0. 지침 우선순위 원칙
- AI 작업 시 저장소의 `AGENTS.md` 및 활성화된 `SKILL` 지침을 우선 적용한다.
- `SKILL` 지침과 일반 규칙이 충돌하면, 더 구체적이고 범위가 좁은 지침(`SKILL`)을 우선한다.
- `SKILL` 미준수로 발생한 변경은 리뷰에서 반려할 수 있다.

1. 사람 책임 원칙
- 최종 책임은 작성자/리뷰어에게 있으며, AI 출력은 그대로 신뢰하지 않는다.

2. 검증 원칙
- AI 변경은 최소 1개 이상의 실행 가능한 검증(테스트/빌드/스모크)을 남긴다.
- 검증 명령과 결과는 커밋 본문 또는 PR 본문에 기록한다.

3. 보안 원칙
- 비밀정보(API 키, 비밀번호, 토큰, 개인정보)를 프롬프트/로그/문서에 남기지 않는다.
- 민감값은 환경변수/비밀 저장소를 사용한다.

4. 최소 변경 원칙
- 범위를 벗어난 리팩터링/형식 변경을 함께 수행하지 않는다.
- 정책/절차 문서는 단일 소스 문서로 유지하고 중복 서술을 피한다.

## AI 커밋 메시지 규칙
1. AI 보조 커밋 제목은 반드시 `[ai-assisted]`로 시작한다.
2. 형식:

```text
[ai-assisted] <type>(<scope>): <summary>
```

예시:
- `[ai-assisted] feat(attachment-egov): add mapper-based dao`
- `[ai-assisted] fix(sql): align image update query`
- `[ai-assisted] docs(contributing): add changelog update rule`

타입:
- `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

## CHANGELOG 업데이트 규칙
1. 아래 조건 중 하나라도 해당하면 같은 작업 단위에서 `CHANGELOG.md`를 함께 갱신한다.
- 사용자 동작/API/DB 스키마/운영 스크립트/개발 프로세스 규칙이 변경된 경우
- 회귀 방지 테스트가 추가되거나 검증 절차가 바뀐 경우

2. 문서 전용 오탈자 수정처럼 사용자 영향이 없는 변경은 생략 가능하다.

3. 항목 구성은 날짜별 섹션 내 `변경됨`/`검증`을 기본으로 한다.

## 문서 관계(중복 방지)
- 개발 참여 절차: `CONTRIBUTING.md`
- AI 상세 규칙: `AI_DEVELOPMENT_POLICY.md` (본 문서)
- 레거시 커밋 정책 문서: `docs/dev/ai-commit-policy.md` (참조 포인터)
