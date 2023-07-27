/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.oceanbase.ocp.security.iam.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.core.security.model.AuthenticatedUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class UserEntity extends UserBaseEntity {

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "need_change_password", nullable = false)
    private boolean needChangePassword;

    @Column(name = "lock_expired_time", columnDefinition = "DATETIME")
    private OffsetDateTime lockExpiredTime;

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    public boolean isAccountManualOrAutoLocked() {
        return isAccountManualLocked() || isAccountAutoLocked();
    }

    public boolean isAccountAutoLocked() {
        return this.accountLocked && this.lockExpiredTime != null && this.lockExpiredTime.isAfter(OffsetDateTime.now());
    }

    public boolean isAccountManualLocked() {
        return this.accountLocked && this.lockExpiredTime == null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("username", getUsername())
                .toString();
    }

    public AuthenticatedUser toAuthenticatedUser() {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setId(this.getId());
        user.setUsername(this.getUsername());
        user.setPassword(this.getPassword());
        user.setAccountLocked(this.isAccountManualOrAutoLocked());
        user.setEnabled(this.isEnabled());
        user.setCreateTime(this.getCreateTime());
        user.setUpdateTime(this.getUpdateTime());
        user.setLockExpiredTime(this.getLockExpiredTime());
        user.setNeedChangePassword(this.isNeedChangePassword());
        return user;
    }

}
