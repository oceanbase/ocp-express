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

package com.oceanbase.ocp.controller.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.util.WebRequestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        // do something like logging
        String originalUrl = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        log.warn("Requested URL not found: [url={}, client={}, traceId={}]", originalUrl,
                WebRequestUtils.getClientAddress(request), TraceUtils.getTraceId());
        return new ModelAndView("index");
    }

}
