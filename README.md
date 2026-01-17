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

## 주요 기능
- Forum: 생성/목록/상세/설정 변경
- Category: 생성/목록
- Topic: 생성/목록/상세/상태변경
- Post: 생성/목록
- Membership: 게시판별 관리자/운영진/회원 관리

## 권한별 작업
Public (사용자)
- Forum: 목록/상세 조회 (게시판 타입/정책에 따라 제한)
- Category: 목록 조회
- Topic: 목록/상세 조회 (READ_LIST vs READ_CONTENT 분리)
- Post: 목록 조회

Admin (관리자)
- Forum: 생성, 설정 변경
- Category: 생성
- Topic: 생성, 상태 변경
- Post: 생성
- Membership: 게시판별 멤버/역할 관리

## API
Public
- POST `/api/forums`
- GET `/api/forums?q=&in=slug,name,description&page=&size=&sort=`
- GET `/api/forums/{forumSlug}` (ETag)
- PUT `/api/forums/{forumSlug}/settings` (If-Match)
- POST `/api/forums/{forumSlug}/categories`
- GET `/api/forums/{forumSlug}/categories`
- POST `/api/forums/{forumSlug}/categories/{categoryId}/topics`
- GET `/api/forums/{forumSlug}/topics?q=&in=title,tags&fields=...&page=&size=&sort=`
- GET `/api/forums/{forumSlug}/topics/{topicId}` (ETag)
- PATCH `/api/forums/{forumSlug}/topics/{topicId}/status` (If-Match)
- POST `/api/forums/{forumSlug}/topics/{topicId}/posts`
- GET `/api/forums/{forumSlug}/topics/{topicId}/posts?page=&size=&sort=`

Admin
- POST `/api/mgmt/forums`
- PUT `/api/mgmt/forums/{forumSlug}/settings` (If-Match)
- POST `/api/mgmt/forums/{forumSlug}/categories`
- POST `/api/mgmt/forums/{forumSlug}/categories/{categoryId}/topics`
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

## 검색 파라미터
- `q`: 검색어
- `in`: 검색 대상 필드 목록 (예: `title,tags`)
- `fields`: 응답 필드 선택 (예: `topicId,title,updatedAt`)
- `page`, `size`, `sort`

## 동시성 제어
- 조회 응답에 ETag 반환
- 수정/상태변경 요청은 If-Match 필요

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
| `studio.features.forums.authz.admin-roles` | 관리자 역할 목록 (쉼표 구분) | `ROLE_ADMIN,ADMIN` |
| `studio.features.forums.authz.secret-list-visible` | SECRET 게시판 목록 노출 여부 | `false` |

## 게시판 타입 정책
- COMMON: 누구나 읽기, 회원 쓰기
- NOTICE: 읽기 누구나, 쓰기는 관리자/운영진만
- SECRET: 본문은 작성자/관리자만, 목록 노출은 설정으로 제어
- ADMIN_ONLY: 관리자/운영진만 목록/본문 접근

## 게시판별 멤버십
- 테이블: `tb_application_forum_member`
- 역할: `OWNER`, `ADMIN`, `MODERATOR`, `MEMBER`
- 권한 계산: 글로벌 роли + 게시판별 멤버십을 합쳐서 평가
| `studio.features.forums.cache.record-stats` | forums 캐시 통계 수집 | `true` |
| `features.forums.persistence` | persistence 선택 (`jpa` or `jdbc`) | 글로벌 기본값 |
| `features.forums.entity-packages` | JPA 엔티티 스캔 패키지 | `studio.one.application.forums.persistence.jpa.entity` |

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
