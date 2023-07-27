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

package com.oceanbase.ocp.core.util.jackson;

import java.io.IOException;

import org.apache.commons.lang3.Validate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.Data;

@Data
public class LocaleString {

    private final String key;

    public static LocaleString of(String key) {
        Validate.notNull(key, "Resource key require non-null");
        return new LocaleString(key);
    }

    static final class Serializer extends StdSerializer<LocaleString> {

        private final MessageSource messageSource;

        protected Serializer(MessageSource messageSource) {
            super(LocaleString.class);
            this.messageSource = messageSource;
        }

        @Override
        public void serialize(LocaleString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            String message;
            message = messageSource.getMessage(value.key, null, LocaleContextHolder.getLocale());
            gen.writeString(message);
        }
    }

}
