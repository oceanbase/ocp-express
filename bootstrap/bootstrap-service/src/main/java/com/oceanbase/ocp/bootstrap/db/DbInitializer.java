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

package com.oceanbase.ocp.bootstrap.db;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.oceanbase.ocp.bootstrap.config.DataConfig;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.ProgressHandler;
import com.oceanbase.ocp.bootstrap.core.Stage;
import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.db.data.DataSqlGenerator;
import com.oceanbase.ocp.bootstrap.db.table.AlterTableGenerator;
import com.oceanbase.ocp.bootstrap.db.table.AlterTables;
import com.oceanbase.ocp.bootstrap.db.table.CreateTableGenerator;
import com.oceanbase.ocp.bootstrap.db.table.TableComparator;
import com.oceanbase.ocp.bootstrap.db.table.TableDiff;
import com.oceanbase.ocp.bootstrap.db.table.TableLoader;
import com.oceanbase.ocp.bootstrap.spi.AfterAllTableInitializationHook;
import com.oceanbase.ocp.bootstrap.spi.AfterDataInitializationHook;
import com.oceanbase.ocp.bootstrap.spi.AfterNecessaryTableInitializationHook;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;
import com.oceanbase.ocp.bootstrap.util.ServiceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbInitializer {

    private final String dataSourceName;
    private final ProgressHandler progressHandler;
    private final TableLoader tableLoader;
    private final SqlExecutor executor;
    private final SqlQuerier querier;

    public static class SqlTask {

        private final String name;
        private final List<String> sqls;

        public SqlTask(String name, List<String> sqls) {
            this.name = name;
            this.sqls = sqls;
        }

        public SqlTask(String name, String sql) {
            this(name, Collections.singletonList(sql));
        }

        public String getName() {
            return name;
        }

        public List<String> getSqls() {
            return sqls;
        }

        public boolean isEmpty() {
            return sqls.isEmpty();
        }
    }

    public DbInitializer(String dataSourceName, SqlExecutor executor, SqlQuerier querier,
            ProgressHandler progressHandler) {
        this.dataSourceName = dataSourceName;
        this.executor = executor;
        this.querier = querier;
        this.tableLoader = new TableLoader(querier);
        this.progressHandler = progressHandler;
    }

    public DbInitializer(String dataSourceName, DataSource dataSource, ProgressHandler progressHandler) {
        this(dataSourceName,
                sqls -> SQLUtils.executeBatch(dataSource, sqls),
                sql -> SQLUtils.queryRows(dataSource, sql),
                progressHandler);
    }

    public void initialize(Action action, DataConfig dataConfig) {
        switch (action) {
            case INSTALL:
                install(dataConfig);
                break;
            case UPGRADE:
                upgrade(dataConfig);
                break;
            default:
        }
    }

    public void install(DataConfig dataConfig) {
        progressHandler.beginAction(dataSourceName, Action.INSTALL);
        try {
            List<SqlTask> sqlTasks = generateCreateTableTasks(dataConfig.tableDefinitions());
            executeSqlTasks(Stage.CREATE_TABLES, sqlTasks);

            sleepBetweenDdlAndDml();
            log.info("run AfterNecessaryTableInitializationHook");
            ServiceUtils.loadServices(AfterNecessaryTableInitializationHook.class)
                    .forEach(hook -> hook.initialized(Action.INSTALL, dataSourceName));

            writeDefaultData(dataConfig.dataDefinitions());

            log.info("run AfterDataInitializationHook");
            ServiceUtils.loadServices(AfterDataInitializationHook.class)
                    .forEach(hook -> hook.initialized(Action.INSTALL, dataSourceName));

            log.info("run AfterAllTableInitializationHook");
            ServiceUtils.loadServices(AfterAllTableInitializationHook.class)
                    .forEach(hook -> hook.initialized(Action.INSTALL, dataSourceName));
            progressHandler.endAction(dataSourceName, Action.INSTALL, null);
        } catch (Exception e) {
            progressHandler.endAction(dataSourceName, Action.INSTALL, e);
            throw e;
        }
    }

    public void upgrade(DataConfig dataConfig) {
        progressHandler.beginAction(dataSourceName, Action.UPGRADE);
        TableTasks tableTasks;
        try {
            tableTasks = buildUpgradeTableTasks(dataConfig);
            executeSqlTasks(Stage.RENAME_TABLES, tableTasks.renameTableTasks);
            executeSqlTasks(Stage.CREATE_TABLES, tableTasks.createTableTasks);
            executeSqlTasks(Stage.ALTER_TABLES, tableTasks.necessaryAlterTableTasks());

            sleepBetweenDdlAndDml();
            log.info("run AfterNecessaryTableInitializationHook");
            ServiceUtils.loadServices(AfterNecessaryTableInitializationHook.class)
                    .forEach(hook -> hook.initialized(Action.UPGRADE, dataSourceName));

            writeDefaultData(dataConfig.dataDefinitions());
            doMigrations(dataConfig.migrations());

            log.info("run AfterDataInitializationHook");
            ServiceUtils.loadServices(AfterDataInitializationHook.class)
                    .forEach(hook -> hook.initialized(Action.UPGRADE, dataSourceName));
        } catch (Exception e) {
            progressHandler.endAction(dataSourceName, Action.UPGRADE, e);
            throw e;
        }
        new Thread(() -> {
            log.info("starting delayed DDL and DML");
            try {
                executeSqlTasks(Stage.ALTER_TABLES_DELAYED, tableTasks.changeIndexDelayedTasks);
                executeSqlTasks(Stage.DROP_TABLES, tableTasks.dropIndexTasks);

                sleepBetweenDdlAndDml();

                doMigrationsDelayed(dataConfig.migrations());
                ServiceUtils.loadServices(AfterAllTableInitializationHook.class)
                        .forEach(hook -> hook.initialized(Action.UPGRADE, dataSourceName));
                progressHandler.endAction(dataSourceName, Action.UPGRADE, null);
            } catch (Exception e) {
                progressHandler.endAction(dataSourceName, Action.UPGRADE, e);
            }
        }, "OCP-Bootstrap-Async-Upgrade-Task").start();
    }

    void sleepBetweenDdlAndDml() {
        try {
            long sleepMsBetweenDdlAndDml = 100L;
            Version obVersion = getObVersion();
            if (obVersion != null && obVersion.before("2.0.0")) {
                sleepMsBetweenDdlAndDml = TimeUnit.SECONDS.toMillis(5);
            }
            log.info("sleep {}ms between DDL and DML", sleepMsBetweenDdlAndDml);
            Thread.sleep(sleepMsBetweenDdlAndDml);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void executeSqlTasks(Stage stage, Collection<SqlTask> sqlTasks) {
        if (sqlTasks.isEmpty()) {
            return;
        }
        progressHandler.beginStage(dataSourceName, stage, sqlTasks.size());
        for (SqlTask sqlTask : sqlTasks) {
            executeSqlTask(stage, sqlTask);
        }
        progressHandler.endStage(dataSourceName, stage);
    }

    void executeSqlTask(Stage stage, SqlTask sqlTask) {
        try {
            progressHandler.beginTask(dataSourceName, stage, "sqls", sqlTask.getName(),
                    Integer.toString(sqlTask.getSqls().size()));
            executor.execute(sqlTask.sqls);
            progressHandler.endTask(dataSourceName, stage, "sqls", sqlTask.getName(), null);
        } catch (BatchUpdateException e) {
            int[] updateCounts = e.getUpdateCounts();
            String failedSql = "";
            for (int i = 0; i < updateCounts.length; i++) {
                int n = updateCounts[i];
                if (n == Statement.EXECUTE_FAILED) {
                    failedSql = sqlTask.getSqls().get(i);
                    break;
                }
            }
            Exception wrapped = new SQLException(
                    "execute sql task failed. task: " + sqlTask.getName() + ", sql:" + failedSql, e.getCause());
            progressHandler.endTask(dataSourceName, stage, "sqls", sqlTask.getName(), wrapped);
            throw new IllegalStateException(wrapped);
        } catch (Exception e) {
            progressHandler.endTask(dataSourceName, stage, "sqls", sqlTask.getName(), e);
            throw new IllegalStateException(e);
        }
    }

    List<SqlTask> generateCreateTableTasks(Collection<TableDefinition> tableDefinitions) {
        CreateTableGenerator createTableGenerator = new CreateTableGenerator();
        List<SqlTask> ret = new ArrayList<>(tableDefinitions.size());
        for (TableDefinition tableDefinition : tableDefinitions) {
            if (tableDefinition.isDrop()) {
                continue;
            }
            String createTable = createTableGenerator.generate(tableDefinition);
            SqlTask sqlTask = new SqlTask(tableDefinition.getName(), createTable);
            ret.add(sqlTask);
        }
        return ret;
    }

    public void writeDefaultData(Collection<DataDefinition> dataDefinitions) {
        List<SqlTask> sqlTasks = new ArrayList<>();
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        for (DataDefinition dataDefinition : dataDefinitions) {
            List<String> insertSqlList = dataSqlGenerator.generateInsert(dataDefinition);
            if (!insertSqlList.isEmpty()) {
                sqlTasks.add(new SqlTask(dataDefinition.getName(), insertSqlList));
            }
            List<String> delSqlList = dataSqlGenerator.generateDelete(dataDefinition);
            if (!delSqlList.isEmpty()) {
                sqlTasks.add(new SqlTask(dataDefinition.getName(), delSqlList));
            }
        }
        progressHandler.beginStage(dataSourceName, Stage.DEFAULT_DATA, sqlTasks.size());
        for (SqlTask sqlTask : sqlTasks) {
            executeSqlTask(Stage.DEFAULT_DATA, sqlTask);
        }
        progressHandler.endStage(dataSourceName, Stage.DEFAULT_DATA);
    }

    public void doMigrations(Collection<Migration> migrations) {
        doMigrations(migrations, migration -> !migration.isDelayed(), Stage.MIGRATIONS);
    }

    public void doMigrationsDelayed(Collection<Migration> migrations) {
        doMigrations(migrations, migration -> migration.isDelayed(), Stage.MIGRATIONS_DELAYED);
    }

    private void doMigrations(Collection<Migration> migrations, Predicate<Migration> filter, Stage stage) {
        List<Migration> todo = migrations.stream()
                .filter(filter)
                .collect(Collectors.toList());
        progressHandler.beginStage(dataSourceName, stage, todo.size());
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        for (Migration migration : todo) {
            List<String> sqlList = dataSqlGenerator.generateForMigration(migration, querier);
            executeSqlTask(stage, new SqlTask(migration.getName(), sqlList));
        }
        progressHandler.endStage(dataSourceName, stage);
    }

    public static class TableTasks {

        /**
         * Task orders: <br>
         * 1. rename table <br>
         * 2. create table <br>
         * 3. delete indexes <br>
         * 4. alter or delete column, modify table properties <br>
         * 5. add or alter indexes <br>
         * 6. delete tables
         */
        List<SqlTask> renameTableTasks = new ArrayList<>();
        List<SqlTask> createTableTasks = new ArrayList<>();
        List<SqlTask> dropIndexTasks = new ArrayList<>();
        List<SqlTask> alterTableTasks = new ArrayList<>();
        List<SqlTask> changeIndexTasks = new ArrayList<>();
        List<SqlTask> changeIndexDelayedTasks = new ArrayList<>();
        List<SqlTask> dropTableTasks = new ArrayList<>();


        void merge(TableTasks another) {
            renameTableTasks.addAll(another.renameTableTasks);
            createTableTasks.addAll(another.createTableTasks);
            dropIndexTasks.addAll(another.dropIndexTasks);
            alterTableTasks.addAll(another.alterTableTasks);
            changeIndexTasks.addAll(another.changeIndexTasks);
            changeIndexDelayedTasks.addAll(another.changeIndexDelayedTasks);
        }

        public List<SqlTask> necessaryAlterTableTasks() {
            return Lists.newArrayList(Iterables.concat(dropIndexTasks, alterTableTasks, changeIndexTasks));
        }

        public List<SqlTask> getByStage(Stage stage) {
            switch (stage) {
                case RENAME_TABLES:
                    return renameTableTasks;
                case CREATE_TABLES:
                    return createTableTasks;
                case ALTER_TABLES:
                    return necessaryAlterTableTasks();
                case ALTER_TABLES_DELAYED:
                    return changeIndexDelayedTasks;
                case DROP_TABLES:
                    return dropTableTasks;
                default:
                    return Collections.emptyList();
            }
        }
    }

    static void generateAlterTableTasks(List<TableDiff> tableDiff, TableTasks ret) {
        for (TableDiff diff : tableDiff) {
            ret.merge(generateAlterTableTasks(diff));
        }
    }

    static TableTasks generateAlterTableTasks(TableDiff tableDiff) {
        TableTasks ret = new TableTasks();

        String name = tableDiff.getNewTable().getName();
        AlterTableGenerator alterTableGenerator = new AlterTableGenerator();
        AlterTables alterTables = alterTableGenerator.generate(tableDiff);
        if (!alterTables.getDeletedIndices().isEmpty()) {
            ret.dropIndexTasks.add(new SqlTask(name, alterTables.getDeletedIndices()));
        }
        List<String> alterTableSqls = new ArrayList<>();
        alterTableSqls.addAll(alterTables.getChangedTableDefs());
        alterTableSqls.addAll(alterTables.getAddedColumns());
        alterTableSqls.addAll(alterTables.getChangedColumns());
        alterTableSqls.addAll(alterTables.getDeletedColumns());
        if (!alterTableSqls.isEmpty()) {
            ret.alterTableTasks.add(new SqlTask(name, alterTableSqls));
        }
        List<String> changeIndexSqls = new ArrayList<>();
        changeIndexSqls.addAll(alterTables.getAddedIndices());
        changeIndexSqls.addAll(alterTables.getChangedIndices());
        if (!changeIndexSqls.isEmpty()) {
            ret.changeIndexTasks.add(new SqlTask(name, changeIndexSqls));
        }
        List<String> changeIndexDelayedSqls = new ArrayList<>();
        changeIndexDelayedSqls.addAll(alterTables.getAddedIndicesDelayed());
        changeIndexDelayedSqls.addAll(alterTables.getChangedIndicesDelayed());
        changeIndexDelayedSqls.addAll(alterTables.getDeletedIndicesDelayed());
        if (!changeIndexDelayedSqls.isEmpty()) {
            ret.changeIndexDelayedTasks.add(new SqlTask(name, changeIndexDelayedSqls));
        }
        return ret;
    }

    static List<SqlTask> generateDropTableTasks(List<String> dropTableNames) {
        AlterTableGenerator alterTableGenerator = new AlterTableGenerator();
        List<SqlTask> ret = new ArrayList<>(dropTableNames.size());
        for (String tableName : dropTableNames) {
            String sql = alterTableGenerator.generateDropTable(tableName);
            ret.add(new SqlTask(tableName, sql));
        }
        return ret;
    }

    TableTasks buildUpgradeTableTasks(Map<String, TableDefinition> existTableDefinitions, DataConfig dataConfig) {
        log.info("building upgrade table sqls");
        TableTasks tableTasks = new TableTasks();
        tableTasks.renameTableTasks = handleRenameTables(existTableDefinitions, dataConfig);
        CompareResult compareResult = compareTables(existTableDefinitions, dataConfig);
        tableTasks.createTableTasks = generateCreateTableTasks(compareResult.newTables);
        generateAlterTableTasks(compareResult.diffs, tableTasks);
        tableTasks.dropTableTasks = generateDropTableTasks(compareResult.dropTables);
        return tableTasks;
    }

    public TableTasks buildSingleTableTasks(TableDefinition tableDefinition) {
        TableDefinition existed;
        try {
            existed = tableLoader.load(tableDefinition.getName());
        } catch (SQLException e) {
            existed = null;
        }
        DataConfig mock = new DataConfig(dataSourceName,
                Collections.singletonMap(tableDefinition.getName(), tableDefinition),
                Collections.emptyMap(), Collections.emptyMap());
        return buildUpgradeTableTasks(Collections.singletonMap(existed.getName(), existed), mock);
    }

    public TableTasks buildUpgradeTableTasks(DataConfig dataConfig) {
        Map<String, TableDefinition> existTableDefinitions = loadExistTables();
        return buildUpgradeTableTasks(existTableDefinitions, dataConfig);
    }

    private Map<String, TableDefinition> loadExistTables() {
        try {
            return tableLoader.loadAllTables();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static class CompareResult {

        List<TableDiff> diffs;
        List<TableDefinition> newTables;
        List<String> dropTables;
    }

    static CompareResult compareTables(Map<String, TableDefinition> existTableDefinitions, DataConfig dataConfig) {
        List<TableDiff> diffs = new ArrayList<>();
        List<TableDefinition> newTables = new ArrayList<>();
        List<String> dropTables = new ArrayList<>();
        TableComparator tableComparator = new TableComparator();
        for (TableDefinition tableDefinition : dataConfig.tableDefinitions()) {
            TableDefinition old = existTableDefinitions.get(tableDefinition.getName());
            if (tableDefinition.isDrop()) {
                if (old != null) {
                    dropTables.add(tableDefinition.getName());
                }
            } else if (old == null) {
                newTables.add(tableDefinition);
            } else {
                TableDiff tableDiff = tableComparator.compare(old, tableDefinition);
                if (!tableDiff.isEmpty()) {
                    diffs.add(tableDiff);
                }
            }
        }
        CompareResult ret = new CompareResult();
        ret.diffs = diffs;
        ret.newTables = newTables;
        ret.dropTables = dropTables;
        return ret;
    }

    List<SqlTask> handleRenameTables(Map<String, TableDefinition> existTableDefinitions, DataConfig dataConfig) {
        HashMap<String, String> renamePairs = new HashMap<>();
        for (TableDefinition tableDefinition : dataConfig.tableDefinitions()) {
            if (tableDefinition.getRenamedFrom() != null) {
                renamePairs.put(tableDefinition.getRenamedFrom(), tableDefinition.getName());
            }
        }
        if (renamePairs.size() == 0) {
            return Collections.emptyList();
        }
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        renamePairs
                .forEach((from, to) -> handleRenameTablePair(from, to, renamePairs, existTableDefinitions, resultMap));

        AlterTableGenerator alterTableGenerator = new AlterTableGenerator();
        List<SqlTask> ret = new ArrayList<>(resultMap.size());
        resultMap.forEach((from, to) -> {
            String sql = alterTableGenerator.generateRenameTable(from, to);
            String name = dataConfig.getTableDefinition(to).getName();
            ret.add(new SqlTask(name, sql));
            TableDefinition renamed = existTableDefinitions.remove(from);
            renamed.setName(to);
            existTableDefinitions.put(to, renamed);
        });
        return ret;
    }

    Version getObVersion() {
        String sql = "select @@version_comment";
        List<Row> rows;
        try {
            rows = querier.query(sql);
        } catch (SQLException e) {
            return null;
        }
        if (rows.isEmpty()) {
            return null;
        }
        String verString = (String) rows.get(0).get("@@version_comment");
        if (verString == null) {
            return null;
        }
        String[] parts = verString.split(" ", 3);
        if (parts.length < 2) {
            return null;
        }
        return new Version(parts[1]);
    }

    private static boolean handleRenameTablePair(String from, String to, HashMap<String, String> renamePairs,
            Map<String, TableDefinition> existTableDefinitions,
            LinkedHashMap<String, String> resultMap) {
        if (!existTableDefinitions.containsKey(from)) {
            return false;
        }
        if (!existTableDefinitions.containsKey(to)) {
            resultMap.put(from, to);
            return true;
        }
        if (resultMap.containsKey(to)) {
            resultMap.put(from, to);
            return true;
        }
        if (!renamePairs.containsKey(to)) {
            return false;
        }
        String prevTo = renamePairs.get(to);
        boolean prevHandled = handleRenameTablePair(to, prevTo, renamePairs, existTableDefinitions, resultMap);
        if (!prevHandled) {
            return false;
        }
        resultMap.put(from, to);
        return true;
    }
}
