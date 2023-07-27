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

package com.oceanbase.ocp.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oceanbase.ocp.core.exception.AuthenticationException;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.exception.UnexpectedException;
import com.oceanbase.ocp.core.i18n.ErrorCode;
import com.oceanbase.ocp.core.i18n.ErrorCodes;

import lombok.extern.slf4j.Slf4j;

/**
 * An OcpException utility, contain some helper methods to throw exceptions when
 * the given expression is FALSE.
 */
@Slf4j
public final class ExceptionUtils {

    public static void illegalArgs(boolean expression, Object... args) {
        if (!expression) {
            throwException(IllegalArgumentException.class, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, args);
        }
    }

    public static void illegalArgs(boolean expression, ErrorCode errorCode, Object... args) {
        if (!expression) {
            throwException(IllegalArgumentException.class, errorCode, args);
        }
    }

    public static void initIllegalArgs(boolean expression, Object... args) {
        if (!expression) {
            throwException(IllegalArgumentException.class, ErrorCodes.COMMON_INIT_ILLEGAL_ARGUMENT, args);
        }
    }

    public static void notFound(boolean expression, Object... args) {
        if (!expression) {
            throwException(NotFoundException.class, ErrorCodes.COMMON_NOT_FOUND, args);
        }
    }

    public static void notAuthenticated(boolean expression, Object... args) {
        if (!expression) {
            throwException(AuthenticationException.class, ErrorCodes.IAM_USER_NOT_AUTHENTICATED, args);
        }
    }

    public static void unExpected(boolean expression, Object... args) {
        if (!expression) {
            throwException(UnexpectedException.class, ErrorCodes.COMMON_UNEXPECTED, args);
        }
    }

    public static void unExpected(boolean expression, ErrorCode errorCode, Object... args) {
        if (!expression) {
            throwException(UnexpectedException.class, errorCode, args);
        }
    }

    /**
     * throw an OcpException (and all its subclasses) with the given arguments.
     */
    public static <T extends OcpException> void throwException(Class<T> clazz, ErrorCode errorCode, Object... args) {
        throw newException(clazz, errorCode, args);
    }

    public static <T extends OcpException> T newException(Class<T> clazz, ErrorCode errorCode, Object... args) {
        T ex;
        try {
            ex = clazz.getConstructor(ErrorCode.class, Object[].class).newInstance(errorCode, args);
        } catch (Throwable e) {
            log.error("Unexpected error: init {} instance with code {}, args {}. The cause is {} and error is {}.",
                    clazz.getCanonicalName(), errorCode, args, e.getCause(), e.getMessage());
            throw new UnexpectedException(ErrorCodes.COMMON_UNEXPECTED, e.getCause(), e.getMessage());
        }
        log.error("Checked Exception: {} occurred with code {}, and args {}", clazz.getCanonicalName(),
                errorCode.getKey(), args);
        return ex;
    }

    public static List<String> getStackTraceList(Throwable throwable) {
        Stream<String> stream = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString);
        return Stream.concat(Stream.of(throwable.toString()), stream).collect(Collectors.toList());
    }

    /**
     * Check is express true, throw specified error if not true.
     *
     * @param expression target expression
     * @param errorCode error code
     */
    public static void require(boolean expression, ErrorCodes errorCode) {
        if (!expression) {
            throw errorCode.exception();
        }
    }

    /**
     * Check is express true, throw specified error if not true.
     *
     * @param expression target expression
     * @param errorCode error code
     * @param args arguments of error code
     */
    public static void require(boolean expression, ErrorCodes errorCode, Object... args) {
        if (!expression) {
            throw errorCode.exception(args);
        }
    }

    /**
     * Throw exception with error code and args.
     */
    public static void throwException(ErrorCodes errorCode, Object... args) {
        throw newException(errorCode, args);
    }

    /**
     * Construct OCP exception with specified error code and args.
     */
    public static OcpException newException(ErrorCodes errorCode, Object... args) {
        OcpException ex = errorCode.exception(args);
        log.error("Checked Exception: {} occurred with code {}, and args {}", ex.getClass().getCanonicalName(),
                errorCode.getKey(), args);
        return ex;
    }

}
