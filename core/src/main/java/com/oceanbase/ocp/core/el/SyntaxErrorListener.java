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

package com.oceanbase.ocp.core.el;

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.Interval;

import com.oceanbase.ocp.core.el.exception.SyntaxErrorException;
import com.oceanbase.ocp.core.el.meta.ELLexer;
import com.oceanbase.ocp.core.el.meta.ELParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyntaxErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg,
            RecognitionException e) {
        if (recognizer instanceof ELLexer) {
            Lexer lexer = (Lexer) recognizer;
            String text = lexer._input.getText(Interval.of(lexer._tokenStartCharIndex, lexer._input.index()));
            String errorText = lexer.getErrorDisplay(text);
            log.warn("[Lex Error] row:{}, col:{}, lex:{}, symbol:{}, error message:{}", line, charPositionInLine,
                    errorText, offendingSymbol, msg);
        } else if (recognizer instanceof ELParser) {
            List<String> ruleStack = ((Parser) recognizer).getRuleInvocationStack();
            Collections.reverse(ruleStack);
            log.warn("[Syntax Error] row:{}, col:{},symbol:{}, error message:{}", line, charPositionInLine,
                    offendingSymbol, msg);
        }
        throw SyntaxErrorException.builder()
                .line(line)
                .charPositionInLine(charPositionInLine)
                .msg(msg)
                .offendingSymbol(offendingSymbol)
                .build();
    }


}
