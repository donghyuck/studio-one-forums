# Attachment Security Ops

## Recommended defaults
- `studio.features.forums.attachments.max-upload-size-bytes=10485760`
- `studio.features.forums.attachments.upload-requests-per-minute=20`
- `studio.features.forums.attachments.thumbnail-requests-per-minute=60`

## Tune by environment
- Public community boards: keep upload rate low, thumbnail rate moderate
- Internal/private boards: raise upload rate only if large batch posting is expected
- If an API gateway already rate-limits these routes, keep application limits slightly higher to avoid duplicate user-facing failures

## Allowed file policy
- Allowed now: `png`, `jpg`, `jpeg`, `gif`, `pdf`, `txt`
- Blocked by design: `svg`, `html`, scriptable document formats
- If product requirements need office documents, add explicit extension/MIME/signature rules instead of broad wildcard allows
- Module decision: keep the current allowlist as the default release policy

## Client-facing failure reasons
- `file too large`
- `unsupported file extension`
- `unsupported content type`
- `file signature does not match content type`
- `upload rate limit exceeded`
- `thumbnail rate limit exceeded`

## Logging fields
- `security_event=attachment_uploaded`
- `security_event=attachment_deleted`
- `security_event=attachment_upload_rejected`
- `security_event=thumbnail_rejected`
- Keep current event names stable for this release candidate; add actor/user fields only with a coordinated log schema update

## Detection guidance
- Alert on repeated `attachment_upload_rejected reason=signature_mismatch`
- Alert on repeated `attachment_upload_rejected reason=rate_limit`
- Alert on repeated `thumbnail_rejected reason=rate_limit`
- Correlate by `forumSlug`, `topicId`, `postId`, `attachmentId`

## Upstream integration checklist
- Map API gateway/WAF limits to the same route set
- Ensure client UI shows validation and rate-limit errors without retry storms
- Forward `security_event=*` logs to SIEM or alerting pipeline
