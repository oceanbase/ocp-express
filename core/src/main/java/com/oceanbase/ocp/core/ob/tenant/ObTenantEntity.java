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

package com.oceanbase.ocp.core.ob.tenant;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ob_tenant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ObTenantEntity {

    @Deprecated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @Column(name = "create_time", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    @Column(name = "creator")
    private String creator;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ob_tenant_id", nullable = false)
    private Long obTenantId;

    @Column(name = "mode")
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private TenantMode mode = TenantMode.MYSQL;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private TenantStatus status = TenantStatus.CREATING;

    @Column(name = "is_locked")
    @Builder.Default
    private Boolean locked = false;

    @Column(name = "is_read_only")
    @Builder.Default
    private Boolean readonly = false;

    @Column(name = "primary_zone")
    private String primaryZone;

    @Column(name = "zone_list", nullable = false)
    private String zoneListStr;

    @Column(name = "locality", nullable = false)
    private String locality;

    @Column(name = "description")
    private String description;


    public List<String> getZoneList() {
        if (StringUtils.isEmpty(zoneListStr)) {
            return Collections.emptyList();
        }
        return Stream.of(zoneListStr.split(";")).collect(Collectors.toList());
    }

    public void setZoneList(List<String> zoneList) {
        if (zoneList == null) {
            zoneListStr = null;
        } else {
            zoneListStr = String.join(";", zoneList);
        }
    }

    public boolean isMysqlMode() {
        return mode == TenantMode.MYSQL;
    }

    public boolean isOracleMode() {
        return mode == TenantMode.ORACLE;
    }

}
