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
package com.oceanbase.ocp.core.i18n;

import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class I18nService {

    @Autowired
    private MessageSource messageSource;

    /**
     * Return a localized error message based on the message key, args and locale.
     */
    public String getLocalizedMessage(ErrorCode errorCode, Object[] args, Locale locale) {
        return getMessage(errorCode.getKey(), args, errorCode.toString(), locale);
    }

    public String getMessage(String key, Object[] args, String defaultValue, Locale locale) {
        Object[] stringArgs = args;
        if (null != args) {
            stringArgs = Arrays.stream(args).map(String::valueOf).toArray();
        }
        return messageSource.getMessage(key, stringArgs, defaultValue, locale);
    }

}
