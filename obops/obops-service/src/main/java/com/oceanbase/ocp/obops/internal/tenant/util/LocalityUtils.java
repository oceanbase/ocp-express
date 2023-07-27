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

package com.oceanbase.ocp.obops.internal.tenant.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalityUtils {

    private LocalityUtils() {}


    public static String scaleOut(String locality, String zone, String replicaType) {
        zone = zone.trim();
        replicaType = replicaType.trim();
        Map<String, String> replicaTypeMap = buildReplicaInfoMap(locality);
        if (replicaTypeMap.get(zone) != null) {
            log.error("locality={} already contains zone={}", locality, zone);
            throw new IllegalArgumentException(ErrorCodes.OB_TENANT_ZONE_NOT_VALID, zone);
        }
        return locality + "," + replicaType + "@" + zone;
    }

    public static String scaleIn(String locality, List<String> zoneList) {
        Validate.notEmpty(zoneList, "param zoneList must not be empty.");
        String target = locality;
        for (String zone : zoneList) {
            target = scaleIn(target, zone);
        }
        return target;
    }

    public static String scaleIn(String locality, String zone) {
        zone = zone.trim();
        Map<String, String> replicaTypeMap = buildReplicaInfoMap(locality);
        if (replicaTypeMap.get(zone) == null) {
            log.error("locality={} does not contains zone={}", locality, zone);
            throw new IllegalArgumentException(ErrorCodes.OB_TENANT_ZONE_NOT_VALID, zone);
        }
        if (replicaTypeMap.keySet().size() <= 1) {
            log.error("the zone number of locality={} can not be reduced to 0", locality);
            throw new IllegalArgumentException(ErrorCodes.OB_TENANT_REMAIN_ZONE_COUNT_INVALID);
        }

        replicaTypeMap.remove(zone);

        return replicaTypeMap.entrySet().stream()
                .map(t -> t.getValue() + "@" + t.getKey())
                .collect(Collectors.joining(", "));
    }

    public static String alterReplicaType(String locality, String zone, String replicaType) {
        zone = zone.trim();
        replicaType = replicaType.trim();
        Map<String, String> replicaTypeMap = buildReplicaInfoMap(locality);
        if (replicaTypeMap.get(zone) == null) {
            log.error("locality={} does not contains zone={}", locality, zone);
            throw new IllegalArgumentException(ErrorCodes.OB_TENANT_ZONE_NOT_VALID, zone);
        }

        replicaTypeMap.put(zone, replicaType);

        return replicaTypeMap.entrySet().stream()
                .map(t -> t.getValue() + "@" + t.getKey())
                .collect(Collectors.joining(", "));
    }

    public static List<String> getZoneList(String locality) {
        Map<String, String> replicaInfoMap = buildReplicaInfoMap(locality);
        return new ArrayList<>(replicaInfoMap.keySet());
    }

    static Map<String, String> buildReplicaInfoMap(String locality) {
        String targetStr = locality;
        Map<String, String> replicaInfoMap = new LinkedHashMap<>();
        while (targetStr != null && targetStr.contains("@")) {
            int atIndex = targetStr.indexOf("@");
            String replicaStr = targetStr.substring(0, atIndex);
            String lastStr = targetStr.substring(atIndex + 1);
            String key;
            int commaIndex = lastStr.indexOf(",");
            if (commaIndex == -1) {
                key = lastStr;
                targetStr = null;
            } else {
                key = lastStr.substring(0, commaIndex);
                targetStr = lastStr.substring(commaIndex + 1).trim();
            }
            replicaInfoMap.put(key, replicaStr);
        }
        return replicaInfoMap;
    }

    public static Map<String, String> buildReplicaTypeMap(String locality) {
        Map<String, String> replicaInfoMap = buildReplicaInfoMap(locality);
        return replicaInfoMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> extractReplicaType(e.getValue())));
    }

    private static String extractReplicaType(String replicaStr) {
        String targetStr = replicaStr;
        int indexLeft = targetStr.indexOf("{");
        int indexRight = targetStr.indexOf("}");
        while (indexLeft != -1 && indexRight != -1) {
            targetStr = targetStr.substring(0, indexLeft) + targetStr.substring(indexRight + 1);
            indexLeft = targetStr.indexOf("{");
            indexRight = targetStr.indexOf("}");
        }
        return targetStr;
    }
}
