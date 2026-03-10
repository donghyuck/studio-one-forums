# Security Change Summary

## What changed
- Added service-layer path consistency validation for `forumSlug`, `topicId`, `postId`, and `attachmentId`
- Added authorization on attachment thumbnail access
- Tightened attachment deletion to `EDIT_POST`
- Added upload validation for size, extension, MIME type, and file signature
- Added in-memory rate limiting for attachment uploads and thumbnail generation
- Removed remaining unsafe `${...}` SQL assembly from topic/post query repositories
- Added security-oriented audit logs with stable `security_event=` fields

## Validation
- `./gradlew test`

## Deployment notes
- Review and align `studio.features.forums.attachments.*` rate-limit and size properties with production traffic
- Verify top-level Spring Security/JWT/CORS/CSRF settings using `docs/security-followups.md`

## Recommended PR description snippet
- Risk addressed: unauthorized attachment exposure, path mismatch abuse, unsafe file uploads, and dynamic SQL misuse
- Verification: unit tests added for guard logic, rate limiter, controller security annotation, and attachment validation
