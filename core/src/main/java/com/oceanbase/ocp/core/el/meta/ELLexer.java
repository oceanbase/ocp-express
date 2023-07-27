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

package com.oceanbase.ocp.core.el.meta;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ELLexer extends Lexer {

    static {
        RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, NUMBER = 6, OR = 7, AND = 8, EQUALS = 9,
            NOTEQUALS = 10, LT = 11, LTEQ = 12, GT = 13, GTEQ = 14, PLUS = 15, SUB = 16, MULT = 17,
            DIV = 18, MOD = 19, POW = 20, NOT = 21, IN = 22, AS = 23, FALSE = 24, TRUE = 25, MEMBERACCESSOR = 26,
            STD = 27, AVG = 28, VAR = 29, MAX = 30, MIN = 31, SUM = 32, COUNT = 33, DISTINCT = 34,
            ISNULL = 35, NOTNULL = 36, ISEMPTY = 37, NOTEMPTY = 38, ID = 39, STRING = 40, LINE_COMMENT = 41,
            COMMENT = 42, WS = 43, InvalidIdentifier = 44, UnknownToken = 45;
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };

    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    private static String[] makeRuleNames() {
        return new String[] {
                "T__0", "T__1", "T__2", "T__3", "T__4", "NUMBER", "OR", "AND", "EQUALS",
                "NOTEQUALS", "LT", "LTEQ", "GT", "GTEQ", "PLUS", "SUB", "MULT", "DIV",
                "MOD", "POW", "NOT", "IN", "AS", "FALSE", "TRUE", "MEMBERACCESSOR", "STD",
                "AVG", "VAR", "MAX", "MIN", "SUM", "COUNT", "DISTINCT", "ISNULL", "NOTNULL",
                "ISEMPTY", "NOTEMPTY", "ID", "STRING", "LINE_COMMENT", "COMMENT", "WS",
                "InvalidIdentifier", "UnknownToken", "ESC", "DIGIT", "A", "B", "C", "D",
                "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] {
                null, "'('", "')'", "'$'", "'@'", "','", null, null, null, null, null,
                "'<'", "'<='", "'>'", "'>='", "'+'", "'-'", "'*'", "'/'", "'%'", "'^'"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] {
                null, null, null, null, null, null, "NUMBER", "OR", "AND", "EQUALS",
                "NOTEQUALS", "LT", "LTEQ", "GT", "GTEQ", "PLUS", "SUB", "MULT", "DIV",
                "MOD", "POW", "NOT", "IN", "AS", "FALSE", "TRUE", "MEMBERACCESSOR", "STD",
                "AVG", "VAR", "MAX", "MIN", "SUM", "COUNT", "DISTINCT", "ISNULL", "NOTNULL",
                "ISEMPTY", "NOTEMPTY", "ID", "STRING", "LINE_COMMENT", "COMMENT", "WS",
                "InvalidIdentifier", "UnknownToken"
        };
    }

    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }


    public ELLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, new PredictionContextCache());
    }

    @Override
    public String getGrammarFileName() {
        return "EL.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2/\u01f2\b\1\4\2\t" +
                    "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
                    "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
                    "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4" +
                    ",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t" +
                    "\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t=" +
                    "\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I" +
                    "\tI\4J\tJ\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\5\7\u00a1\n\7\3" +
                    "\7\6\7\u00a4\n\7\r\7\16\7\u00a5\3\7\3\7\6\7\u00aa\n\7\r\7\16\7\u00ab\5" +
                    "\7\u00ae\n\7\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00b6\n\b\3\t\3\t\3\t\3\t\3\t" +
                    "\3\t\3\t\3\t\5\t\u00c0\n\t\3\n\3\n\3\n\5\n\u00c5\n\n\3\13\3\13\3\13\3" +
                    "\13\5\13\u00cb\n\13\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\20" +
                    "\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\26" +
                    "\3\26\3\26\3\26\3\26\5\26\u00ea\n\26\3\27\3\27\3\27\3\27\5\27\u00f0\n" +
                    "\27\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u00f8\n\30\3\31\3\31\3\31\3\31" +
                    "\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u0109\n\31" +
                    "\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u0117" +
                    "\n\32\3\33\3\33\3\33\5\33\u011c\n\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35" +
                    "\3\35\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3!\3!\3!\3!" +
                    "\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#\3#\3$\3$\3$\5$\u0148" +
                    "\n$\3$\3$\3$\3$\3$\3%\3%\3%\3%\5%\u0153\n%\3%\3%\3%\3%\3%\3&\3&\3&\5&" +
                    "\u015d\n&\3&\3&\3&\3&\3&\3&\3\'\3\'\3\'\3\'\5\'\u0169\n\'\3\'\3\'\3\'" +
                    "\3\'\3\'\3\'\3(\3(\7(\u0173\n(\f(\16(\u0176\13(\3)\3)\7)\u017a\n)\f)\16" +
                    ")\u017d\13)\3)\3)\3)\7)\u0182\n)\f)\16)\u0185\13)\3)\5)\u0188\n)\3*\3" +
                    "*\3*\3*\7*\u018e\n*\f*\16*\u0191\13*\3*\5*\u0194\n*\3*\3*\3*\3*\3+\3+" +
                    "\3+\3+\7+\u019e\n+\f+\16+\u01a1\13+\3+\3+\3+\3+\3+\3,\6,\u01a9\n,\r,\16" +
                    ",\u01aa\3,\3,\3-\3-\6-\u01b1\n-\r-\16-\u01b2\3.\3.\3/\3/\3/\3/\5/\u01bb" +
                    "\n/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3" +
                    "\66\3\67\3\67\38\38\39\39\3:\3:\3;\3;\3<\3<\3=\3=\3>\3>\3?\3?\3@\3@\3" +
                    "A\3A\3B\3B\3C\3C\3D\3D\3E\3E\3F\3F\3G\3G\3H\3H\3I\3I\3J\3J\6\u017b\u0183" +
                    "\u018f\u019f\2K\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31" +
                    "\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65" +
                    "\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\2_\2a\2c\2e\2g\2" +
                    "i\2k\2m\2o\2q\2s\2u\2w\2y\2{\2}\2\177\2\u0081\2\u0083\2\u0085\2\u0087" +
                    "\2\u0089\2\u008b\2\u008d\2\u008f\2\u0091\2\u0093\2\3\2!\5\2C\\aac|\6\2" +
                    "\62;C\\aac|\5\2\13\f\17\17\"\"\3\2\62;\5\2\62;C\\c|\4\2CCcc\4\2DDdd\4" +
                    "\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMm" +
                    "m\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2" +
                    "VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u01f7\2\3\3\2" +
                    "\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17" +
                    "\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2" +
                    "\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3" +
                    "\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3" +
                    "\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2" +
                    "=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3" +
                    "\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2" +
                    "\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\3\u0095\3\2\2\2\5\u0097\3\2\2\2\7" +
                    "\u0099\3\2\2\2\t\u009b\3\2\2\2\13\u009d\3\2\2\2\r\u00a0\3\2\2\2\17\u00b5" +
                    "\3\2\2\2\21\u00bf\3\2\2\2\23\u00c4\3\2\2\2\25\u00ca\3\2\2\2\27\u00cc\3" +
                    "\2\2\2\31\u00ce\3\2\2\2\33\u00d1\3\2\2\2\35\u00d3\3\2\2\2\37\u00d6\3\2" +
                    "\2\2!\u00d8\3\2\2\2#\u00da\3\2\2\2%\u00dc\3\2\2\2\'\u00de\3\2\2\2)\u00e0" +
                    "\3\2\2\2+\u00e9\3\2\2\2-\u00ef\3\2\2\2/\u00f7\3\2\2\2\61\u0108\3\2\2\2" +
                    "\63\u0116\3\2\2\2\65\u011b\3\2\2\2\67\u011d\3\2\2\29\u0121\3\2\2\2;\u0125" +
                    "\3\2\2\2=\u0129\3\2\2\2?\u012d\3\2\2\2A\u0131\3\2\2\2C\u0135\3\2\2\2E" +
                    "\u013b\3\2\2\2G\u0144\3\2\2\2I\u014e\3\2\2\2K\u0159\3\2\2\2M\u0164\3\2" +
                    "\2\2O\u0170\3\2\2\2Q\u0187\3\2\2\2S\u0189\3\2\2\2U\u0199\3\2\2\2W\u01a8" +
                    "\3\2\2\2Y\u01ae\3\2\2\2[\u01b4\3\2\2\2]\u01ba\3\2\2\2_\u01bc\3\2\2\2a" +
                    "\u01be\3\2\2\2c\u01c0\3\2\2\2e\u01c2\3\2\2\2g\u01c4\3\2\2\2i\u01c6\3\2" +
                    "\2\2k\u01c8\3\2\2\2m\u01ca\3\2\2\2o\u01cc\3\2\2\2q\u01ce\3\2\2\2s\u01d0" +
                    "\3\2\2\2u\u01d2\3\2\2\2w\u01d4\3\2\2\2y\u01d6\3\2\2\2{\u01d8\3\2\2\2}" +
                    "\u01da\3\2\2\2\177\u01dc\3\2\2\2\u0081\u01de\3\2\2\2\u0083\u01e0\3\2\2" +
                    "\2\u0085\u01e2\3\2\2\2\u0087\u01e4\3\2\2\2\u0089\u01e6\3\2\2\2\u008b\u01e8" +
                    "\3\2\2\2\u008d\u01ea\3\2\2\2\u008f\u01ec\3\2\2\2\u0091\u01ee\3\2\2\2\u0093" +
                    "\u01f0\3\2\2\2\u0095\u0096\7*\2\2\u0096\4\3\2\2\2\u0097\u0098\7+\2\2\u0098" +
                    "\6\3\2\2\2\u0099\u009a\7&\2\2\u009a\b\3\2\2\2\u009b\u009c\7B\2\2\u009c" +
                    "\n\3\2\2\2\u009d\u009e\7.\2\2\u009e\f\3\2\2\2\u009f\u00a1\7/\2\2\u00a0" +
                    "\u009f\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a3\3\2\2\2\u00a2\u00a4\5_" +
                    "\60\2\u00a3\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5" +
                    "\u00a6\3\2\2\2\u00a6\u00ad\3\2\2\2\u00a7\u00a9\7\60\2\2\u00a8\u00aa\5" +
                    "_\60\2\u00a9\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab" +
                    "\u00ac\3\2\2\2\u00ac\u00ae\3\2\2\2\u00ad\u00a7\3\2\2\2\u00ad\u00ae\3\2" +
                    "\2\2\u00ae\16\3\2\2\2\u00af\u00b0\7~\2\2\u00b0\u00b6\7~\2\2\u00b1\u00b2" +
                    "\7q\2\2\u00b2\u00b6\7t\2\2\u00b3\u00b4\7Q\2\2\u00b4\u00b6\7T\2\2\u00b5" +
                    "\u00af\3\2\2\2\u00b5\u00b1\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b6\20\3\2\2" +
                    "\2\u00b7\u00b8\7(\2\2\u00b8\u00c0\7(\2\2\u00b9\u00ba\7c\2\2\u00ba\u00bb" +
                    "\7p\2\2\u00bb\u00c0\7f\2\2\u00bc\u00bd\7C\2\2\u00bd\u00be\7P\2\2\u00be" +
                    "\u00c0\7F\2\2\u00bf\u00b7\3\2\2\2\u00bf\u00b9\3\2\2\2\u00bf\u00bc\3\2" +
                    "\2\2\u00c0\22\3\2\2\2\u00c1\u00c5\7?\2\2\u00c2\u00c3\7?\2\2\u00c3\u00c5" +
                    "\7?\2\2\u00c4\u00c1\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c5\24\3\2\2\2\u00c6" +
                    "\u00c7\7#\2\2\u00c7\u00cb\7?\2\2\u00c8\u00c9\7>\2\2\u00c9\u00cb\7@\2\2" +
                    "\u00ca\u00c6\3\2\2\2\u00ca\u00c8\3\2\2\2\u00cb\26\3\2\2\2\u00cc\u00cd" +
                    "\7>\2\2\u00cd\30\3\2\2\2\u00ce\u00cf\7>\2\2\u00cf\u00d0\7?\2\2\u00d0\32" +
                    "\3\2\2\2\u00d1\u00d2\7@\2\2\u00d2\34\3\2\2\2\u00d3\u00d4\7@\2\2\u00d4" +
                    "\u00d5\7?\2\2\u00d5\36\3\2\2\2\u00d6\u00d7\7-\2\2\u00d7 \3\2\2\2\u00d8" +
                    "\u00d9\7/\2\2\u00d9\"\3\2\2\2\u00da\u00db\7,\2\2\u00db$\3\2\2\2\u00dc" +
                    "\u00dd\7\61\2\2\u00dd&\3\2\2\2\u00de\u00df\7\'\2\2\u00df(\3\2\2\2\u00e0" +
                    "\u00e1\7`\2\2\u00e1*\3\2\2\2\u00e2\u00ea\7#\2\2\u00e3\u00e4\7p\2\2\u00e4" +
                    "\u00e5\7q\2\2\u00e5\u00ea\7v\2\2\u00e6\u00e7\7P\2\2\u00e7\u00e8\7Q\2\2" +
                    "\u00e8\u00ea\7V\2\2\u00e9\u00e2\3\2\2\2\u00e9\u00e3\3\2\2\2\u00e9\u00e6" +
                    "\3\2\2\2\u00ea,\3\2\2\2\u00eb\u00ec\7k\2\2\u00ec\u00f0\7p\2\2\u00ed\u00ee" +
                    "\7K\2\2\u00ee\u00f0\7P\2\2\u00ef\u00eb\3\2\2\2\u00ef\u00ed\3\2\2\2\u00f0" +
                    ".\3\2\2\2\u00f1\u00f2\7C\2\2\u00f2\u00f8\7U\2\2\u00f3\u00f4\7C\2\2\u00f4" +
                    "\u00f8\7u\2\2\u00f5\u00f6\7c\2\2\u00f6\u00f8\7u\2\2\u00f7\u00f1\3\2\2" +
                    "\2\u00f7\u00f3\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\60\3\2\2\2\u00f9\u00fa" +
                    "\7h\2\2\u00fa\u00fb\7c\2\2\u00fb\u00fc\7n\2\2\u00fc\u00fd\7u\2\2\u00fd" +
                    "\u0109\7g\2\2\u00fe\u00ff\7H\2\2\u00ff\u0100\7c\2\2\u0100\u0101\7n\2\2" +
                    "\u0101\u0102\7u\2\2\u0102\u0109\7g\2\2\u0103\u0104\7H\2\2\u0104\u0105" +
                    "\7C\2\2\u0105\u0106\7N\2\2\u0106\u0107\7U\2\2\u0107\u0109\7G\2\2\u0108" +
                    "\u00f9\3\2\2\2\u0108\u00fe\3\2\2\2\u0108\u0103\3\2\2\2\u0109\62\3\2\2" +
                    "\2\u010a\u010b\7v\2\2\u010b\u010c\7t\2\2\u010c\u010d\7w\2\2\u010d\u0117" +
                    "\7g\2\2\u010e\u010f\7V\2\2\u010f\u0110\7t\2\2\u0110\u0111\7w\2\2\u0111" +
                    "\u0117\7g\2\2\u0112\u0113\7V\2\2\u0113\u0114\7T\2\2\u0114\u0115\7W\2\2" +
                    "\u0115\u0117\7G\2\2\u0116\u010a\3\2\2\2\u0116\u010e\3\2\2\2\u0116\u0112" +
                    "\3\2\2\2\u0117\64\3\2\2\2\u0118\u011c\7\60\2\2\u0119\u011a\7/\2\2\u011a" +
                    "\u011c\7@\2\2\u011b\u0118\3\2\2\2\u011b\u0119\3\2\2\2\u011c\66\3\2\2\2" +
                    "\u011d\u011e\5\u0085C\2\u011e\u011f\5\u0087D\2\u011f\u0120\5g\64\2\u0120" +
                    "8\3\2\2\2\u0121\u0122\5a\61\2\u0122\u0123\5\u008bF\2\u0123\u0124\5m\67" +
                    "\2\u0124:\3\2\2\2\u0125\u0126\5\u008bF\2\u0126\u0127\5a\61\2\u0127\u0128" +
                    "\5\u0083B\2\u0128<\3\2\2\2\u0129\u012a\5y=\2\u012a\u012b\5a\61\2\u012b" +
                    "\u012c\5\u008fH\2\u012c>\3\2\2\2\u012d\u012e\5y=\2\u012e\u012f\5q9\2\u012f" +
                    "\u0130\5{>\2\u0130@\3\2\2\2\u0131\u0132\5\u0085C\2\u0132\u0133\5\u0089" +
                    "E\2\u0133\u0134\5y=\2\u0134B\3\2\2\2\u0135\u0136\5e\63\2\u0136\u0137\5" +
                    "}?\2\u0137\u0138\5\u0089E\2\u0138\u0139\5{>\2\u0139\u013a\5\u0087D\2\u013a" +
                    "D\3\2\2\2\u013b\u013c\5g\64\2\u013c\u013d\5q9\2\u013d\u013e\5\u0085C\2" +
                    "\u013e\u013f\5\u0087D\2\u013f\u0140\5q9\2\u0140\u0141\5{>\2\u0141\u0142" +
                    "\5e\63\2\u0142\u0143\5\u0087D\2\u0143F\3\2\2\2\u0144\u0145\5q9\2\u0145" +
                    "\u0147\5\u0085C\2\u0146\u0148\7a\2\2\u0147\u0146\3\2\2\2\u0147\u0148\3" +
                    "\2\2\2\u0148\u0149\3\2\2\2\u0149\u014a\5{>\2\u014a\u014b\5\u0089E\2\u014b" +
                    "\u014c\5w<\2\u014c\u014d\5w<\2\u014dH\3\2\2\2\u014e\u014f\5{>\2\u014f" +
                    "\u0150\5}?\2\u0150\u0152\5\u0087D\2\u0151\u0153\7a\2\2\u0152\u0151\3\2" +
                    "\2\2\u0152\u0153\3\2\2\2\u0153\u0154\3\2\2\2\u0154\u0155\5{>\2\u0155\u0156" +
                    "\5\u0089E\2\u0156\u0157\5w<\2\u0157\u0158\5w<\2\u0158J\3\2\2\2\u0159\u015a" +
                    "\5q9\2\u015a\u015c\5\u0085C\2\u015b\u015d\7a\2\2\u015c\u015b\3\2\2\2\u015c" +
                    "\u015d\3\2\2\2\u015d\u015e\3\2\2\2\u015e\u015f\5i\65\2\u015f\u0160\5y" +
                    "=\2\u0160\u0161\5\177@\2\u0161\u0162\5\u0087D\2\u0162\u0163\5\u0091I\2" +
                    "\u0163L\3\2\2\2\u0164\u0165\5{>\2\u0165\u0166\5}?\2\u0166\u0168\5\u0087" +
                    "D\2\u0167\u0169\7a\2\2\u0168\u0167\3\2\2\2\u0168\u0169\3\2\2\2\u0169\u016a" +
                    "\3\2\2\2\u016a\u016b\5i\65\2\u016b\u016c\5y=\2\u016c\u016d\5\177@\2\u016d" +
                    "\u016e\5\u0087D\2\u016e\u016f\5\u0091I\2\u016fN\3\2\2\2\u0170\u0174\t" +
                    "\2\2\2\u0171\u0173\t\3\2\2\u0172\u0171\3\2\2\2\u0173\u0176\3\2\2\2\u0174" +
                    "\u0172\3\2\2\2\u0174\u0175\3\2\2\2\u0175P\3\2\2\2\u0176\u0174\3\2\2\2" +
                    "\u0177\u017b\7$\2\2\u0178\u017a\13\2\2\2\u0179\u0178\3\2\2\2\u017a\u017d" +
                    "\3\2\2\2\u017b\u017c\3\2\2\2\u017b\u0179\3\2\2\2\u017c\u017e\3\2\2\2\u017d" +
                    "\u017b\3\2\2\2\u017e\u0188\7$\2\2\u017f\u0183\7)\2\2\u0180\u0182\13\2" +
                    "\2\2\u0181\u0180\3\2\2\2\u0182\u0185\3\2\2\2\u0183\u0184\3\2\2\2\u0183" +
                    "\u0181\3\2\2\2\u0184\u0186\3\2\2\2\u0185\u0183\3\2\2\2\u0186\u0188\7)" +
                    "\2\2\u0187\u0177\3\2\2\2\u0187\u017f\3\2\2\2\u0188R\3\2\2\2\u0189\u018a" +
                    "\7\61\2\2\u018a\u018b\7\61\2\2\u018b\u018f\3\2\2\2\u018c\u018e\13\2\2" +
                    "\2\u018d\u018c\3\2\2\2\u018e\u0191\3\2\2\2\u018f\u0190\3\2\2\2\u018f\u018d" +
                    "\3\2\2\2\u0190\u0193\3\2\2\2\u0191\u018f\3\2\2\2\u0192\u0194\7\17\2\2" +
                    "\u0193\u0192\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0195\3\2\2\2\u0195\u0196" +
                    "\7\f\2\2\u0196\u0197\3\2\2\2\u0197\u0198\b*\2\2\u0198T\3\2\2\2\u0199\u019a" +
                    "\7\61\2\2\u019a\u019b\7,\2\2\u019b\u019f\3\2\2\2\u019c\u019e\13\2\2\2" +
                    "\u019d\u019c\3\2\2\2\u019e\u01a1\3\2\2\2\u019f\u01a0\3\2\2\2\u019f\u019d" +
                    "\3\2\2\2\u01a0\u01a2\3\2\2\2\u01a1\u019f\3\2\2\2\u01a2\u01a3\7,\2\2\u01a3" +
                    "\u01a4\7\61\2\2\u01a4\u01a5\3\2\2\2\u01a5\u01a6\b+\2\2\u01a6V\3\2\2\2" +
                    "\u01a7\u01a9\t\4\2\2\u01a8\u01a7\3\2\2\2\u01a9\u01aa\3\2\2\2\u01aa\u01a8" +
                    "\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ad\b,\2\2\u01ad" +
                    "X\3\2\2\2\u01ae\u01b0\t\5\2\2\u01af\u01b1\t\6\2\2\u01b0\u01af\3\2\2\2" +
                    "\u01b1\u01b2\3\2\2\2\u01b2\u01b0\3\2\2\2\u01b2\u01b3\3\2\2\2\u01b3Z\3" +
                    "\2\2\2\u01b4\u01b5\13\2\2\2\u01b5\\\3\2\2\2\u01b6\u01b7\7^\2\2\u01b7\u01bb" +
                    "\7$\2\2\u01b8\u01b9\7^\2\2\u01b9\u01bb\7^\2\2\u01ba\u01b6\3\2\2\2\u01ba" +
                    "\u01b8\3\2\2\2\u01bb^\3\2\2\2\u01bc\u01bd\t\5\2\2\u01bd`\3\2\2\2\u01be" +
                    "\u01bf\t\7\2\2\u01bfb\3\2\2\2\u01c0\u01c1\t\b\2\2\u01c1d\3\2\2\2\u01c2" +
                    "\u01c3\t\t\2\2\u01c3f\3\2\2\2\u01c4\u01c5\t\n\2\2\u01c5h\3\2\2\2\u01c6" +
                    "\u01c7\t\13\2\2\u01c7j\3\2\2\2\u01c8\u01c9\t\f\2\2\u01c9l\3\2\2\2\u01ca" +
                    "\u01cb\t\r\2\2\u01cbn\3\2\2\2\u01cc\u01cd\t\16\2\2\u01cdp\3\2\2\2\u01ce" +
                    "\u01cf\t\17\2\2\u01cfr\3\2\2\2\u01d0\u01d1\t\20\2\2\u01d1t\3\2\2\2\u01d2" +
                    "\u01d3\t\21\2\2\u01d3v\3\2\2\2\u01d4\u01d5\t\22\2\2\u01d5x\3\2\2\2\u01d6" +
                    "\u01d7\t\23\2\2\u01d7z\3\2\2\2\u01d8\u01d9\t\24\2\2\u01d9|\3\2\2\2\u01da" +
                    "\u01db\t\25\2\2\u01db~\3\2\2\2\u01dc\u01dd\t\26\2\2\u01dd\u0080\3\2\2" +
                    "\2\u01de\u01df\t\27\2\2\u01df\u0082\3\2\2\2\u01e0\u01e1\t\30\2\2\u01e1" +
                    "\u0084\3\2\2\2\u01e2\u01e3\t\31\2\2\u01e3\u0086\3\2\2\2\u01e4\u01e5\t" +
                    "\32\2\2\u01e5\u0088\3\2\2\2\u01e6\u01e7\t\33\2\2\u01e7\u008a\3\2\2\2\u01e8" +
                    "\u01e9\t\34\2\2\u01e9\u008c\3\2\2\2\u01ea\u01eb\t\35\2\2\u01eb\u008e\3" +
                    "\2\2\2\u01ec\u01ed\t\36\2\2\u01ed\u0090\3\2\2\2\u01ee\u01ef\t\37\2\2\u01ef" +
                    "\u0092\3\2\2\2\u01f0\u01f1\t \2\2\u01f1\u0094\3\2\2\2\37\2\u00a0\u00a5" +
                    "\u00ab\u00ad\u00b5\u00bf\u00c4\u00ca\u00e9\u00ef\u00f7\u0108\u0116\u011b" +
                    "\u0147\u0152\u015c\u0168\u0174\u017b\u0183\u0187\u018f\u0193\u019f\u01aa" +
                    "\u01b2\u01ba\3\b\2\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
