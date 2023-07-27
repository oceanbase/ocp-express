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
package com.oceanbase.ocp.common.util;


public class ByteSizeUtils {

    /**
     * Convert byte to readable size.
     *
     * assume 1 K = 1,024
     *
     * @param size size in byte unit
     * @return readable byte size
     */
    public static String readableByteSize(long size) {
        long b = size == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(size);
        return b < 1024L ? size + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KB", size / 0x1p10)
                        : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MB", size / 0x1p20)
                                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GB", size / 0x1p30)
                                        : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TB", size / 0x1p40)
                                                : b <= 0xfffccccccccccccL
                                                        ? String.format("%.1f PB", (size >> 10) / 0x1p40)
                                                        : String.format("%.1f EB", (size >> 20) / 0x1p40);
    }

}
