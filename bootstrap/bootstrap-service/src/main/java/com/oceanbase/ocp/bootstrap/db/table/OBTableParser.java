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

package com.oceanbase.ocp.bootstrap.db.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.bootstrap.core.def.DataType;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Field;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Index;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Partition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Primary;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBLexer;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Column_attributeContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Column_definitionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Column_nameContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Create_table_stmtContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Data_typeContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Hash_partition_optionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Index_nameContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Index_optionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Range_partition_elementContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Range_partition_listContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Range_partition_optionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Range_subpartition_elementContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Range_subpartition_listContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Relation_nameContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.StmtContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Subpartition_optionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Table_elementContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Table_optionContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParserBaseListener;

public class OBTableParser implements TableParser {

    static class CreateTableListener extends OBParserBaseListener {

        private static final int NONE = 0, TABLE = 1, COLUMN = 2, PRIMARY = 3, INDEX = 4, PARTITION = 5;
        private int state;

        private final TableDefinition tableDefinition;
        private TableDefinition.Field curField;
        private TableDefinition.Index curIndex;
        private TableDefinition.Partition curPartition;

        CreateTableListener(TableDefinition tableDefinition) {
            this.tableDefinition = tableDefinition;
        }

        @Override
        public void enterCreate_table_stmt(Create_table_stmtContext ctx) {
            // isCreateTable = true;
            state = TABLE;
        }

        @Override
        public void enterRelation_name(Relation_nameContext ctx) {
            if (state == TABLE) {
                String text = removeBackQuote(ctx.getText());
                if (text.startsWith("`")) {
                    text = text.substring(1, text.length() - 2);
                }
                tableDefinition.setName(text);
            }
        }

        @Override
        public void enterColumn_definition(Column_definitionContext ctx) {
            state = COLUMN;
            this.curField = new Field();
        }

        @Override
        public void enterColumn_name(Column_nameContext ctx) {
            String name = removeBackQuote(ctx.getText());
            if (state == COLUMN) {
                this.curField.setName(name);
            } else if (state == PRIMARY) {
                if (this.tableDefinition.getPrimaryKey() == null) {
                    this.tableDefinition.setPrimaryKey(new Primary());
                }
                this.tableDefinition.getPrimaryKey().getFields().add(name);
            } else if (state == INDEX) {
                this.curIndex.getFields().add(name);
            }
        }


        @Override
        public void enterData_type(Data_typeContext ctx) {
            if (state == COLUMN) {
                int childCount = ctx.getChildCount();
                StringBuilder sb = new StringBuilder(20);
                for (int i = 0; i < childCount; i++) {
                    ParseTree child = ctx.getChild(i);
                    if (child.getChildCount() > 0) {
                        for (int j = 0; j < child.getChildCount(); j++) {
                            String text = child.getChild(j).getText();
                            if (sb.length() > 0 && text.matches("[a-zA-Z].*")) {
                                sb.append(" ");
                            }
                            sb.append(text);
                        }
                    } else {
                        String text = child.getText();
                        if (sb.length() > 0 && text.matches("[a-zA-Z].*")) {
                            sb.append(" ");
                        }
                        sb.append(text);
                    }
                }
                this.curField.setType(DataType.fromString(sb.toString()));
            }
        }

        @Override
        public void enterColumn_attribute(Column_attributeContext ctx) {
            if (state == COLUMN) {
                if (ctx.getChildCount() == 1) {
                    String s1 = ctx.getChild(0).getText();
                    if ("AUTO_INCREMENT".equalsIgnoreCase(s1)) {
                        curField.setAutoIncrement(true);
                        return;
                    }
                }
                if (ctx.getChildCount() == 2) {
                    String s1 = ctx.getChild(0).getText();
                    String s2 = ctx.getChild(1).getText();
                    if ("NOT".equalsIgnoreCase(s1) && "NULL".equalsIgnoreCase(s2)) {
                        curField.setNullable(false);
                    } else if ("COMMENT".equalsIgnoreCase(s1)) {
                        curField.setComment(SQLUtils.stringToValue(s2).toString());
                    } else if ("DEFAULT".equalsIgnoreCase(s1)) {
                        curField.setDefaultValue(SQLUtils.stringToValue(s2));
                    }
                }

                if (ctx.ON() != null && ctx.UPDATE() != null) {
                    curField.setOnUpdate(SQLUtils.stringToValue(ctx.cur_timestamp_func().getText()));
                }
            }
        }

        @Override
        public void exitColumn_definition(Column_definitionContext ctx) {
            this.tableDefinition.getFields().add(curField);
            this.curField = null;
            state = NONE;
        }


        @Override
        public void enterTable_element(Table_elementContext ctx) {
            if (ctx.getChildCount() < 1) {
                return;
            }
            String s1 = ctx.getChild(0).getText();
            if ("PRIMARY".equalsIgnoreCase(s1)) {
                state = PRIMARY;
            } else if ("INDEX".equalsIgnoreCase(s1) || "KEY".equalsIgnoreCase(s1)) {
                state = INDEX;
                this.curIndex = new Index();
            } else if ("UNIQUE".equalsIgnoreCase(s1)) {
                state = INDEX;
                this.curIndex = new Index();
                this.curIndex.setUnique(true);
            }
        }

        @Override
        public void exitTable_element(Table_elementContext ctx) {
            if (ctx.getChildCount() < 1) {
                return;
            }
            if (state == INDEX) {
                tableDefinition.getIndexes().add(this.curIndex);
                this.curIndex = null;
                state = NONE;
                return;
            }
            if (state == PRIMARY) {
                state = NONE;
            }
        }

        @Override
        public void enterIndex_name(Index_nameContext ctx) {
            if (state == INDEX) {
                this.curIndex.setName(removeBackQuote(ctx.getChild(0).getText()));
            }
        }

        @Override
        public void enterIndex_option(Index_optionContext ctx) {
            if (state == INDEX) {
                String s = ctx.getText();
                if ("LOCAL".equalsIgnoreCase(s)) {
                    this.curIndex.setLocal(true);
                } else if ("GLOBAL".equalsIgnoreCase(s)) {
                    this.curIndex.setLocal(false);
                }
            }
        }

        @Override
        public void enterTable_option(Table_optionContext ctx) {
            if (ctx.getChildCount() < 1) {
                return;
            }
            String s1 = ctx.getChild(0).getText();
            if ("COMMENT".equalsIgnoreCase(s1)) {
                tableDefinition.setComment(SQLUtils.stringToValue(ctx.getChild(2).getText()).toString());
            } else if ("AUTO_INCREMENT".equalsIgnoreCase(s1)) {
                tableDefinition.setAutoIncrement(Long.parseLong(ctx.getChild(2).getText()));
            } else if ("DEFAULT".equalsIgnoreCase(s1) &&
                    "CHARSET".equalsIgnoreCase(ctx.getChild(1).getText())) {
                tableDefinition.setDefaultCharset(ctx.getChild(3).getText());
            }
        }

        @Override
        public void enterRange_partition_option(Range_partition_optionContext ctx) {
            TableDefinition.Partition partition;
            if (ctx.column_name_list() == null || ctx.column_name_list().isEmpty()) {
                if (ctx.getChildCount() < 5) {
                    return;
                }
                partition = new Partition();
                partition.setByExpr(ctx.getChild(4).getText());
            } else {
                List<Column_nameContext> columnNameCtxs = ctx.column_name_list().column_name();
                partition = parsePartition(columnNameCtxs);
            }
            partition.setType("RANGE");
            tableDefinition.setPartition(partition);
        }

        @Override
        public void enterHash_partition_option(Hash_partition_optionContext ctx) {
            if (ctx.getChildCount() < 9) {
                return;
            }
            Partition partition = new Partition();
            partition.setType("HASH");
            partition.setByExpr(ctx.getChild(4).getText());
            partition.setHashPartitionCount(Integer.parseInt(ctx.getChild(8).getText()));
            tableDefinition.setPartition(partition);

        }

        @Override
        public void enterRange_partition_list(Range_partition_listContext ctx) {
            if (ctx.range_partition_element() == null || ctx.range_partition_element().isEmpty()) {
                return;
            }

            List<TableDefinition.Partition.RangeElement> rangeElements = new ArrayList<>();
            List<Range_partition_elementContext> rangeElementCtxs = ctx.range_partition_element();
            for (Range_partition_elementContext rangePartitionElementCtx : rangeElementCtxs) {
                TableDefinition.Partition.RangeElement element = new TableDefinition.Partition.RangeElement();
                element.setName(rangePartitionElementCtx.relation_factor().getText());
                String valueStr = rangePartitionElementCtx.range_partition_expr().getChild(1).getText();
                element.setValue(SQLUtils.stringToValue(valueStr));
                rangeElements.add(element);
            }
            tableDefinition.getPartition().setRangeElements(rangeElements);
        }

        @Override
        public void enterSubpartition_option(Subpartition_optionContext ctx) {
            if (ctx.getChildCount() < 3) {
                return;
            }
            String type = ctx.getChild(2).getText().toUpperCase();
            TableDefinition.Partition subPartition;
            switch (type) {
                case "HASH":
                    if (ctx.getChildCount() < 8) {
                        return;
                    }
                    subPartition = new Partition();
                    subPartition.setType(type);
                    subPartition.setByExpr(ctx.getChild(4).getText());
                    subPartition.setHashPartitionCount(Integer.parseInt(ctx.getChild(7).getText()));
                    tableDefinition.getPartition().setSubPartition(subPartition);
                    return;
                case "RANGE":
                    if (ctx.column_name_list() == null || ctx.column_name_list().isEmpty()) {
                        return;
                    }
                    List<Column_nameContext> columnNameCtxs = ctx.column_name_list().column_name();
                    subPartition = parsePartition(columnNameCtxs);
                    subPartition.setType(type);
                    tableDefinition.getPartition().setSubPartition(subPartition);
                default:
            }
        }

        @Override
        public void enterRange_subpartition_list(Range_subpartition_listContext ctx) {
            if (ctx.range_subpartition_element() == null || ctx.range_subpartition_element().isEmpty()) {
                return;
            }

            List<TableDefinition.Partition.RangeElement> rangeElements = new ArrayList<>();
            List<Range_subpartition_elementContext> rangeElementCtxs = ctx.range_subpartition_element();
            for (Range_subpartition_elementContext rangePartitionElementCtx : rangeElementCtxs) {
                TableDefinition.Partition.RangeElement element = new TableDefinition.Partition.RangeElement();
                element.setName(rangePartitionElementCtx.relation_factor().getText());
                String valueStr = rangePartitionElementCtx.range_partition_expr().getChild(1).getText();
                element.setValue(SQLUtils.stringToValue(valueStr));
                rangeElements.add(element);
            }
            tableDefinition.getPartition().getSubPartition().setRangeElements(rangeElements);
        }

        static String removeBackQuote(String s) {
            if (s.startsWith("`")) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }

        static TableDefinition.Partition parsePartition(List<Column_nameContext> ColumnNameCtxs) {
            TableDefinition.Partition partition = new TableDefinition.Partition();
            List<String> fields = new ArrayList<>();

            for (Column_nameContext columnNameContext : ColumnNameCtxs) {
                fields.add(removeBackQuote(columnNameContext.getText()));
            }
            partition.setFields(fields);
            return partition;
        }
    }

    @Override
    public TableDefinition parseCreateTable(String sql) {
        TableDefinition ret = new TableDefinition();
        try {
            sql = preprocessHashPartition(sql);
            sql = preprocessHashSubPartition(sql);
            OBLexer lexer = new OBLexer(CharStreams.fromString(sql));
            OBParser obParser = new OBParser(new CommonTokenStream(lexer));
            StmtContext stmt = obParser.stmt();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new CreateTableListener(ret), stmt);
        } catch (Throwable t) {
            throw new IllegalStateException("parse ddl failed. ddl: " + sql + ' ' + t);
        }
        ret.setCreateTableSql(sql);
        return ret;
    }

    private static final Pattern HASH_PARTITION_PATTERN = Pattern.compile(
            "(\\(\\s*partition\\s+(?:\\w+|`\\w+`)(?:\\s*,\\s*partition\\s+(?:\\w+|`\\w+`)\\s*)*\\))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern HASH_SUB_PARTITION_TEMPLATE_PATTERN = Pattern.compile(
            "(subpartition\\s+template\\s*\\(\\s*subpartition\\s+(?:\\w+|`\\w+`)(?:\\s*,\\s*subpartition\\s+(?:\\w+|`\\w+`)\\s*)*\\))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern EXTRA_HASH_SUB_PARTITION = Pattern.compile(
            "\\(\\s*partition (?:\\w+|`\\w+`) values less than \\([^)]+\\) \\((\\s*subpartition (?:\\w+|`\\w+`)(?:\\s*,\\s*subpartition (?:\\w+|`\\w+`))*)\\s*\\)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    static String preprocessHashPartition(String sql) {
        Matcher matcher = HASH_PARTITION_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        int n = StringUtils.countMatches(matcher.group(1), ',') + 1;
        return sql.substring(0, matcher.start(1)) + "PARTITIONS " + n + sql.substring(matcher.end(1));
    }

    static String preprocessHashSubPartition(String sql) {
        Matcher matcher = HASH_SUB_PARTITION_TEMPLATE_PATTERN.matcher(sql);
        if (matcher.find()) {
            int n = StringUtils.countMatches(matcher.group(1), ',') + 1;
            return sql.substring(0, matcher.start(1)) + "SUBPARTITIONS " + n + sql.substring(matcher.end(1));
        }
        matcher = EXTRA_HASH_SUB_PARTITION.matcher(sql);
        if (matcher.find()) {
            int n = StringUtils.countMatches(matcher.group(1), ',') + 1;
            return sql.substring(0, matcher.start(0)) + "SUBPARTITIONS " + n + " (PARTITION DUMMY VALUES LESS THAN(0))";
        }
        return sql;
    }

}
