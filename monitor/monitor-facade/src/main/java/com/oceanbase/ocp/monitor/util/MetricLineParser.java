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
package com.oceanbase.ocp.monitor.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.monitor.model.metric.Metric;
import com.oceanbase.ocp.monitor.model.metric.MetricLabels;
import com.oceanbase.ocp.monitor.model.metric.MetricLine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricLineParser {

    private static final ConcurrentHashMap<String, Metric> METRIC_CACHE = new ConcurrentHashMap<>(10240);

    /**
     * Parse prometheus query result.
     */
    public static LinkedList<MetricLine> parseResultToLines(String metricResult, Long collectAt) {
        LinkedList<MetricLine> lines = new LinkedList<>();
        try (StringReader sr = new StringReader(metricResult);
                BufferedReader reader = new BufferedReader(sr)) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                int len = strLine.length();
                if (len == 0) {
                    continue;
                }
                int st = 0;

                // Find first visible char.
                while ((st < len) && (strLine.charAt(st) <= SPACE)) {
                    st++;
                }
                // Parse line if not comment.
                if (strLine.charAt(st) != '#') {
                    Optional.ofNullable(parse(strLine, collectAt)).ifPresent(lines::add);
                }
            }
            return lines;
        } catch (Exception e) {
            throw new RuntimeException("parse failed", e);
        }
    }

    /**
     * Parse single line in exporter query result.
     */
    public static MetricLine parse(String line, long collectAt) {
        try {
            int labelStart = firstIdxOf(line, 0, line.length(), '{');
            int labelEnd = lastIdxOfRightBrace(line);
            // Parse line without labels.
            if (labelStart < 0 && labelEnd < 0) {
                return parseWithoutLabel(line, collectAt);
            }
            if (labelStart < 0 || labelEnd < 0) {
                throw new IllegalArgumentException("Wrong label format");
            }
            Metric metric = parseMetric(line, labelStart, labelEnd);

            Pair<Double, Long> pair = parseValueAndTs(line, labelEnd, collectAt);
            return new MetricLine(metric, pair.getLeft(), pair.getRight());
        } catch (Exception e) {
            log.warn("Parse metric line failed, line={}, errMsg={}", line, e.getMessage());
            return null;
        }
    }

    private static final char SPACE = ' ';
    private static final char TAB = '\t';
    private static final char EQUAL = '=';
    private static final char QUOTE = '"';
    private static final char COMMA = ',';
    private static final char SLASH = '\\';

    private static MetricLine parseWithoutLabel(String line, long collectAt) {
        int separatorIdx = firstIdxOf(line, 0, line.length(), SPACE);
        if (separatorIdx < 0) {
            separatorIdx = firstIdxOf(line, 0, line.length(), TAB);
        }
        if (separatorIdx < 0) {
            throw new IllegalArgumentException("Invalid line format");
        }
        String name = parseName(line, separatorIdx);
        MetricLabels labels = new MetricLabels();

        Pair<Double, Long> pair = parseValueAndTs(line, separatorIdx, collectAt);
        return new MetricLine(name, labels, pair.getLeft(), pair.getRight());
    }

    private static Metric parseMetric(String line, int labelStart, int labelEnd) {
        String metricNameWithLabel = line.substring(0, labelEnd);
        return METRIC_CACHE.computeIfAbsent(metricNameWithLabel, s -> {
            String name = parseName(line, labelStart);
            MetricLabels labels = parseLabels(line, labelStart, labelEnd);
            String seriesKey = name + "|" + labels.getSeriesKeyPostfix();
            return new Metric(name, labels, seriesKey);
        });
    }

    private static String parseName(String line, int labelStart) {
        int nameStart = firstVisibleIdx(line, 0, labelStart);
        int nameEnd = lastVisibleIdx(line, 0, labelStart);
        return line.substring(nameStart, nameEnd + 1);
    }

    private static MetricLabels parseLabels(String line, int labelStart, int labelEnd) {
        int start = labelStart + 1;
        MetricLabels labels = new MetricLabels();
        // Parse one label_name, label_value pair.
        while (start < labelEnd) {
            int valueStart = firstIdxOf(line, start, labelEnd, EQUAL);
            if (valueStart == -1) {
                return labels;
            }
            int labelNameStart = firstVisibleIdx(line, start, valueStart);
            int labelNameEnd = lastVisibleIdx(line, start, valueStart);
            String labelName = line.substring(labelNameStart, labelNameEnd + 1);
            start = valueStart;

            int commaIdx = firstIdxOf(line, start, labelEnd, COMMA);
            if (commaIdx < 0) {
                commaIdx = labelEnd;
            }
            int quoteStart = firstIdxOf(line, start, commaIdx, QUOTE);
            if (quoteStart == -1) {
                if (commaIdx > 0) {
                    start = commaIdx + 1;
                }
                labels.addLabel(labelName, "");
                continue;
            }
            int idx = quoteStart + 1;
            while (idx < labelEnd) {
                idx = firstIdxOf(line, idx, labelEnd, QUOTE);
                if (!isCharacterEscaped(line, idx)) {
                    break;
                }
                idx++;
            }
            if (idx == -1) {
                throw new IllegalArgumentException("Wrong label format, no label value end quote.");
            }
            int quoteEnd = idx;
            String labelValue = line.substring(quoteStart + 1, quoteEnd);
            labels.addLabel(labelName, labelValue);
            start = quoteEnd + 1;

            commaIdx = firstIdxOf(line, start, labelEnd, COMMA);
            if (commaIdx < 0) {
                break;
            }
            start = commaIdx + 1;
        }
        return labels;
    }

    private static boolean isCharacterEscaped(String s, int charPos) {
        int slashes = 0;
        while (charPos > slashes && s.charAt(charPos - 1 - slashes) == SLASH) {
            slashes++;
        }
        return slashes > 0;
    }

    private static Pair<Double, Long> parseValueAndTs(String line, int labelEnd, long collectAt) {
        int len = line.length();
        int valueStart = firstVisibleIdx(line, labelEnd + 1, len);

        int valueEnd = valueStart;
        while ((valueEnd < len) && (line.charAt(valueEnd) > SPACE)) {
            valueEnd++;
        }
        if (valueStart == valueEnd) {
            throw new IllegalArgumentException("Metric has no value.");
        }
        double value = Double.parseDouble(line.substring(valueStart, valueEnd));

        int tsStart = firstVisibleIdx(line, valueEnd + 1, len);
        int tsEnd = lastVisibleIdx(line, valueEnd, len);
        long ts = tsEnd > tsStart ? parseLong(line.substring(tsStart, tsEnd + 1)) : collectAt;

        return Pair.of(value, ts);
    }

    private static int firstIdxOf(String str, int start, int end, char aChar) {
        int idx = start;
        while (idx < end) {
            if (str.charAt(idx) == aChar) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    private static int lastIdxOfRightBrace(String str) {
        int idx = str.length() - 1;
        while (idx >= 0) {
            if (str.charAt(idx) == '}') {
                return idx;
            }
            idx--;
        }
        return -1;
    }

    private static int firstVisibleIdx(String str, int start, int end) {
        int idx = start;
        while ((idx < end) && (str.charAt(idx) <= SPACE)) {
            idx++;
        }
        return idx;
    }

    private static int lastVisibleIdx(String str, int start, int end) {
        int idx = end - 1;
        while ((idx >= start) && (str.charAt(idx) <= SPACE)) {
            idx--;
        }
        return idx;
    }

    private static long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return 0;
        }
    }

}
