package studio.one.application.forums.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.web.dto.AclDtos;
import studio.one.platform.web.dto.ApiResponse;

/**
 * Forums 관리자 REST 컨트롤러.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@ConditionalOnBean(MutableAclService.class)
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/{forumSlug}/permissions")
public class ForumAclAdminController {
    private final ForumQueryService forumQueryService;
    private final MutableAclService aclService;

    public ForumAclAdminController(ForumQueryService forumQueryService, MutableAclService aclService) {
        this.forumQueryService = forumQueryService;
        this.aclService = aclService;
    }

    @GetMapping
    @PreAuthorize("@forumsAuthz.canManageForumAcl(#forumSlug)")
    public ResponseEntity<ApiResponse<List<AclDtos.AclEntryResponse>>> listPermissions(@PathVariable String forumSlug) {
        ObjectIdentity objectIdentity = resolveForumIdentity(forumSlug);
        List<AclDtos.AclEntryResponse> entries = new ArrayList<>();
        try {
            Acl acl = aclService.readAclById(objectIdentity);
            for (AccessControlEntry entry : acl.getEntries()) {
                entries.add(toResponse(entry));
            }
        } catch (NotFoundException ex) {
            // No ACL defined yet.
        }
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }

    @PostMapping
    @PreAuthorize("@forumsAuthz.canManageForumAcl(#forumSlug)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> grantPermission(@PathVariable String forumSlug,
                                                                            @RequestBody AclDtos.PermissionRequest request) {
        ObjectIdentity objectIdentity = resolveForumIdentity(forumSlug);
        MutableAcl acl = getOrCreateAcl(objectIdentity);
        Sid sid = resolveSid(request.getSidType(), request.getSid());
        Permission permission = resolvePermission(request.getPermission());
        boolean granting = request.getGranting() == null || request.getGranting();

        boolean updated = ensureAce(acl, sid, permission, granting);
        if (updated) {
            aclService.updateAcl(acl);
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", updated)));
    }

    @DeleteMapping
    @PreAuthorize("@forumsAuthz.canManageForumAcl(#forumSlug)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> revokePermission(@PathVariable String forumSlug,
                                                                             @RequestBody AclDtos.PermissionRequest request) {
        ObjectIdentity objectIdentity = resolveForumIdentity(forumSlug);
        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(objectIdentity);
        } catch (NotFoundException ex) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", false)));
        }

        Sid sid = resolveSid(request.getSidType(), request.getSid());
        Permission permission = resolvePermission(request.getPermission());
        Boolean granting = request.getGranting();

        boolean updated = removeAce(acl, sid, permission, granting);
        if (updated) {
            aclService.updateAcl(acl);
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", updated)));
    }

    private ObjectIdentity resolveForumIdentity(String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return new ObjectIdentityImpl(Forum.class, view.getId());
    }

    private MutableAcl getOrCreateAcl(ObjectIdentity objectIdentity) {
        try {
            return (MutableAcl) aclService.readAclById(objectIdentity);
        } catch (NotFoundException ex) {
            return aclService.createAcl(objectIdentity);
        }
    }

    private boolean ensureAce(MutableAcl acl, Sid sid, Permission permission, boolean granting) {
        int mask = permission.getMask();
        for (AccessControlEntry entry : acl.getEntries()) {
            if (entry.getSid().equals(sid)
                && entry.getPermission().getMask() == mask
                && entry.isGranting() == granting) {
                return false;
            }
        }
        acl.insertAce(acl.getEntries().size(), permission, sid, granting);
        return true;
    }

    private boolean removeAce(MutableAcl acl, Sid sid, Permission permission, Boolean granting) {
        boolean removed = false;
        List<AccessControlEntry> entries = acl.getEntries();
        int mask = permission.getMask();
        for (int i = entries.size() - 1; i >= 0; i--) {
            AccessControlEntry entry = entries.get(i);
            if (!entry.getSid().equals(sid)) {
                continue;
            }
            if (entry.getPermission().getMask() != mask) {
                continue;
            }
            if (granting != null && entry.isGranting() != granting) {
                continue;
            }
            acl.deleteAce(i);
            removed = true;
        }
        return removed;
    }

    private Sid resolveSid(String sidType, String sidValue) {
        String type = requireValue("sidType", sidType).toLowerCase(Locale.ROOT);
        String value = requireValue("sid", sidValue);
        if ("role".equals(type) || "authority".equals(type)) {
            return new GrantedAuthoritySid(value);
        }
        if ("principal".equals(type) || "user".equals(type)) {
            return new PrincipalSid(value);
        }
        throw new IllegalArgumentException("sidType must be principal or role");
    }

    private Permission resolvePermission(String permissionValue) {
        String value = requireValue("permission", permissionValue).toLowerCase(Locale.ROOT);
        switch (value) {
            case "read":
                return BasePermission.READ;
            case "write":
                return BasePermission.WRITE;
            case "delete":
                return BasePermission.DELETE;
            case "admin":
            case "administration":
                return BasePermission.ADMINISTRATION;
            default:
                throw new IllegalArgumentException("permission must be read, write, delete, or admin");
        }
    }

    private String requireValue(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private AclDtos.AclEntryResponse toResponse(AccessControlEntry entry) {
        AclDtos.AclEntryResponse response = new AclDtos.AclEntryResponse();
        Sid sid = entry.getSid();
        if (sid instanceof GrantedAuthoritySid) {
        response.setSidType("role");
        response.setSid(((GrantedAuthoritySid) sid).getGrantedAuthority());
        } else if (sid instanceof PrincipalSid) {
        response.setSidType("principal");
        response.setSid(((PrincipalSid) sid).getPrincipal());
        } else {
        response.setSidType("unknown");
        response.setSid(sid.toString());
        }
        response.setPermission(permissionToName(entry.getPermission()));
        response.setGranting(entry.isGranting());
        return response;
    }

    private String permissionToName(Permission permission) {
        int mask = permission.getMask();
        if (mask == BasePermission.READ.getMask()) {
            return "read";
        }
        if (mask == BasePermission.WRITE.getMask()) {
            return "write";
        }
        if (mask == BasePermission.DELETE.getMask()) {
            return "delete";
        }
        if (mask == BasePermission.ADMINISTRATION.getMask()) {
            return "admin";
        }
        return "mask:" + mask;
    }
}
