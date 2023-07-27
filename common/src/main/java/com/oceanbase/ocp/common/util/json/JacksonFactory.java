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

package com.oceanbase.ocp.common.util.json;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

public class JacksonFactory {

    /**
     * To avoid JavaScript long type overflow, if the long exceeds this threshold,
     * it will be converted to String serialization.
     */
    public static final long MAX_SAFE_LONG = (long) Math.pow(2, 53) - 1;
    public static final long MIN_SAFE_LONG = -MAX_SAFE_LONG;

    /**
     * Double keep two digits.
     */
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * Do not enable JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, as only long/double
     * should be converted as string for javascript
     *
     * @return ObjectMapper
     */
    public static ObjectMapper unsafeJsonMapper() {
        return JsonMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_ABSENT)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .addModule(longHandlingModule())
                .build();
    }

    /**
     * Get customized ObjectMapper.
     *
     * @return ObjectMapper
     */
    public static ObjectMapper jsonMapper() {
        return unsafeJsonMapper().registerModule(sensitiveTextHandlingModule());
    }

    private static Module longHandlingModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new LongSerializer());
        module.addSerializer(long.class, new LongSerializer());
        module.addSerializer(long[].class, new LongArraySerializer());
        module.addSerializer(Double.class, new DoubleSerializer());
        module.addSerializer(double.class, new DoubleSerializer());
        module.addSerializer(Float.class, new FloatSerializer());
        module.addSerializer(float.class, new FloatSerializer());
        return module;
    }

    private static Module sensitiveTextHandlingModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new MaskFieldSerializer());
        return module;
    }

    public static class LongSerializer extends JsonSerializer<Long> {

        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            writeLong(gen, value);
        }
    }

    public static class LongArraySerializer extends JsonSerializer<long[]> {

        @Override
        public void serialize(long[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (long v : value) {
                writeLong(gen, v);
            }
            gen.writeEndArray();
        }
    }

    public static class FloatSerializer extends JsonSerializer<Float> {

        @Override
        public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                gen.writeNumber(DECIMAL_FORMAT.format(value));
            }
        }
    }

    public static class DoubleSerializer extends JsonSerializer<Double> {

        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                gen.writeNumber(DECIMAL_FORMAT.format(value));
            }
        }
    }

    /**
     * Handle sensitive data.
     */
    private static class MaskFieldSerializer extends JsonSerializer<String> implements ContextualSerializer {

        @Getter
        private final String maskValue;

        private static final String DEFAULT_MASK_VALUE = "******";

        public MaskFieldSerializer(String maskValue) {
            this.maskValue = maskValue;
        }

        public MaskFieldSerializer() {
            this(DEFAULT_MASK_VALUE);
        }

        @Override
        public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeString(maskValue);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            if (Objects.isNull(beanProperty)) {
                return new StringSerializer();
            }
            MaskField maskField = beanProperty.getAnnotation(MaskField.class);
            if (Objects.isNull(maskField)) {
                maskField = beanProperty.getContextAnnotation(MaskField.class);
            }
            return Objects.isNull(maskField) ? new StringSerializer() : new MaskFieldSerializer(maskField.value());
        }
    }

    private static void writeLong(JsonGenerator gen, long value) throws IOException {
        if (value > MAX_SAFE_LONG || value < MIN_SAFE_LONG) {
            gen.writeString(String.valueOf(value));
        } else {
            gen.writeNumber(value);
        }
    }

}
