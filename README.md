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

## API
- POST `/api/forums`
- GET `/api/forums`
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
features.forums.persistence=jdbc
```

## 설정 키
| Key | 설명 | 기본값 |
| --- | --- | --- |
| `features.forums.enabled` | forums 기능 활성화 | `false` |
| `features.forums.web.enabled` | forums 웹 컨트롤러 활성화 | `true` |
| `features.forums.persistence` | persistence 선택 (`jpa` or `jdbc`) | 글로벌 기본값 |
| `features.forums.entity-packages` | JPA 엔티티 스캔 패키지 | `studio.one.application.forums.persistence.jpa.entity` |
