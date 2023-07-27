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

package com.oceanbase.ocp.obsdk.operator.sql.model;

import static com.oceanbase.ocp.common.util.time.TimeUtils.usToUtcString;

import java.nio.ByteBuffer;
import java.util.Base64;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanRawStatEntity;

import lombok.Data;

@Data
public final class PlanUid {

    public final Long obServerId;
    public final Long planId;
    public final Long firstLoadTimeUs;

    public static PlanUid of(Long obServerId, Long planId, Long firstLoadTimeUs) {
        Validate.notNull(obServerId, "OB Server Id");
        Validate.notNull(planId, "Plan Id");
        Validate.notNull(firstLoadTimeUs, "Plan first load time");
        return new PlanUid(obServerId, planId, firstLoadTimeUs);
    }

    public static PlanUid from(PlanEntity refer) {
        return of(refer.obServerId, refer.planId, refer.firstLoadTime);
    }

    public static PlanUid from(PlanRawStatEntity refer) {
        return of(refer.obServerId, refer.planId, refer.firstLoadTimeUs);
    }

    /**
     * Parse planUid from raw string.
     *
     * @param value Raw string of UID
     * @return PlanUid
     */
    public static PlanUid from(String value) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getUrlDecoder().decode(value));
            return new PlanUid(buffer.getLong(), buffer.getLong(), buffer.getLong());
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Not valid Plan UID: %s", value), e);
        }
    }

    @Override
    public String toString() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(obServerId);
        buffer.putLong(planId);
        buffer.putLong(firstLoadTimeUs);
        return Base64.getUrlEncoder().encodeToString(buffer.array());
    }

    public String info() {
        return String.format("uid=%s, server_id=%d, plan_id=%d, first_load_time=%s(%d)", toString(), obServerId, planId,
                usToUtcString(firstLoadTimeUs), firstLoadTimeUs);
    }
}
