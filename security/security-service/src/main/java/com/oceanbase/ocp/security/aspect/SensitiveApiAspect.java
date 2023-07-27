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

package com.oceanbase.ocp.security.aspect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.exception.UnexpectedException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.security.annotation.SensitiveAttribute;
import com.oceanbase.ocp.security.annotation.SensitiveType;
import com.oceanbase.ocp.security.iam.LoginKeyService;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Order
@Component
@ConditionalOnProperty(name = "ocp.login.encryption.enabled", havingValue = "true")
public class SensitiveApiAspect {

    private final LoginKeyService loginKeyService;

    public SensitiveApiAspect(LoginKeyService loginKeyService) {
        this.loginKeyService = loginKeyService;
    }

    @Pointcut("@annotation(com.oceanbase.ocp.security.annotation.SensitiveApi)")
    private void pointcut() {}

    @Around("pointcut()")
    public Object aroundSensitiveApi(ProceedingJoinPoint joinPoint) {
        log.info("Begin to decrypt the sensitive info in api param");
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();

            for (int idx = 0; idx < args.length; ++idx) {
                SensitiveAttribute attribute = parameters[idx].getAnnotation(SensitiveAttribute.class);
                if (attribute != null) {
                    String decryptedText = loginKeyService.decryptByPrivate(String.valueOf(args[idx]));
                    args[idx] = decryptedText;
                    continue;
                }
                Class<?> paramType = parameters[idx].getType();
                if (paramType.getAnnotation(SensitiveType.class) == null) {
                    continue;
                }
                for (Field field : paramType.getDeclaredFields()) {
                    if (field.getAnnotation(SensitiveAttribute.class) != null) {
                        field.setAccessible(true);
                        String encryptedText = String.valueOf(field.get(args[idx]));
                        String decryptedText = loginKeyService.decryptByPrivate(encryptedText);
                        if (decryptedText == null) {
                            throw new UnexpectedException(ErrorCodes.IAM_SECURITY_FAILED_TO_DECRYPT_SENSITIVE_ATTRIBUTE,
                                    field.getName());
                        }
                        field.set(args[idx], decryptedText);
                    }
                }
            }
            log.info("Success to decrypt the sensitive info in api param");
            return joinPoint.proceed(args);
        } catch (Throwable ex) {
            log.warn("Failed to around sensitive api", ex);
            if (ex instanceof OcpException) {
                throw (OcpException) ex;
            }
            throw new UnexpectedException(ex);
        }
    }
}
