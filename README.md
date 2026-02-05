# studio-one-forums

Discourse 스타일의 포럼(Forums) 모듈입니다. 멀티 포럼/카테고리/토픽/댓글 구조와 목록 검색(q/in/fields), ETag 동시성 제어를 포함합니다.

## 구성
- domain: 순수 비즈니스 모델/정책/예외
- persistence
  - jpa: 엔티티 + Spring Data + Adapter
  - jdbc: 목록/검색/프로젝션 전용 QueryRepository + JDBC CRUD Adapter
- service: 유스케이스/트랜잭션
- web: Controller/DTO/Mapper
- resources
  - schema: PostgreSQL/MySQL DDL
  - sql: SqlQuery SQLSet
  - i18n: 포럼 모듈 메시지

## 컨트롤러 네이밍 규칙
- 관리자 전용: `*MgmtController`
- 일반 사용자용: `*Controller`
- 공개(인증 없음): `*PublicController`
- 본인 전용: `*MeController`

## 주요 기능
- Forum: 생성/목록/상세/설정 변경
- Category: 생성/목록
- Topic: 생성/목록/상세/수정/삭제/상태변경
- Post: 생성/목록/수정/삭제
- Attachments: 댓글 첨부파일 업로드/목록/다운로드/삭제
- Membership: 게시판별 관리자/운영진/회원 관리

## 권한별 작업
Public (사용자)
- Forum: 목록/상세 조회 (게시판 타입/정책에 따라 제한)
- Category: 목록 조회
- Topic: 생성/목록/상세/수정/삭제 (권한 기반)
- Post: 생성/목록/수정/삭제 (권한 기반)
- Attachment: 댓글 첨부파일 업로드/목록/다운로드/삭제 (권한 기반)

Admin (관리자)
- Forum: 생성, 설정 변경
- Category: 생성
- Topic: 생성/수정/삭제/상태 변경/핀/락
- Post: 생성/수정/삭제/숨김
- Attachment: 댓글 첨부파일 업로드/목록/다운로드/삭제
- Membership: 게시판별 멤버/역할 관리

## 응답 포맷
- 사용자/관리자 모두 `ApiResponse`로 응답하며, 동일한 작업은 동일한 payload 구조를 사용합니다.
- Forum 응답에는 `viewType`(UI/콘텐츠 모드)과 whitelist된 `properties`만 노출됩니다.
- Forum 목록 응답에도 `viewType`이 포함됩니다.

## API
Public
- GET `/api/forums?q=&in=slug,name,description&page=&size=&sort=`
- GET `/api/forums/{forumSlug}` (ETag)
- GET `/api/forums/{forumSlug}/categories`
- POST `/api/forums/{forumSlug}/categories/{categoryId}/topics`
- POST `/api/forums/{forumSlug}/topics`
- GET `/api/forums/{forumSlug}/topics?q=&in=title,tags&fields=...&page=&size=&sort=`
- GET `/api/forums/{forumSlug}/topics?q=&in=title,tags&fields=...&page=&size=&sort=` (TopicSummary 확장 필드 포함)
- GET `/api/forums/{forumSlug}/topics/{topicId}` (ETag)
- PATCH `/api/forums/{forumSlug}/topics/{topicId}` (If-Match)
- DELETE `/api/forums/{forumSlug}/topics/{topicId}` (If-Match)
- POST `/api/forums/{forumSlug}/topics/{topicId}/posts`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts?page=&size=&sort=`
- PATCH `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}` (If-Match)
- DELETE `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}` (If-Match)
- POST `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments/{attachmentId}`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments/{attachmentId}/thumbnail`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments/{attachmentId}/download`
- DELETE `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments/{attachmentId}`

Admin
- GET `/api/mgmt/forums?q=&in=slug,name,description&page=&size=&sort=`
- GET `/api/mgmt/forums/{forumSlug}` (ETag)
- POST `/api/mgmt/forums`
- PUT `/api/mgmt/forums/{forumSlug}/settings` (If-Match)
- POST `/api/mgmt/forums/{forumSlug}/categories`
- DELETE `/api/mgmt/forums/{forumSlug}/categories/{categoryId}`
- POST `/api/mgmt/forums/{forumSlug}/categories/{categoryId}/topics`
- GET `/api/mgmt/forums/{forumSlug}/topics?includeHidden=false&q=&in=title,tags&fields=...&page=&size=&sort=`
- PATCH `/api/mgmt/forums/{forumSlug}/topics/{topicId}/status` (If-Match)
- PATCH `/api/mgmt/forums/{forumSlug}/topics/{topicId}/pin` (If-Match)
- PATCH `/api/mgmt/forums/{forumSlug}/topics/{topicId}/lock` (If-Match)
- DELETE `/api/mgmt/forums/{forumSlug}/topics/{topicId}` (If-Match)
- POST `/api/mgmt/forums/{forumSlug}/topics/{topicId}/posts`
- PATCH `/api/mgmt/forums/{forumSlug}/topics/{topicId}/posts/{postId}/hide`
- DELETE `/api/mgmt/forums/{forumSlug}/topics/{topicId}/posts/{postId}`
- GET `/api/mgmt/forums/{forumSlug}/members?page=&size=`
- POST `/api/mgmt/forums/{forumSlug}/members`
- PATCH `/api/mgmt/forums/{forumSlug}/members/{userId}`
- DELETE `/api/mgmt/forums/{forumSlug}/members/{userId}`
- GET `/api/mgmt/forums/{forumSlug}/permissions`
- POST `/api/mgmt/forums/{forumSlug}/permissions`
- DELETE `/api/mgmt/forums/{forumSlug}/permissions`

## Attachments
- Requires `attachment-service` (or starter equivalent) and `studio.features.attachment.enabled=true`.
- Configure `studio.features.forums.attachments.object-type` with the `forum_post` object type ID registered via the objecttype admin API (DB mode). Uploads/listing rely on this ID for policy + ownership.
- Attachment endpoints sit under `/api/forums/{forumSlug}/topics/{topicId}/posts/{postId}/attachments` and return a `downloadUrl` that honors `studio.features.forums.web.base-path`.
- Thumbnail endpoint is public (no auth) for UI thumbnails: `/thumbnail` returns 204 for non-image or unavailable thumbnails.
- 권한은 forums 권한 시스템(ForumAuthz: Policy + ACL)으로 제어하며, 첨부파일 엔드포인트는 아래 `PermissionAction`을 사용합니다.
  - GET(목록/조회/다운로드): `READ_ATTACHMENT`
  - POST/DELETE(업로드/삭제): `UPLOAD_ATTACHMENT`
- 기본 정책은 게시판 타입별로 다음과 같이 동작합니다.
  - COMMON/NOTICE: `READ_ATTACHMENT`는 공개(ALLOW), `UPLOAD_ATTACHMENT`는 작성자/관리자만 허용
  - SECRET: `READ_ATTACHMENT`도 작성자/관리자만 허용 (권한 없으면 404로 숨김)
- 다운로드를 사용자/역할 단위로 차단하려면 ACL 룰에서 `READ_ATTACHMENT`에 `DENY`를 추가하세요. (예: `ROLE_ANONYMOUS` DENY → 로그인 사용자만 다운로드 가능)

## 권한 관리 UI 연동 (관리자 저장소)
- **GET `/api/mgmt/forums/{forumSlug}/permissions/actions`**  
  관리자 UI가 보여줄 `PermissionAction` 전체 목록(이름/설명/displayName)을 가져갑니다.
- **GET `/api/mgmt/forums/{forumSlug}/permissions`**  
  현재 포럼/카테고리 조건의 ACL 룰을 조회합니다.
- **POST/PATCH/DELETE `/api/mgmt/forums/{forumSlug}/permissions`**  
  룰은 `PermissionAction`, `Effect(ALLOW/DENY)`, `Ownership`, `SubjectType(ROLE|USER)` 등을 포함합니다. 프론트에서는 `role`, `subjectName`/`subjectId`, `priority` 등을 입력하며, `identifierType=NAME`이면 `subjectName`을, `identifierType=ID`이면 `subjectId`를 필수로 넣어야 DB 제약(`ck_forum_acl_rule_subject_identifier`)에 걸리지 않습니다.
- **GET `/api/mgmt/forums/{forumSlug}/permissions/check`**  
  관리자 시뮬레이터로 `action`, `role`, `ownerId`, `locked`, `userId`, `username`을 넘겨 policy + ACL 평가 결과(`allowed`, `policyDecision`, `aclDecision`, `denyReason`)를 반환합니다.
- `ForumMemberMgmtController`: OWNER/ADMIN/MODERATOR/MEMBER 롤만 부여하며, 세부 액션은 ACL 룰로 제어됩니다.

### 사용자 뷰용 권한 상태 API
- **GET `/api/forums/{forumSlug}/authz`**  
  로그인한 사용자/역할 조합에서 `{forumSlug}` 게시판의 `PermissionAction`별 `allowed` 여부만 리턴합니다. 일반 뷰에서 메뉴/버튼 노출을 판단할 때 메뉴가 사용할 수 있습니다.
- **GET `/api/forums/{forumSlug}/authz/simulate`**  
  일반 사용자도 접근 가능한 시뮬레이터이며, `action`, `role`, `categoryId`, `ownerId`, `locked`, `userId`, `username`을 받아 policy+ACL 결과를 리턴합니다. 관리자 `/permissions/check`처럼 정책/ACL 평가 결과를 동일하게 돌려주지만, 게시판 뷰에서는 GET만 지원하므로 POST/PATCH/DELETE로 접근하면 `error.request.method.not-allowed`가 발생합니다.

### 역할별 권한 매트릭스 가이드 (Vue UI 참고 데이터)
```
export const rolePermissionRows: RolePermissionRow[] = [
  {
    role: "OWNER",
    label: "OWNER (소유자)",
    basic:
      "READ_*, CREATE_TOPIC, REPLY_POST, UPLOAD_ATTACHMENT (자신 글만), EDIT_TOPIC, DELETE_TOPIC, EDIT_POST, DELETE_POST (자신 글만)",
    admin:
      "HIDE_POST/LOCK_TOPIC/MANAGE_BOARD 등 관리자 액션은 ACL에서 ALLOW 필요",
    note:
      "ForumAccessResolver에서 OWNER는 ADMIN으로 매핑되어 정책/ACL 평가에서 관리자 후보군",
    grantedActions: ["READ_BOARD", "READ_TOPIC_LIST", "READ_TOPIC_CONTENT", "READ_ATTACHMENT", "CREATE_TOPIC", "EDIT_TOPIC", "DELETE_TOPIC", "REPLY_POST", "UPLOAD_ATTACHMENT", "EDIT_POST", "DELETE_POST"],
    adminActions: [],
  },
  {
    role: "ADMIN",
    label: "ADMIN (관리자)",
    basic: "OWNER과 동일 + 첨부파일 업로드/삭제 포함 + 관리자 전용 요청(기본 DENY)",
    admin: "관리자 전용 액션은 ACL에서 명시적으로 ALLOW",
    note: "OWNER/ADMIN/studio.features.forums.authz.admin-roles가 동일하게 취급",
    grantedActions: ["READ_BOARD", "READ_TOPIC_LIST", "READ_TOPIC_CONTENT", "READ_ATTACHMENT", "CREATE_TOPIC", "EDIT_TOPIC", "DELETE_TOPIC", "REPLY_POST", "UPLOAD_ATTACHMENT", "EDIT_POST", "DELETE_POST", "HIDE_POST", "MODERATE", "PIN_TOPIC", "LOCK_TOPIC", "MANAGE_BOARD"],
    adminActions: ["HIDE_POST", "MODERATE", "PIN_TOPIC", "LOCK_TOPIC", "MANAGE_BOARD"],
  },
  {
    role: "MODERATOR",
    label: "MODERATOR (모더레이터)",
    basic: "READ_*, CREATE_TOPIC, REPLY_POST, UPLOAD_ATTACHMENT (자신 글만), 조건부 EDIT_POST/DELETE_POST",
    admin: "HIDE_POST, MANAGE_BOARD 등 관리자 액션을 ACL로 ALLOW하면 운영자 기능 수행",
    note: "ForumAccessResolver.isAdmin에서 관리자처럼 처리되어 ACL만 추가하면 운영자 기능 가능",
    grantedActions: ["READ_BOARD", "READ_TOPIC_LIST", "READ_TOPIC_CONTENT", "READ_ATTACHMENT", "CREATE_TOPIC", "REPLY_POST", "UPLOAD_ATTACHMENT", "EDIT_POST", "DELETE_POST"],
    adminActions: [],
  },
  {
    role: "MEMBER",
    label: "MEMBER (일반 멤버)",
    basic: "READ_*, CREATE_TOPIC, REPLY_POST (LOCKED 토픽 제한), UPLOAD_ATTACHMENT (자신 글만), 본인 글 EDIT/DELETE",
    admin: "HIDE_POST 등은 기본 DENY → ForumAclRule로 추가",
    note: "일반 사용자, 확장 권한은 ACL 룰로 제어",
    grantedActions: ["READ_BOARD", "READ_TOPIC_LIST", "READ_TOPIC_CONTENT", "READ_ATTACHMENT", "CREATE_TOPIC", "REPLY_POST", "UPLOAD_ATTACHMENT", "EDIT_POST", "DELETE_POST"],
  },
];
```

### UI 구성 시 체크리스트
- 관리자 화면에서는 `/api/mgmt/forums/{forumSlug}/permissions`만 호출하고 일반 `/api/forums/...` 뷰에서는 이 엔드포인트를 직접 사용하지 마세요. 대신 `/api/forums/{forumSlug}/authz`와 `/authz/simulate`로 현재 사용자 권한을 확인합니다.
- `/authz/simulate`는 GET만 허용하므로 POST/PATCH/DELETE로 접근하면 405(`error.request.method.not-allowed`) 에러가 납니다.
- 첨부파일은 본문(`READ_TOPIC_CONTENT`)과 별개로 `READ_ATTACHMENT`/`UPLOAD_ATTACHMENT`로 제어됩니다. 공개 글이라도 다운로드를 막고 싶으면 `READ_ATTACHMENT`에 `DENY` 룰을 추가하세요.
- ACL 룰 추가 요청의 INSERT 결과가 `rule_id` 외에도 `board_id` 등이 섞여 있으면 `GeneratedKeyHolder.getKey()`가 `InvalidDataAccessApiUsageException`을 던집니다. 쿼리에서는 `RETURNING rule_id`만 가져오거나 `GeneratedKeyHolder.getKeyMap()`을 사용해 다중 키를 처리하세요.

## 검색 파라미터
- `q`: 검색어
- `in`: 검색 대상 필드 목록 (예: `title,tags`)
- `fields`: 응답 필드 선택 (예: `topicId,title,updatedAt,postCount,lastActivityAt`)
- `page`, `size`, `sort`

## TopicSummaryResponse 확장 필드
- `createdById`, `createdBy`
- `postCount`
- `lastPostUpdatedAt`
- `lastPostUpdatedById`, `lastPostUpdatedBy`
- `lastPostId`
- `lastActivityAt` (댓글이 있으면 마지막 댓글 수정일, 없으면 토픽 updatedAt)
- `excerpt` (마지막 댓글 내용 200자 요약)

## 목록 정렬 기본값
- Topic 목록 기본 정렬: `lastActivityAt desc, topicId desc`

## 관리자 목록 옵션
- `includeHidden`: 숨김 댓글 포함 여부 (기본값 `false`)

## 정렬 가능한 Topic 목록 필드
- `updatedAt`
- `postCount`
- `lastPostUpdatedAt`
- `lastPostUpdatedById`
- `lastPostId`
- `lastActivityAt`

## Topic 목록 응답 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "title": "첫 번째 토픽",
        "status": "OPEN",
        "updatedAt": "2026-01-20T10:15:30+09:00",
        "createdById": 7,
        "createdBy": "alice",
        "postCount": 5,
        "lastPostUpdatedAt": "2026-01-22T09:40:10+09:00",
        "lastPostUpdatedById": 12,
        "lastPostUpdatedBy": "bob",
        "lastPostId": 5501,
        "lastActivityAt": "2026-01-22T09:40:10+09:00",
        "excerpt": "마지막 댓글 요약 내용..."
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## Forum 목록 응답 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "slug": "general",
        "name": "General",
        "viewType": "GENERAL",
        "updatedAt": "2026-01-22T08:10:00+09:00",
        "topicCount": 12,
        "postCount": 58,
        "lastActivityAt": "2026-01-22T09:40:10+09:00",
        "lastActivityById": 12,
        "lastActivityBy": "bob",
        "lastActivityType": "POST",
        "lastActivityId": 5501
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## 동시성 제어 (ETag/If-Match)
- 조회 응답에 ETag(`W/"{version}"`) 반환
- 수정/상태변경 요청은 `If-Match` 필요
- `If-Match` 누락: 428 Precondition Required
- `If-Match` 유효하지 않음: 412 Precondition Failed

## If-Match 오류 응답 예시
```json
{
  "success": false,
  "error": {
    "code": "error.http.precondition.required",
    "message": "If-Match header is required"
  }
}
```
```json
{
  "success": false,
  "error": {
    "code": "error.http.precondition.failed",
    "message": "Invalid If-Match header"
  }
}
```

## Forum viewType/properties
- `viewType`: `GENERAL | GALLERY | VIDEO | LIBRARY | NOTICE` (기본값 `GENERAL`)
- 저장/응답 properties는 whitelist 키만 허용/노출
  - `viewType`
  - `media.allowedExt`
  - `library.maxFileMb`

## Forum properties 검증 실패 예시
```json
{
  "success": false,
  "error": {
    "message": "unknown forum properties: someKey"
  }
}
```

## 데이터 필드
- createdById, createdBy, createdAt
- updatedById, updatedBy, updatedAt

## JDBC SqlQuery
- SQL 파일: `src/main/resources/sql/forums-sqlset.xml`
- 동적 구문은 `<dynamic>` + Freemarker 사용

## 스키마
- PostgreSQL: `src/main/resources/schema/forums-postgres.sql`
- MySQL: `src/main/resources/schema/forums-mysql.sql`
- 테이블 prefix: `tb_application_`

## 빌드
```
./gradlew build
```

## Starter (자동 설정)
- 경로: `starter/src/main/java/studio/one/application/forums/autoconfigure/ForumsAutoConfiguration.java`
- 활성화: `features.forums.enabled=true`

## 설정 예시
```properties
features.forums.enabled=true
features.forums.web.enabled=true
studio.features.forums.web.base-path=/api/forums
studio.features.forums.web.mgmt-base-path=/api/mgmt/forums
studio.features.forums.cache.enabled=true
studio.features.forums.cache.list-ttl=60s
studio.features.forums.cache.detail-ttl=5m
studio.features.forums.cache.list-max-size=10000
studio.features.forums.cache.detail-max-size=50000
studio.features.forums.cache.record-stats=true
studio.features.forums.authz.admin-roles=ROLE_ADMIN,ADMIN
studio.features.forums.authz.secret-list-visible=false
features.forums.persistence=jdbc
```

## 설정 키
| Key | 설명 | 기본값 |
| --- | --- | --- |
| `features.forums.enabled` | forums 기능 활성화 | `false` |
| `features.forums.web.enabled` | forums 웹 컨트롤러 활성화 | `true` |
| `studio.features.forums.web.base-path` | forums 공개 API 기본 경로 | `/api/forums` |
| `studio.features.forums.web.mgmt-base-path` | forums 관리자 API 기본 경로 | `/api/mgmt/forums` |
| `studio.features.forums.cache.enabled` | forums 캐시 사용 여부 | `true` |
| `studio.features.forums.cache.list-ttl` | forums 목록 캐시 TTL | `60s` |
| `studio.features.forums.cache.detail-ttl` | forums 상세 캐시 TTL | `5m` |
| `studio.features.forums.cache.list-max-size` | forums 목록 캐시 최대 항목 수 | `10000` |
| `studio.features.forums.cache.detail-max-size` | forums 상세 캐시 최대 항목 수 | `50000` |
| `studio.features.forums.cache.record-stats` | forums 캐시 통계 수집 | `true` |
| `studio.features.forums.authz.admin-roles` | 관리자 역할 목록 (쉼표 구분) | `ROLE_ADMIN,ADMIN` |
| `studio.features.forums.authz.secret-list-visible` | SECRET 게시판 목록 노출 여부 | `false` |
| `features.forums.persistence` | persistence 선택 (`jpa` or `jdbc`) | 글로벌 기본값 |
| `features.forums.entity-packages` | JPA 엔티티 스캔 패키지 | `studio.one.application.forums.persistence.jpa.entity` |

## 게시판 타입 정책
- COMMON: 누구나 읽기, 회원 쓰기
- NOTICE: 읽기 누구나, 쓰기는 관리자/운영진만
- SECRET: 본문은 작성자/관리자만, 목록 노출은 설정으로 제어
- ADMIN_ONLY: 관리자/운영진만 목록/본문 접근

## 권한 매트릭스 (요약)
| ForumType | Actor | READ_BOARD | READ_TOPIC | CREATE_TOPIC | EDIT_TOPIC | DELETE_TOPIC | REPLY_POST | EDIT_POST | DELETE_POST |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| COMMON | anonymous | Y | Y | N | N | N | N | N | N |
| COMMON | member | Y | Y | Y | Y* | Y* | Y | Y* | Y* |
| COMMON | admin | Y | Y | Y | Y | Y | Y | Y | Y |
| NOTICE | anonymous | Y | Y | N | N | N | N | N | N |
| NOTICE | member | Y | Y | N | N | N | N | N | N |
| NOTICE | admin | Y | Y | Y | Y | Y | Y | Y | Y |
| SECRET | anonymous | N | N | N | N | N | N | N | N |
| SECRET | member | Y** | Y** | Y | Y* | Y* | Y | Y* | Y* |
| SECRET | admin | Y | Y | Y | Y | Y | Y | Y | Y |
| ADMIN_ONLY | anonymous | N | N | N | N | N | N | N | N |
| ADMIN_ONLY | member | N | N | N | N | N | N | N | N |
| ADMIN_ONLY | admin | Y | Y | Y | Y | Y | Y | Y | Y |
*작성자/관리자만 허용, **작성자/관리자 및 멤버십/설정에 따라 목록 노출

### 롤별 기본 역할  
- `OWNER` : 작성자 본인 글에 대한 `EDIT_*`/`DELETE_*`가 정책에 따라 허용되며, `HANDLE`/운영 액션(`MANAGE_BOARD`, `MODERATE`, `HIDE_POST` 등)은 ACL로 `role=OWNER` + `action` 조합을 `effect=ALLOW`로 명시해야 제공됩니다.  
- `ADMIN` : 일반 게시글/댓글 작업과 관리자 전용 기능 모두 기본적으로 허용됩니다. ACL 계산에서도 가장 먼저 확인하며, `studio.features.forums.authz.admin-roles`에 있는 다른 역할도 같은 권한을 가집니다.
- `MODERATOR` : 일반적인 게시글 쓰기/답글/목록 조회는 MEMBERSHIP과 동일하지만, `HIDE_POST`/`LOCK_TOPIC` 등 고급 액션은 기본적으로 DENY입니다. ACL로 `role=MODERATOR` + `action`을 `ALLOW`하면 관리자처럼 기능을 수행합니다.  
- `MEMBER` : 일반적인 글 읽기/쓰기/댓글과 본인 글에 대한 수정/삭제는 허용되며, NOTICE/SECRET처럼 일부 게시판이나 LOCKED 토픽에서는 제한됩니다. 부족한 기능은 ACL(`role=MEMBER` + `action`)을 추가하면 해결됩니다.
- `anonymous` : 포럼/목록/본문 읽기만 허용되며, 특정 사용자에 대해 추가 권한을 주려면 `subjectType=USER`/`identifierType` 조합으로 ACL 룰을 등록하십시오. 

## 상태 변경/숨김 옵션
- Topic 관리: `status`, `pin`, `lock` (관리자)
- Post 관리: `hide` (관리자)

## 게시판별 멤버십
- 테이블: `tb_application_forum_member`
- 역할: `OWNER`, `ADMIN`, `MODERATOR`, `MEMBER`
- 권한 계산: 글로벌 роли + 게시판별 멤버십을 합쳐서 평가

## 캐시 키
- `forums.list`
- `forums.bySlug`
- `forums.categories.byForum`
- `forums.topics.byId`
- `forums.topics.list.{forumSlug}`
- `forums.posts.list.{topicId}`

## 캐시 TTL
- TTL은 CacheManager 설정에서 조정합니다.
- 목록 캐시는 짧은 TTL(예: 30~60초), 상세 캐시는 중간 TTL(예: 3~5분)로 시작하는 것을 권장합니다.
- forums 모듈은 `studio.features.forums.cache.list-ttl`, `studio.features.forums.cache.detail-ttl`로 기본 TTL을 조정합니다.
- Caffeine CacheManager 사용 시에만 TTL/max-size/통계 옵션이 적용됩니다.

## ACL 엔트리 관리 예시
- **관리 범위**: `forumSlug` 단위로 읽기/쓰기/삭제 권한을 부여/회수합니다.
- **대상(SID)**: 사용자(`principal`) 또는 역할(`role`)을 기준으로 설정합니다.
- **권한**: `read`, `write`, `delete`, `admin` 중 하나를 지정합니다.

권한 부여 예시 (특정 사용자에게 읽기 권한 부여)
```json
{
  "sidType": "principal",
  "sid": "user123",
  "permission": "read",
  "granting": true
}
```

권한 부여 예시 (역할에 쓰기 권한 부여)
```json
{
  "sidType": "role",
  "sid": "ROLE_FORUM_MODERATOR",
  "permission": "write",
  "granting": true
}
```

권한 회수 예시 (특정 사용자 읽기 권한 회수)
```json
{
  "sidType": "principal",
  "sid": "user123",
  "permission": "read",
  "granting": true
}
```
