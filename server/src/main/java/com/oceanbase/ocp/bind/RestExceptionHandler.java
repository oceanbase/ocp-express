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

package com.oceanbase.ocp.bind;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.exception.WithTarget;
import com.oceanbase.ocp.core.i18n.ErrorCode;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.i18n.I18nService;
import com.oceanbase.ocp.core.response.ErrorResponse;
import com.oceanbase.ocp.core.response.error.ApiError;
import com.oceanbase.ocp.core.response.error.ApiErrorBuilder;
import com.oceanbase.ocp.core.response.error.ApiErrorTarget;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.exception.ConnectorInitFailedException;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;

import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = {"com.oceanbase.ocp"})
@Slf4j
public class RestExceptionHandler {

    private static final String SERVER = HostUtils.getLocalIp();

    @Autowired
    private I18nService i18nService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Return a localized error message based on the message key, args and locale.
     */
    private String getLocalizedMessage(ErrorCode errorCode, Object[] args, Locale locale) {
        return i18nService.getLocalizedMessage(errorCode, args, locale);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, ApiError apiError) {
        String traceId = TraceUtils.getTraceId();
        long duration = TraceUtils.getDuration();
        ErrorResponse response = ErrorResponse.error(status, apiError, traceId, duration);
        response.setServer(SERVER);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Handles OcpException.
     *
     * @param ex the OcpException
     * @return the ApiError object
     */
    @ExceptionHandler(OcpException.class)
    protected ResponseEntity<Object> handleOcpException(OcpException ex, Locale locale) {
        ApiError apiError = new ApiError();
        apiError.setCode(ex.getErrorCode().getCode());
        apiError.setMessage(getLocalizedMessage(ex.getErrorCode(), ex.getArgs(), locale));
        if (ex.getCause() != null) {
            apiError.setDebugMessage(ex.getCause().getLocalizedMessage());
        }
        if (ex instanceof WithTarget) {
            ApiErrorTarget target = ((WithTarget) ex).buildTarget();
            apiError.setTarget(target);
        }
        String returnStackTrace = request.getHeader("X-ReturnStackTrace");
        if (Boolean.parseBoolean(returnStackTrace)) {
            apiError.setStackTrace(ExceptionUtils.getStackTraceList(ex));
        }
        return buildResponseEntity(ex.getStatus(), apiError);
    }

    /**
     * Handles org.springframework.security.access.AccessDeniedException.
     *
     * @param ex the AccessDeniedException
     * @return the ApiError object
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, Locale locale) {
        ApiError apiError = new ApiError();
        apiError.setCode(ErrorCodes.IAM_OPERATION_NOT_PERMITTED.getCode());
        apiError.setMessage(getLocalizedMessage(ErrorCodes.IAM_OPERATION_NOT_PERMITTED, null, locale));
        if (ex.getCause() != null) {
            apiError.setDebugMessage(ex.getCause().getLocalizedMessage());
        }
        return buildResponseEntity(HttpStatus.FORBIDDEN, apiError);
    }

    /**
     * Handles com.alipay.ocp.core.obsdk.exception.OceanBaseException
     *
     * @param ex the OceanBaseException
     * @return the ApiError object
     */
    @ExceptionHandler(OceanBaseException.class)
    protected ResponseEntity<Object> handleOceanBaseException(OceanBaseException ex, Locale locale) {
        ApiError apiError = new ApiError();
        apiError.setCode(ErrorCodes.COMMON_OB_OPERATION_FAILED.getCode());
        apiError.setMessage(
                getLocalizedMessage(ErrorCodes.COMMON_OB_OPERATION_FAILED, new String[] {ex.getMessage()}, locale));
        apiError.setDebugMessage(ex.getDebugMessage());
        apiError.setSubErrors(ApiErrorBuilder.buildOceanBaseSubErrors(ex));
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, apiError);
    }

    /**
     * Handles javax.validation.ConstraintViolationException. Thrown when @Validated
     * fails.
     *
     * @param ex the ConstraintViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        ApiError apiError = new ApiError();
        Set<ConstraintViolation<?>> constraintViolationSet = ex.getConstraintViolations();
        if (!CollectionUtils.isEmpty(constraintViolationSet)) {
            apiError.setMessage(constraintViolationSet.iterator().next().getMessage());
        } else {
            apiError.setMessage("Validation exception");
        }
        apiError.addValidationErrors(constraintViolationSet);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    /**
     * Handle javax.persistence.NotFoundException
     */
    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(javax.persistence.EntityNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, new ApiError(ex));
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB
     * causes.
     *
     * @param ex the DataIntegrityViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof ConstraintViolationException) {
            return buildResponseEntity(HttpStatus.CONFLICT, new ApiError("Database exception", ex.getCause()));
        }
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(ex));
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError apiError = new ApiError();
        apiError.setMessage(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required'
     * request parameter is missing.
     *
     * @param ex MissingServletRequestParameterException
     * @return the ApiError object
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String error = ex.getParameterName() + " parameter is missing";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, new ApiError(error, ex));
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is
     * invalid as well.
     *
     * @param ex HttpMediaTypeNotSupportedException
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                new ApiError(builder.substring(0, builder.length() - 2), ex));
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid
     * validation.
     *
     * @param ex the MethodArgumentNotValidException that is thrown when @Valid
     *        validation fails
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ApiError apiError = new ApiError();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        if (!CollectionUtils.isEmpty(fieldErrors)) {
            apiError.setMessage(fieldErrors.get(0).getDefaultMessage());
        } else if (!CollectionUtils.isEmpty(globalErrors)) {
            apiError.setMessage(globalErrors.get(0).getDefaultMessage());
        } else {
            apiError.setMessage("Validation exception");
        }
        apiError.addValidationErrors(fieldErrors);
        apiError.addValidationError(globalErrors);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is
     * malformed.
     *
     * @param ex HttpMessageNotReadableException
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String error = "Malformed JSON request";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, new ApiError(error, ex));
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex HttpMessageNotWritableException
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex) {
        String error = "Error writing JSON output";
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(error, ex));
    }

    /**
     * Handle NoHandlerFoundException.
     *
     * @param ex NoHandlerFoundException
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setMessage(
                String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    @ExceptionHandler(PropertyAccessException.class)
    public ResponseEntity<Object> handlePropertyAccessException(PropertyAccessException ex, Locale locale) {
        ApiError apiError = new ApiError();
        apiError.setCode(ErrorCodes.COMMON_ILLEGAL_ARGUMENT.getCode());
        apiError.setMessage(getLocalizedMessage(ErrorCodes.COMMON_ILLEGAL_ARGUMENT, null, locale));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        ApiError apiError = new ApiError();
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, apiError);
    }

    @ExceptionHandler(ConnectorInitFailedException.class)
    protected ResponseEntity<Object> handleConnectorInitFailedException(ConnectorInitFailedException ex,
            Locale locale) {
        ApiError apiError = new ApiError();
        apiError.setCode(ErrorCodes.OB_TENANT_CONNECT_FAILED.getCode());
        apiError.setMessage(getLocalizedMessage(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                new String[] {ex.getConnectProperties().getTenantName()}, locale));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, apiError);
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Object> handleClientAbortException(ClientAbortException ex) {
        String uri = Optional.ofNullable(request).map(HttpServletRequest::getRequestURI).orElse(null);
        log.warn("Client aborted, uri={}, duration={}", uri, TraceUtils.getDuration());
        ApiError apiError = new ApiError();
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, apiError);
    }

    /**
     * All unhandled exceptions fall to this method.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        String uri = Optional.ofNullable(request).map(HttpServletRequest::getRequestURI).orElse(null);
        log.error("Unhandled exception, uri={}, duration={}", uri, TraceUtils.getDuration(), ex);
        ApiError apiError = new ApiError();
        String message = ex.getMessage();
        if (StringUtils.isNotEmpty(message)) {
            apiError.setMessage(
                    String.format("Unhandled exception, type=%s, message=%s", ex.getClass().getSimpleName(), message));
        } else {
            apiError.setMessage(String.format("Unhandled exception, type=%s", ex.getClass().getSimpleName()));
        }
        apiError.setDebugMessage(message);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, apiError);
    }
}
