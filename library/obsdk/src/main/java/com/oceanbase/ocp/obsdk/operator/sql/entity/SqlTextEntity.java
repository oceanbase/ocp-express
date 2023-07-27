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

package com.oceanbase.ocp.obsdk.operator.sql.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SqlTextEntity {

    @JsonProperty("ObTenantId")
    public Long obTenantId;

    @JsonProperty("CollectTimeUs")
    public Long collectTimeUs;

    @JsonProperty("TenantName")
    public String tenantName;

    @JsonProperty("ObServerId")
    public Long obServerId;

    @JsonProperty("ObDbId")
    public Long obDbId;

    @JsonProperty("DbName")
    public String dbName;

    @JsonProperty("ObUserId")
    public Long obUserId;

    @JsonProperty("UserName")
    public String userName;

    @JsonProperty("SqlId")
    public String sqlId;

    @JsonProperty("SqlText")
    public String sqlText;

    @JsonProperty("CreateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp createTime;

    @Column(name = "statement")
    @JsonProperty("Statement")
    public String statement;

    @JsonProperty("SqlType")
    public String sqlType;

    @JsonProperty("TableList")
    public String tableList;


    @Data
    public static final class Key implements Serializable {

        private static final long serialVersionUID = -1727425841060721963L;

        public Long obClusterId;
        public String clusterName;
        public Long obTenantId;
        public Long collectTimeUs;
        public Long obServerId;
        public Long obDbId;
        public Long obUserId;
        public String sqlId;
    }

    @Override
    public String toString() {
        return "SqlTextEntity{" +
                "sqlId='" + sqlId + '\'' +
                '}';
    }

    @Transient
    public Integer truncated;
}
