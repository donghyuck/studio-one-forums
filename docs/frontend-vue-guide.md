# Vue Frontend Guide (API 연동 · ETag/If-Match)

이 문서는 Vue 프론트엔드에서 Forums API를 연동할 때의 기본 패턴과 ETag/If-Match 동시성 제어 흐름을 정리합니다.

## 1) 기본 API 연동

### Base Path
- Public: `/api/forums`
- Admin: `/api/mgmt/forums`

### 공통 응답 포맷
- 모든 응답은 `ApiResponse` 형태입니다.
- `success`가 `true`이면 `data` 사용, `false`이면 `error` 사용.

### 권장 Axios 설정 예시
```ts
// src/api/http.ts
import axios from "axios";

export const http = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    // 공통 에러 처리(권한, 412/428 등)
    return Promise.reject(error);
  }
);
```

## 2) ETag / If-Match 동시성 제어

### 정책 요약
- 조회 응답에는 ETag가 내려옵니다. (예: `W/"{version}"`)
- 수정/삭제 요청에는 `If-Match` 헤더를 반드시 포함해야 합니다.
- `If-Match` 누락: 428 Precondition Required
- `If-Match` 유효하지 않음: 412 Precondition Failed

### 흐름 예시 (Topic 수정)
1. GET으로 리소스 조회 → 응답 헤더의 `ETag` 저장
2. PATCH/PUT/DELETE 시 `If-Match`에 저장된 ETag 전달

```ts
// src/api/topics.ts
import { http } from "./http";

export async function getTopic(forumSlug: string, topicId: number) {
  const res = await http.get(`/forums/${forumSlug}/topics/${topicId}`);
  return {
    data: res.data,
    etag: res.headers["etag"],
  };
}

export async function updateTopic(
  forumSlug: string,
  topicId: number,
  payload: Record<string, unknown>,
  etag: string
) {
  return http.patch(`/forums/${forumSlug}/topics/${topicId}`, payload, {
    headers: { "If-Match": etag },
  });
}
```

### 실패 처리 가이드
- 428 응답: “데이터가 오래되었을 수 있습니다. 다시 로드 후 시도하세요.”
- 412 응답: “최신 버전이 아닙니다. 새로고침 후 다시 시도하세요.”

## 3) 자주 사용하는 Public API 예시
- Forum 목록: `GET /api/forums?q=&in=slug,name,description&page=&size=&sort=`
- Forum 상세: `GET /api/forums/{forumSlug}` (ETag)
- Topic 목록: `GET /api/forums/{forumSlug}/topics?q=&in=title,tags&fields=...&page=&size=&sort=`
- Topic 상세: `GET /api/forums/{forumSlug}/topics/{topicId}` (ETag)
- Topic 수정: `PATCH /api/forums/{forumSlug}/topics/{topicId}` (If-Match)
- Topic 삭제: `DELETE /api/forums/{forumSlug}/topics/{topicId}` (If-Match)
- Post 생성: `POST /api/forums/{forumSlug}/topics/{topicId}/posts`
- Post 수정: `PATCH /api/forums/{forumSlug}/topics/{topicId}/posts/{postId}` (If-Match)
- Post 삭제: `DELETE /api/forums/{forumSlug}/topics/{topicId}/posts/{postId}` (If-Match)

## 4) Admin API 사용 시 유의사항
- Admin API는 별도 base-path(`/api/mgmt/forums`)를 사용합니다.
- 권한이 필요한 작업은 서버 인증/인가 설정에 따라 헤더/쿠키가 필요합니다.
- 실제 인증 방식은 서비스 환경에 맞게 적용하세요.

## 5) 구현 팁
- ETag는 리소스 단위로 관리하세요(Topic/Post별 저장).
- 리스트에서는 ETag가 내려오지 않으므로, 수정 전에 상세 재조회 후 ETag 확보가 안전합니다.
- 페이지 전환 시 stale ETag 방지를 위해 캐시 정책을 명확히 하세요.
