package com.sss.app.entity.permissions;

import com.sss.app.entity.roles.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Grants a Permission to a Role. Composite-keyed join, same pattern as the
 * other library many-to-many join tables (hotel_meal_plans, etc.) rather than
 * a surrogate id, since this row has no identity beyond the pairing itself.
 */
@Entity
@Table(name = "role_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RolePermissionId.class)
public class RolePermission {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
