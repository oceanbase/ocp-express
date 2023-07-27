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

package com.oceanbase.ocp.common.version;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * the format of version is
 * <MajorVersion>.<MinorVersion>.<PatchVersion>-<BuildNumber> or
 * <MajorVersion>.<MinorVersion>.<PatchVersion>.<incrVersion>-<BuildNumber>
 */
@EqualsAndHashCode
public class Version implements Comparable<Version> {

    @Getter
    private int majorVersion;

    @Getter
    private int minorVersion;

    @Getter
    private int patchVersion;

    @Getter
    private int incrVersion;

    @Getter
    private long buildNumber;

    @Getter
    private String fullVersion;

    @Getter
    private String shortVersion;

    @Getter
    private int maxLen;

    private Version() {}

    public Version(String version) {
        setVersion(version);
    }

    public Version(String shortVersion, long buildNumber) {
        setVersion(shortVersion + "-" + buildNumber);
    }

    public Version(int major, int minor, int patch, int incr) {
        setVersion(major, minor, patch, incr, -1);
    }

    public Version(int major, int minor, int patch, int incr, long buildNumber) {
        setVersion(major, minor, patch, incr, buildNumber);
    }

    private void setVersion(String version) {
        Validate.notNull(version, "The input version is null.");
        String[] parts = version.split("-");
        String versionStr = parts[0];
        String[] versionGroup = versionStr.split("\\.");
        if (versionGroup.length < 3 || versionGroup.length > 4) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        this.majorVersion = Integer.parseInt(versionGroup[0]);
        this.minorVersion = Integer.parseInt(versionGroup[1]);
        this.patchVersion = Integer.parseInt(versionGroup[2]);
        this.maxLen = versionGroup.length;
        if (versionGroup.length == 4) {
            this.incrVersion = Integer.parseInt(versionGroup[3]);
        } else {
            this.incrVersion = -1;
        }
        if (parts.length > 1) {
            this.buildNumber = Long.parseLong(parts[1]);
        } else {
            this.buildNumber = -1;
        }
        buildVersion();
    }

    private void setVersion(int major, int minor, int patch, int incr, long buildNumber) {
        Validate.isTrue(major >= 0, "Input major version is invalid.");
        Validate.isTrue(minor >= 0, "Input minor version is invalid.");
        Validate.isTrue(patch >= 0, "Input patch version is invalid.");
        this.majorVersion = major;
        this.minorVersion = minor;
        this.patchVersion = patch;
        if (incr >= 0) {
            this.incrVersion = incr;
        } else {
            this.incrVersion = -1;
        }
        if (buildNumber >= 0) {
            this.buildNumber = buildNumber;
        } else {
            this.buildNumber = -1;
        }
        buildVersion();
    }

    private void buildVersion() {
        if (this.incrVersion >= 0) {
            this.shortVersion = String.format("%d.%d.%d.%d", majorVersion, minorVersion, patchVersion, incrVersion);
        } else {
            this.shortVersion = String.format("%d.%d.%d", majorVersion, minorVersion, patchVersion);
        }

        if (this.buildNumber >= 0) {
            this.fullVersion = String.format("%s-%d", shortVersion, buildNumber);
        } else {
            this.fullVersion = shortVersion;
        }
    }

    @Override
    public int compareTo(Version other) {
        return compareFullVersion(this, other);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "["
                + "fullVersion=" + fullVersion
                + ", shortVersion=" + shortVersion
                + ", majorVersion=" + majorVersion
                + ", minorVersion=" + minorVersion
                + ", patchVersion=" + patchVersion
                + ", incrVersion=" + incrVersion
                + ", buildNumber=" + buildNumber
                + "]";
    }

    private static int compareShortVersion(Version v1, Version v2) {
        int maxLen = Math.max(v1.maxLen, v2.maxLen);
        String s1 = normalize(v1.getShortVersion(), maxLen);
        String s2 = normalize(v2.getShortVersion(), maxLen);
        return s1.compareTo(s2);
    }

    private static int compareFullVersion(Version v1, Version v2) {
        int result = compareShortVersion(v1, v2);
        if (result == 0) {
            long buildNumberDiff = v1.getBuildNumber() - v2.getBuildNumber();
            if (0 == buildNumberDiff) {
                return 0;
            } else {
                return buildNumberDiff > 0 ? 1 : -1;
            }
        }
        return result;
    }

    private static String normalize(String version, int maxLen) {
        return normalize(version, ".", 4, maxLen);
    }

    private static String normalize(String version, String separator, int maxWidth, int maxLen) {
        if (Objects.isNull(version)) {
            return "";
        }
        String[] split = Pattern.compile(separator, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + "s", s));
        }
        int len = split.length;
        while (len < maxLen) {
            sb.append(String.format("%" + maxWidth + "s", 0));
            len++;
        }
        return sb.toString();
    }
}
