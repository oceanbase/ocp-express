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
package com.oceanbase.ocp.common.util.encode;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class ByteArrayToHexConverter implements ByteArrayToStringConverter {

    @Override
    public byte[] convertToLeft(String right) {
        try {
            return Hex.decodeHex(right);
        } catch (DecoderException e) {
            throw new RuntimeException("decodeHex failed: ", e);
        }
    }

    @Override
    public String convertToRight(byte[] left) {
        return Hex.encodeHexString(left);
    }
}
