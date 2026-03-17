---
name: Bug Report
about: Report a bug with reproduction, impact, validation, and deployment notes.
title: "[Bug] "
labels: bug
assignees: ""
---

## Summary
-

## Size
아래 항목은 **정확히 하나만** 체크합니다.
- [ ] Small (1) - 단순 수정 / 단일 파일
- [ ] Medium (2) - 기능 단위 변경 / 다중 파일
- [ ] Large (3) - 구조 변경 / 복수 모듈

## Environment
- Version/commit:
- Runtime (JDK/Spring profile):
- DB/infra:

## Reproduction Steps
1.
2.
3.

## Expected Result
-

## Actual Result
-

## Logs / Evidence
- Stack trace / query / screenshot:

## Impact
- User impact:
- Scope:

## Security Impact
- Risk:
- Mitigation:

## Root Cause (Optional)
-

## Proposed Fix
-

## Validation
- [ ] `./gradlew -q compileJava`
- [ ] `./gradlew test`
- Additional checks:

## AI-Assisted
아래 항목은 **정확히 하나만** 체크합니다.
- [ ] Yes (AI 사용 작업)
- [ ] No (AI 미사용 작업)

## AI Usage Type
`AI-Assisted`가 `Yes`인 경우에만 해당 항목을 체크합니다. (복수 선택 가능)
- [ ] Issue draft
- [ ] Log / root cause analysis
- [ ] Impact analysis
- [ ] Code generation / modification
- [ ] Refactoring suggestion
- [ ] Test case / validation checklist
- [ ] Documentation draft

## Deployment Notes
- Migration/ordering:
- Rollback plan:
- Post-deploy checks:
