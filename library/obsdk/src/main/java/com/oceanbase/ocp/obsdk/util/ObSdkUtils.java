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

package com.oceanbase.ocp.obsdk.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.alibaba.druid.util.JdbcUtils;

import com.oceanbase.ocp.common.version.Version;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerInnerStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides utility methods for ob sdk
 *
 */
@Slf4j
public class ObSdkUtils {

    private static final Pattern OB_TENANT_GROUPS =
            Pattern.compile("((\\(\\w+(,\\w+)+\\))|(\\(\\(\\w+(,\\w+)+\\)(,\\(\\w+(,\\w+)+\\))+\\)))"
                    + "(,((\\(\\w+(,\\w+)+\\))|(\\(\\(\\w+(,\\w+)+\\)(,\\(\\w+(,\\w+)+\\))+\\))))*");
    private static final Pattern OB_TENANT_GROUP_ROW = Pattern.compile("(\\w,)+\\w");
    private static final Pattern OB_TENANT_GROUP_ARRAY = Pattern.compile("(\\(\\w+(,\\w+)+\\))(,\\(\\w+(,\\w+)+\\))*");

    // Suppresses default constructor, ensuring non-instantiability.
    private ObSdkUtils() {}

    /**
     * Join the elements of the provided list into a single String separated by
     * comma, for example: zone1,zone2,zone3
     *
     * @param list the list of values to join together, may be null
     * @return the joined String, or empty String if null list input
     */
    public static String toCommaSeparatedString(List<?> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return StringUtils.join(list.toArray(), ",");
    }

    /**
     * Join the elements of the provided list into a single String separated by
     * comma with quotation marks, for example: 'zone1','zone2','zone3'
     *
     * @param list the list of values to join together, may be null
     * @return the joined String, or empty String if null list input
     */
    public static String toCommaSeparatedStringWithQuotationMark(List<?> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return StringUtils.join(list.stream().map(t -> "'" + t + "'").toArray(), ",");
    }

    /**
     * Compare current version to target version. Returns true when current version
     * is before target version (not equal to).
     *
     * @param currVersion current version
     * @param tarVersion target version
     * @return true when current version is before target version (not equal to),
     *         otherwise returns false.
     * @throws NullPointerException if the input {@code currVersion} or
     *         {@code tarVersion} is {@code null}
     * @throws IllegalArgumentException if the input {@code currVersion} or
     *         {@code tarVersion} is not valid
     */
    public static boolean versionBefore(String currVersion, String tarVersion) {
        Validate.notNull(currVersion, "The input currVersion is null.");
        Validate.notNull(tarVersion, "The input tarVersion is null.");

        Version currVer = new Version(currVersion);
        Version tarVer = new Version(tarVersion);
        return tarVer.compareTo(currVer) > 0;
    }

    /**
     * Compare current version to target version. Returns true when current version
     * ia equal to or after target version.
     *
     * @param currVersion current version
     * @param tarVersion target version
     * @return true when current version ia equal to or after target version,
     *         otherwise returns false.
     * @throws NullPointerException if the input {@code currVersion} or
     *         {@code tarVersion} is {@code null}
     * @throws IllegalArgumentException if the input {@code currVersion} or
     *         {@code tarVersion} is not valid
     */
    public static boolean versionAfter(String currVersion, String tarVersion) {
        return !versionBefore(currVersion, tarVersion);
    }

    /**
     * check if current version is after or equals target version
     *
     * @param currVersion current version
     * @param tarVersion target version
     * @throws UnsupportedOperationException if the input {@code currVersion} is
     *         before {@code tarVersion}
     */
    public static void versionShouldAfter(String currVersion, String tarVersion) {
        if (ObSdkUtils.versionBefore(currVersion, tarVersion)) {
            log.error("Operation is not supported, version should be after {}, current version:{}", tarVersion,
                    currVersion);
            throw new UnsupportedOperationException("Operation is not supported, version should be after " + tarVersion
                    + ", current version:" + currVersion);
        }
    }

    /**
     * check if current version is before target version
     *
     * @param currVersion current version
     * @param tarVersion target version
     * @throws UnsupportedOperationException if the input {@code currVersion} is
     *         after or equals {@code tarVersion}
     */
    public static void versionShouldBefore(String currVersion, String tarVersion) {
        if (ObSdkUtils.versionAfter(currVersion, tarVersion)) {
            log.error("Operation is not supported, version should be before {}, current version:{}", tarVersion,
                    currVersion);
            throw new UnsupportedOperationException("Operation is not supported, version should be before " + tarVersion
                    + ", current version:" + currVersion);
        }
    }

    /**
     * If the input value is greater than the max value, than return the max value.
     * If the input value is less the min value, then return the min value.
     * Otherwise, return the value itself.
     *
     * @param min min value
     * @param max max value
     * @param value input value
     * @return restricted value
     */
    public static long restrictValue(long min, long max, long value) {
        long res = value;
        res = Math.max(res, min);
        res = Math.min(res, max);
        return res;
    }

    /**
     * Split value unit
     *
     * <pre>
     * e.g.
     * - '12 GB' --> ['12', 'GB']
     * - '130ms' --> ['130', 'ms']
     * </pre>
     *
     * @param text
     * @return
     */
    public static String[] splitValueUnit(String text) {
        Validate.notNull(text, "text is null");
        char[] chars = text.toCharArray();
        int unitStartPos = chars.length;
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isDigit(chars[i]) && '-' != chars[i]) {
                unitStartPos = i;
                break;
            }
        }
        if (unitStartPos == chars.length) {
            return new String[] {text, ""};
        }
        String[] valueUnit = new String[2];
        valueUnit[0] = new String(chars, 0, unitStartPos);
        valueUnit[1] = new String(chars, unitStartPos, chars.length - unitStartPos).trim();
        return valueUnit;
    }

    public static List<String> parseTenantGroups(String tenantGroupsString) {
        List<String> tenantGroupsStr = new ArrayList<>();
        if (StringUtils.isNoneEmpty(tenantGroupsString)) {
            String tenantGroups = StringUtils.deleteWhitespace(tenantGroupsString);

            if (!OB_TENANT_GROUPS.matcher(tenantGroups).matches()) {
                log.warn("invalid tenantGroups, tenantGroups={}", tenantGroups);
                throw new IllegalArgumentException("invalid tenantGroups info: " + tenantGroups);
            }
            Stack<Character> sc = new Stack<>();
            int startIndex = 0;
            for (int i = 0; i < tenantGroups.length(); i++) {
                Character ch = tenantGroups.charAt(i);
                if (ch.equals('(')) {
                    if (sc.empty()) {
                        startIndex = i + 1;
                    }
                    sc.push(ch);
                } else if (ch.equals(')')) {
                    sc.pop();
                    if (sc.empty()) {
                        tenantGroupsStr.add(tenantGroups.substring(startIndex, i));
                        startIndex = 0;
                    }
                }
            }
        }
        return tenantGroupsStr;
    }

    public static List<String> parseTenantGroup(String tenantGroupString) {
        List<String> tenantNames = new ArrayList<>();
        if (StringUtils.isNoneEmpty(tenantGroupString)) {
            String tenantGroup = StringUtils.deleteWhitespace(tenantGroupString);
            if (!tenantGroup.startsWith("(")) {
                if (!OB_TENANT_GROUP_ROW.matcher(tenantGroup).matches()) {
                    log.warn("get invalid tenant group, tenant_group = {}", tenantGroup);
                    throw new IllegalArgumentException("invalid tenantGroup info: " + tenantGroup);
                }
                tenantNames.addAll(Arrays.asList(tenantGroup.split(",")));
            } else {
                if (!OB_TENANT_GROUP_ARRAY.matcher(tenantGroup).matches()) {
                    log.warn("get invalid tenant group, tenant_group = {}", tenantGroup);
                    throw new IllegalArgumentException("invalid tenantGroup info: " + tenantGroup);
                }
                int startIndex = 0;
                for (int i = 0; i < tenantGroup.length(); i++) {
                    if (tenantGroup.charAt(i) == '(') {
                        startIndex = i + 1;
                    } else if (tenantGroup.charAt(i) == ')') {
                        tenantNames.addAll(Arrays.asList(tenantGroup.substring(startIndex, i).split(",")));
                    }
                }
            }
        }
        return tenantNames;
    }

    public static String deleteEscapeChar(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        value = StringUtils.replace(value, "\\%", "%");
        return StringUtils.replace(value, "\\_", "_");
    }


    public static long userObjectIdToSysObjectId(long userObjectId) {
        return (userObjectId << 24 >> 24) | (1L << 40);
    }


    public static long sysObjectIdToUserObjectId(long sysObjectId, long tenantId) {
        return (sysObjectId << 24 >> 24) | (tenantId << 40);
    }


    public static boolean isServerActive(ObServer observer) {
        Validate.notNull(observer, "observer is null");
        String status = observer.getStatus();
        Long startServiceTime = observer.getStartServiceTime();
        boolean result = ObServerInnerStatus.fromValue(status) == ObServerInnerStatus.ACTIVE && startServiceTime > 0L;
        log.info("check observer active, observer={}, status={}, startServiceTime={}, active={}", observer.getAddress(),
                status, startServiceTime, result);
        return result;
    }


    public static boolean isServerStopped(ObServer observer) {
        Validate.notNull(observer, "observer is null");
        Long stopTime = observer.getStopTime();
        boolean result = stopTime > 0L;
        log.info("check observer stopped, observer={}, stopTime={}, stopped={}", observer.getAddress(), stopTime,
                result);
        return result;
    }


    public static boolean checkConnection(Connection conn, String validQuerySql, int timeoutSeconds) {
        Statement stmt = null; // NOPMD
        ResultSet rs = null; // NOPMD

        try {
            stmt = conn.createStatement();
            if (timeoutSeconds > 0) {
                stmt.setQueryTimeout(timeoutSeconds);
            }
            rs = stmt.executeQuery(validQuerySql);
            return true;
        } catch (Exception ex) {
            log.warn("failed to valid connection, errMsg:{}, cause:{}", ex.getMessage(), ex.getCause());
            return false;
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
        }
    }


    public static String parseVersionComment(String versionComment) {
        if (StringUtils.isEmpty(versionComment)) {
            throw new OceanBaseException("version_comment is empty");
        }
        return versionComment.split(" ")[1];
    }
}
