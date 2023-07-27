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

import java.nio.ByteBuffer;

import org.apache.commons.lang3.Validate;

public class DoubleArrayToByteArrayConverter implements Converter<double[], byte[]> {

    @Override
    public double[] convertToLeft(byte[] bytes) {
        Validate.notNull(bytes);
        Validate.isTrue(bytes.length % 8 == 0);
        Validate.isTrue(bytes.length > 0);

        ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);
        int doubleCount = bytes.length / 8;
        double[] doubles = new double[doubleCount];
        for (int i = 0; i < doubleCount; i++) {
            doubles[i] = inputBuffer.getDouble();
        }
        return doubles;
    }

    @Override
    public byte[] convertToRight(double[] doubles) {
        Validate.notNull(doubles);
        Validate.isTrue(doubles.length > 0);

        ByteBuffer inputBuffer = ByteBuffer.allocate(doubles.length * 8);
        for (double d : doubles) {
            inputBuffer.putDouble(d);
        }
        return inputBuffer.array();

    }
}
