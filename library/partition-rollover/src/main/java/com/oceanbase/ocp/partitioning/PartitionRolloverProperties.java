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

package com.oceanbase.ocp.partitioning;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ConfigurationProperties(prefix = "ocp.partitioning")
public class PartitionRolloverProperties {

    private boolean debug = false;

    private boolean disabled = false;

    /**
     * Partition rollover delay after application bootstrap.
     */
    @Value("#{T(org.springframework.boot.convert.DurationStyle).SIMPLE.parse(\"${initial-worker-delay:15s}\")}")
    private Duration initialWorkerDelay = Duration.ofSeconds(15);

    @Autowired
    private DbProperties dbProperties;

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "spring.datasource")
    public static class DbProperties {

        private static Pattern jdbcUrlPattern = Pattern.compile("://(.*):(\\d+)/(\\w*)");

        private String url;
        private String username;
        @ToString.Exclude
        private String password;

        private String getMatcherPatternValue(int patternIndex) {
            Matcher matcher = jdbcUrlPattern.matcher(url);
            String patternValue = "";
            if (matcher.find()) {
                patternValue = matcher.group(patternIndex);
            }
            return patternValue;
        }

        public String getHost() {
            String host = "localhost";
            String hostValue = getMatcherPatternValue(1);
            if (StringUtils.isNotEmpty(hostValue)) {
                host = hostValue;
            }
            return host;
        }

        public String getPort() {
            return getMatcherPatternValue(2);
        }

        public String getDatabase() {
            return getMatcherPatternValue(3);
        }
    }
}
