package com.oceanbase.ocp.bootstrap.db.table;

import java.lang.reflect.Proxy;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBLexer;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.StmtContext;
import com.oceanbase.ocp.sqlparser.obmysql.sql.OBParserListener;

public class TableParserTest {

    private static final String sql = "CREATE TABLE IF NOT EXISTS `ob_hist_sql_audit_stat_1` (\n"
            + "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Id',\n"
            + "  `name` VARCHAR(128) NOT NULL COMMENT 'OB的集群名称',\n"
            + "  `tenant_id` BIGINT NOT NULL COMMENT 'OB的租户Id',\n"
            + "  `sql_id` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'SQL_ID',\n"
            + "  PRIMARY KEY (`tenant_id`, `id`),\n"
            + "  INDEX idx_name_sql (tenant_id, name) LOCAL,\n"
            + "  UNIQUE KEY `unique` (`name`) BLOCK_SIZE 16384\n"
            + ") COMMENT = 'OB历史SQL性能指标的第一级归集'\n"
            // + "PARTITION BY RANGE COLUMNS (`begin_interval_time`) (PARTITION DUMMY VALUES
            // LESS THAN (0))\n"
            + "AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMPRESSION = 'lz4_1.0'\n"
            + "PARTITION BY RANGE COLUMNS (`begin_interval_time`) (PARTITION DUMMY VALUES LESS THAN (0))\n";


    // @Test
    // public void parseDruid() throws SqlParseException {
    //
    // SQLStatement statements =
    // SQLUtils.parseSingleStatement(sql, JdbcConstants.ORACLE,
    // SQLParserFeature.KeepComments);
    // MySqlCreateTableStatement createTable = (MySqlCreateTableStatement)
    // statements;
    //
    // // createTable.getName();
    // // createTable.getColumnDefinitions();
    // // createTable.getTablePartitions();
    // // createTable.getPrimaryKeyNames();
    // // createTable.getMysqlIndexes();
    // }

    // @Test
    // public void parseCalcite() throws SqlParseException {
    // Config config = Config.DEFAULT
    // .withLex(Lex.ORACLE)
    // .withCaseSensitive(false)
    // .withQuoting(Quoting.BACK_TICK)
    // .withConformance(SqlConformanceEnum.ORACLE_12)
    // .withParserFactory(SqlDdlParserImpl.FACTORY)
    // .withCaseSensitive(false);
    //
    // SqlNode sqlNode = SqlParser.create(new SourceStringReader(sql),
    // config).parseStmt();
    // }

    @Test
    public void parseOBParser() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql);
        System.out.println(tableDefinition);

        OBLexer lexer = new OBLexer(CharStreams.fromString(sql));
        OBParser obParser = new OBParser(new CommonTokenStream(lexer));
        StmtContext stmt = obParser.stmt();
        ParseTreeWalker walker = new ParseTreeWalker();
        OBParserListener proxy = (OBParserListener) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[] {OBParserListener.class},
                (proxy1, method, args) -> {
                    String methodName = method.getName();
                    if (methodName.equals("enterEveryRule") ||
                            methodName.equals("enterEmpty") ||
                    // methodName.equals("enterNot") ||
                            methodName.equals("visitTerminal") ||
                            methodName.equals("exitEveryRule") ||
                    // methodName.startsWith("exit") ||
                            methodName.equals("enterStmt")) {
                        return null;
                    }
                    ParserRuleContext context = (ParserRuleContext) args[0];
                    if (context.getChildCount() == 0) {
                        return null;
                    }
                    if (context.getChildCount() > 9) {
                        return null;
                    }
                    System.out.printf("%s %d\n", methodName, context.getChildCount());
                    for (int i = 0; i < context.getChildCount(); i++) {
                        System.out.printf("    %s\n", context.getChild(i).getText());
                    }

                    return null;
                });
        walker.walk(proxy, stmt);
    }

    // @Test
    // public void testJSqlParser() throws JSQLParserException, ParseException {
    // CCJSqlParser sqlParser = new CCJSqlParser(new StringProvider(sql))
    // .withAllowComplexParsing(true);
    // // sqlParser.setErrorRecovery(true);
    //
    // Statement statement = sqlParser.Statement();
    // CreateTable createTable = (CreateTable) statement;
    // List<String> columns = createTable.getColumns();
    // Table table = createTable.getTable();
    // System.out.println(columns);
    // }
}
