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

package com.oceanbase.ocp.bootstrap.config.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Version implements Comparable<Version> {

    private final String verStr;
    private final String simpleVerStr;

    private final List<Integer> parts;
    private final String note;

    public Version(String verStr) {
        String[] verNote = verStr.split("-");
        if (verNote.length > 2) {
            throw new IllegalArgumentException("not a valid version string: " + verStr);
        }
        if (verNote.length == 2) {
            this.note = verNote[1];
        } else {
            this.note = "";
        }
        String[] parts = verNote[0].split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("not a valid version string: " + verStr);
        }
        List<Integer> verParts = new ArrayList<>(parts.length);
        for (String part : parts) {
            verParts.add(Integer.parseInt(part));
        }
        this.parts = Collections.unmodifiableList(verParts);
        this.verStr = verStr;
        this.simpleVerStr = verNote[0];
    }

    @Override
    public String toString() {
        return getVersionString();
    }

    public String getVersionString() {
        return verStr;
    }

    public String getSimpleVersionString() {
        return simpleVerStr;
    }

    @Override
    public int compareTo(Version o) {
        int n = Integer.max(parts.size(), o.parts.size());
        for (int i = 0; i < n; i++) {
            int n1 = i < parts.size() ? parts.get(i) : 0;
            int n2 = i < o.parts.size() ? o.parts.get(i) : 0;
            int ret = Integer.compare(n1, n2);
            if (ret != 0) {
                return ret;
            }
        }
        return note.compareTo(o.note);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return verStr.equals(((Version) o).verStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verStr);
    }

    public int compareTo(String o) {
        return compareTo(new Version(o));
    }

    public boolean before(String o) {
        return compareTo(o) < 0;
    }

    public boolean after(String o) {
        return compareTo(o) > 0;
    }

    public boolean same(String o) {
        return compareTo(o) == 0;
    }

    public boolean before(Version o) {
        return compareTo(o) < 0;
    }

    public boolean after(Version o) {
        return compareTo(o) > 0;
    }

    public boolean same(Version o) {
        return compareTo(o) == 0;
    }

    public List<Integer> getParts() {
        return parts;
    }

    public String getNote() {
        return note;
    }
}
