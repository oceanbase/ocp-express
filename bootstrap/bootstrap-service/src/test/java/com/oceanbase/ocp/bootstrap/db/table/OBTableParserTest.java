package com.oceanbase.ocp.bootstrap.db.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;

public class OBTableParserTest {

    String sql0 = "CREATE TABLE `ob_hist_sqltext` (\n"
            + "  `collect_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '数据的收集时间（截断到UTC天）',\n"
            + "  `ob_cluster_id` bigint(20) NOT NULL COMMENT 'OB的集群Id',\n"
            + "  `cluster_name` varchar(128) NOT NULL COMMENT 'OB的集群名称',\n"
            + "  `ob_tenant_id` bigint(20) NOT NULL COMMENT 'OB的租户Id',\n"
            + "  `tenant_name` varchar(64) NOT NULL COMMENT '租户的名称',\n"
            + "  `ob_server_id` bigint(20) NOT NULL COMMENT 'OB的服务Id',\n"
            + "  `ob_db_id` bigint(20) NOT NULL COMMENT 'OB的数据库Id',\n"
            + "  `db_name` varchar(128) NOT NULL COMMENT '数据库的名称',\n"
            + "  `ob_user_id` bigint(20) NOT NULL COMMENT 'OB的用户Id',\n"
            + "  `user_name` varchar(64) NOT NULL COMMENT '用户的名称',\n"
            + "  `sql_id` varchar(32) NOT NULL COMMENT 'SQL_ID',\n"
            + "  `sql_text` text NOT NULL COMMENT 'SQL的文本',\n"
            + "  `statement` text DEFAULT NULL COMMENT 'SQL的参数化文本',\n"
            + "  `table_list` varchar(4096) DEFAULT NULL COMMENT 'SQL 涉及的表',\n"
            + "  `sql_type` varchar(1024) DEFAULT NULL COMMENT 'SQL的类型',\n"
            + "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n"
            + "  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',\n"
            + "  PRIMARY KEY (`ob_cluster_id`, `cluster_name`, `ob_tenant_id`, `collect_time`, `ob_server_id`, `ob_db_id`, "
            + "`ob_user_id`, `sql_id`),\n"
            + "  KEY `idx_sqltext_cluster_tenant_time_sqlid` (`ob_cluster_id`, `cluster_name`, `ob_tenant_id`, `collect_time`, "
            + "`sql_id`) BLOCK_SIZE 16384 LOCAL\n"
            + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 1 BLOCK_SIZE = 16384 "
            + "USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0 COMMENT = 'OB历史SQL的文本信息表'\n"
            + " partition by range columns(`collect_time`)\n"
            + "(partition DUMMY values less than (0))";

    String sql1 = "CREATE TABLE IF NOT EXISTS `test1` (\n"
            + "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Id',\n"
            + "  `name` VARCHAR(128) NOT NULL COMMENT 'OB的集群名称\\\\',\n"
            + "  `tenant_id` BIGINT NOT NULL COMMENT 'OB的租户Id',\n"
            + "  `sql_id` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'SQL_ID',\n"
            + "  PRIMARY KEY (`tenant_id`, `id`),\n"
            + "  INDEX idx_name_sql (tenant_id, name) LOCAL,\n"
            + "  UNIQUE KEY `unique` (`name`) BLOCK_SIZE 16384\n"
            + ") COMMENT = 'OB历史SQL性能指标的第一级归集'\n"
            // + "PARTITION BY RANGE COLUMNS (`begin_interval_time`) (PARTITION DUMMY VALUES
            // LESS THAN (0))\n"
            + "AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMPRESSION = 'lz4_1.0'\n"
            + "PARTITION BY RANGE COLUMNS (`begin_interval_time`) SUBPARTITION BY RANGE COLUMNS (`collect_time`) SUBPARTITION TEMPLATE\n"
            + "(SUBPARTITION mp0 VALUES LESS THAN(2020),\n"
            + " SUBPARTITION mp1 VALUES LESS THAN(2021),\n"
            + " SUBPARTITION mp2 VALUES LESS THAN(2022))\n"
            + "(PARTITION DUMMY VALUES LESS THAN (0),\n"
            + "PARTITION P20220726 VALUES LESS THAN (1657830100000000),\n"

            + "PARTITION P20220802 VALUES LESS THAN (1659830400000000))\n";
    String sql2 = "CREATE TABLE `metric_data_hour` (\n"
            + "  `series_id` bigint(20) NOT NULL COMMENT '指标序列ID',\n"
            + "  `timestamp` bigint(20) NOT NULL COMMENT '时间戳，单位(秒)',\n"
            + "  `value` double NOT NULL COMMENT '值',\n"
            + "  PRIMARY KEY (`series_id`, `timestamp`)\n"
            + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 1 BLOCK_SIZE = 16384 "
            + "USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0 COMMENT = '监控小时级别指标数据'\n"
            + " partition by range columns(`timestamp`) subpartition by hash(series_id) subpartition template (\n"
            + "subpartition p0,\n"
            + "subpartition p1,\n"
            + "subpartition p2,\n"
            + "subpartition p3,\n"
            + "subpartition p4,\n"
            + "subpartition p5,\n"
            + "subpartition p6,\n"
            + "subpartition p7,\n"
            + "subpartition p8,\n"
            + "subpartition p9,\n"
            + "subpartition p10,\n"
            + "subpartition p11,\n"
            + "subpartition p12,\n"
            + "subpartition p13,\n"
            + "subpartition p14,\n"
            + "subpartition p15,\n"
            + "subpartition p16,\n"
            + "subpartition p17,\n"
            + "subpartition p18,\n"
            + "subpartition p19,\n"
            + "subpartition p20,\n"
            + "subpartition p21,\n"
            + "subpartition p22,\n"
            + "subpartition p23,\n"
            + "subpartition p24,\n"
            + "subpartition p25,\n"
            + "subpartition p26,\n"
            + "subpartition p27,\n"
            + "subpartition p28,\n"
            + "subpartition p29)\n"
            + "(partition DUMMY values less than (0),\n"
            + "partition p2019 values less than (1577836800),\n"
            + "partition p2020 values less than (1609459200),\n"
            + "partition p2021 values less than (1640995200),\n"
            + "partition p2022 values less than (1672531200),\n"
            + "partition p2023 values less than (1704067200))";

    String sql3 = "CREATE TABLE IF NOT EXISTS `test_hash` (\n"
            + "  `series_id` bigint(20) NOT NULL,\n"
            + "  `timestamp` bigint(20) NOT NULL,\n"
            + "  `data` varbinary(65535) NOT NULL,\n"
            + "  `interval` tinyint(4) DEFAULT '1' COMMENT '秒级别监控采集间隔',\n"
            + "  PRIMARY KEY (`series_id`, `timestamp`)\n"
            + ") PARTITION BY HASH(series_id) PARTITIONS 30";

    String sql4 = "CREATE TABLE `test_hash` (\n"
            + "  `series_id` bigint(20) NOT NULL,\n"
            + "  `timestamp` bigint(20) NOT NULL,\n"
            + "  `data` varbinary(65535) NOT NULL,\n"
            + "  `interval` tinyint(4) DEFAULT '1' COMMENT '秒级别监控采集间隔',\n"
            + "  PRIMARY KEY (`series_id`, `timestamp`)\n"
            + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 1 BLOCK_SIZE = 16384 "
            + "USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0\n"
            + " partition by hash(series_id)\n"
            + "(partition p0,\n"
            + "partition p1,\n"
            + "partition p2,\n"
            + "partition p3,\n"
            + "partition p4,\n"
            + "partition p5,\n"
            + "partition p6,\n"
            + "partition p7,\n"
            + "partition p8,\n"
            + "partition p9,\n"
            + "partition p10,\n"
            + "partition p11,\n"
            + "partition p12,\n"
            + "partition p13,\n"
            + "partition p14,\n"
            + "partition p15,\n"
            + "partition p16,\n"
            + "partition p17,\n"
            + "partition p18,\n"
            + "partition p19,\n"
            + "partition p20,\n"
            + "partition p21,\n"
            + "partition p22,\n"
            + "partition p23,\n"
            + "partition p24,\n"
            + "partition p25,\n"
            + "partition p26,\n"
            + "partition p27,\n"
            + "partition p28,\n"
            + "partition p29)";

    String sql5 = "CREATE TABLE `test_hash` (\n"
            + "id bigint(20) unsigned not null auto_increment,\n"
            + "f1 int unsigned not null,\n"
            + "primary key(id)\n"
            + ")";

    String sql6 = "CREATE TABLE `metric_data_second` (\n" +
            "  `series_id` bigint(20) NOT NULL,\n" +
            "  `timestamp` bigint(20) NOT NULL,\n" +
            "  `data` varbinary(65535) NOT NULL,\n" +
            "  `interval` tinyint(4) DEFAULT '1' COMMENT '秒级别监控采集间隔',\n" +
            "  PRIMARY KEY (`series_id`, `timestamp`)\n" +
            ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.3.8' REPLICA_NUM = 1 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0 \n"
            +
            " partition by range columns(`timestamp`) subpartition by hash(series_id)\n" +
            "(partition DUMMY values less than (0) (\n" +
            "subpartition DUMMYsp0,\n" +
            "subpartition DUMMYsp1,\n" +
            "subpartition DUMMYsp2,\n" +
            "subpartition DUMMYsp3,\n" +
            "subpartition DUMMYsp4,\n" +
            "subpartition DUMMYsp5,\n" +
            "subpartition DUMMYsp6,\n" +
            "subpartition DUMMYsp7,\n" +
            "subpartition DUMMYsp8,\n" +
            "subpartition DUMMYsp9,\n" +
            "subpartition DUMMYsp10,\n" +
            "subpartition DUMMYsp11,\n" +
            "subpartition DUMMYsp12,\n" +
            "subpartition DUMMYsp13,\n" +
            "subpartition DUMMYsp14,\n" +
            "subpartition DUMMYsp15,\n" +
            "subpartition DUMMYsp16,\n" +
            "subpartition DUMMYsp17,\n" +
            "subpartition DUMMYsp18,\n" +
            "subpartition DUMMYsp19,\n" +
            "subpartition DUMMYsp20,\n" +
            "subpartition DUMMYsp21,\n" +
            "subpartition DUMMYsp22,\n" +
            "subpartition DUMMYsp23,\n" +
            "subpartition DUMMYsp24,\n" +
            "subpartition DUMMYsp25,\n" +
            "subpartition DUMMYsp26,\n" +
            "subpartition DUMMYsp27,\n" +
            "subpartition DUMMYsp28,\n" +
            "subpartition DUMMYsp29),\n" +
            "partition p20230504 values less than (1683129600) (\n" +
            "subpartition p20230504sp0,\n" +
            "subpartition p20230504sp1,\n" +
            "subpartition p20230504sp2,\n" +
            "subpartition p20230504sp3,\n" +
            "subpartition p20230504sp4,\n" +
            "subpartition p20230504sp5,\n" +
            "subpartition p20230504sp6,\n" +
            "subpartition p20230504sp7,\n" +
            "subpartition p20230504sp8,\n" +
            "subpartition p20230504sp9,\n" +
            "subpartition p20230504sp10,\n" +
            "subpartition p20230504sp11,\n" +
            "subpartition p20230504sp12,\n" +
            "subpartition p20230504sp13,\n" +
            "subpartition p20230504sp14,\n" +
            "subpartition p20230504sp15,\n" +
            "subpartition p20230504sp16,\n" +
            "subpartition p20230504sp17,\n" +
            "subpartition p20230504sp18,\n" +
            "subpartition p20230504sp19,\n" +
            "subpartition p20230504sp20,\n" +
            "subpartition p20230504sp21,\n" +
            "subpartition p20230504sp22,\n" +
            "subpartition p20230504sp23,\n" +
            "subpartition p20230504sp24,\n" +
            "subpartition p20230504sp25,\n" +
            "subpartition p20230504sp26,\n" +
            "subpartition p20230504sp27,\n" +
            "subpartition p20230504sp28,\n" +
            "subpartition p20230504sp29),\n" +
            "partition p20230505 values less than (1683216000) (\n" +
            "subpartition p20230505sp0,\n" +
            "subpartition p20230505sp1,\n" +
            "subpartition p20230505sp2,\n" +
            "subpartition p20230505sp3,\n" +
            "subpartition p20230505sp4,\n" +
            "subpartition p20230505sp5,\n" +
            "subpartition p20230505sp6,\n" +
            "subpartition p20230505sp7,\n" +
            "subpartition p20230505sp8,\n" +
            "subpartition p20230505sp9,\n" +
            "subpartition p20230505sp10,\n" +
            "subpartition p20230505sp11,\n" +
            "subpartition p20230505sp12,\n" +
            "subpartition p20230505sp13,\n" +
            "subpartition p20230505sp14,\n" +
            "subpartition p20230505sp15,\n" +
            "subpartition p20230505sp16,\n" +
            "subpartition p20230505sp17,\n" +
            "subpartition p20230505sp18,\n" +
            "subpartition p20230505sp19,\n" +
            "subpartition p20230505sp20,\n" +
            "subpartition p20230505sp21,\n" +
            "subpartition p20230505sp22,\n" +
            "subpartition p20230505sp23,\n" +
            "subpartition p20230505sp24,\n" +
            "subpartition p20230505sp25,\n" +
            "subpartition p20230505sp26,\n" +
            "subpartition p20230505sp27,\n" +
            "subpartition p20230505sp28,\n" +
            "subpartition p20230505sp29),\n" +
            "partition p20230506 values less than (1683302400) (\n" +
            "subpartition p20230506sp0,\n" +
            "subpartition p20230506sp1,\n" +
            "subpartition p20230506sp2,\n" +
            "subpartition p20230506sp3,\n" +
            "subpartition p20230506sp4,\n" +
            "subpartition p20230506sp5,\n" +
            "subpartition p20230506sp6,\n" +
            "subpartition p20230506sp7,\n" +
            "subpartition p20230506sp8,\n" +
            "subpartition p20230506sp9,\n" +
            "subpartition p20230506sp10,\n" +
            "subpartition p20230506sp11,\n" +
            "subpartition p20230506sp12,\n" +
            "subpartition p20230506sp13,\n" +
            "subpartition p20230506sp14,\n" +
            "subpartition p20230506sp15,\n" +
            "subpartition p20230506sp16,\n" +
            "subpartition p20230506sp17,\n" +
            "subpartition p20230506sp18,\n" +
            "subpartition p20230506sp19,\n" +
            "subpartition p20230506sp20,\n" +
            "subpartition p20230506sp21,\n" +
            "subpartition p20230506sp22,\n" +
            "subpartition p20230506sp23,\n" +
            "subpartition p20230506sp24,\n" +
            "subpartition p20230506sp25,\n" +
            "subpartition p20230506sp26,\n" +
            "subpartition p20230506sp27,\n" +
            "subpartition p20230506sp28,\n" +
            "subpartition p20230506sp29),\n" +
            "partition p20230507 values less than (1683388800) (\n" +
            "subpartition p20230507sp0,\n" +
            "subpartition p20230507sp1,\n" +
            "subpartition p20230507sp2,\n" +
            "subpartition p20230507sp3,\n" +
            "subpartition p20230507sp4,\n" +
            "subpartition p20230507sp5,\n" +
            "subpartition p20230507sp6,\n" +
            "subpartition p20230507sp7,\n" +
            "subpartition p20230507sp8,\n" +
            "subpartition p20230507sp9,\n" +
            "subpartition p20230507sp10,\n" +
            "subpartition p20230507sp11,\n" +
            "subpartition p20230507sp12,\n" +
            "subpartition p20230507sp13,\n" +
            "subpartition p20230507sp14,\n" +
            "subpartition p20230507sp15,\n" +
            "subpartition p20230507sp16,\n" +
            "subpartition p20230507sp17,\n" +
            "subpartition p20230507sp18,\n" +
            "subpartition p20230507sp19,\n" +
            "subpartition p20230507sp20,\n" +
            "subpartition p20230507sp21,\n" +
            "subpartition p20230507sp22,\n" +
            "subpartition p20230507sp23,\n" +
            "subpartition p20230507sp24,\n" +
            "subpartition p20230507sp25,\n" +
            "subpartition p20230507sp26,\n" +
            "subpartition p20230507sp27,\n" +
            "subpartition p20230507sp28,\n" +
            "subpartition p20230507sp29),\n" +
            "partition p20230508 values less than (1683475200) (\n" +
            "subpartition p20230508sp0,\n" +
            "subpartition p20230508sp1,\n" +
            "subpartition p20230508sp2,\n" +
            "subpartition p20230508sp3,\n" +
            "subpartition p20230508sp4,\n" +
            "subpartition p20230508sp5,\n" +
            "subpartition p20230508sp6,\n" +
            "subpartition p20230508sp7,\n" +
            "subpartition p20230508sp8,\n" +
            "subpartition p20230508sp9,\n" +
            "subpartition p20230508sp10,\n" +
            "subpartition p20230508sp11,\n" +
            "subpartition p20230508sp12,\n" +
            "subpartition p20230508sp13,\n" +
            "subpartition p20230508sp14,\n" +
            "subpartition p20230508sp15,\n" +
            "subpartition p20230508sp16,\n" +
            "subpartition p20230508sp17,\n" +
            "subpartition p20230508sp18,\n" +
            "subpartition p20230508sp19,\n" +
            "subpartition p20230508sp20,\n" +
            "subpartition p20230508sp21,\n" +
            "subpartition p20230508sp22,\n" +
            "subpartition p20230508sp23,\n" +
            "subpartition p20230508sp24,\n" +
            "subpartition p20230508sp25,\n" +
            "subpartition p20230508sp26,\n" +
            "subpartition p20230508sp27,\n" +
            "subpartition p20230508sp28,\n" +
            "subpartition p20230508sp29),\n" +
            "partition p20230509 values less than (1683561600) (\n" +
            "subpartition p20230509sp0,\n" +
            "subpartition p20230509sp1,\n" +
            "subpartition p20230509sp2,\n" +
            "subpartition p20230509sp3,\n" +
            "subpartition p20230509sp4,\n" +
            "subpartition p20230509sp5,\n" +
            "subpartition p20230509sp6,\n" +
            "subpartition p20230509sp7,\n" +
            "subpartition p20230509sp8,\n" +
            "subpartition p20230509sp9,\n" +
            "subpartition p20230509sp10,\n" +
            "subpartition p20230509sp11,\n" +
            "subpartition p20230509sp12,\n" +
            "subpartition p20230509sp13,\n" +
            "subpartition p20230509sp14,\n" +
            "subpartition p20230509sp15,\n" +
            "subpartition p20230509sp16,\n" +
            "subpartition p20230509sp17,\n" +
            "subpartition p20230509sp18,\n" +
            "subpartition p20230509sp19,\n" +
            "subpartition p20230509sp20,\n" +
            "subpartition p20230509sp21,\n" +
            "subpartition p20230509sp22,\n" +
            "subpartition p20230509sp23,\n" +
            "subpartition p20230509sp24,\n" +
            "subpartition p20230509sp25,\n" +
            "subpartition p20230509sp26,\n" +
            "subpartition p20230509sp27,\n" +
            "subpartition p20230509sp28,\n" +
            "subpartition p20230509sp29),\n" +
            "partition p20230510 values less than (1683648000) (\n" +
            "subpartition p20230510sp0,\n" +
            "subpartition p20230510sp1,\n" +
            "subpartition p20230510sp2,\n" +
            "subpartition p20230510sp3,\n" +
            "subpartition p20230510sp4,\n" +
            "subpartition p20230510sp5,\n" +
            "subpartition p20230510sp6,\n" +
            "subpartition p20230510sp7,\n" +
            "subpartition p20230510sp8,\n" +
            "subpartition p20230510sp9,\n" +
            "subpartition p20230510sp10,\n" +
            "subpartition p20230510sp11,\n" +
            "subpartition p20230510sp12,\n" +
            "subpartition p20230510sp13,\n" +
            "subpartition p20230510sp14,\n" +
            "subpartition p20230510sp15,\n" +
            "subpartition p20230510sp16,\n" +
            "subpartition p20230510sp17,\n" +
            "subpartition p20230510sp18,\n" +
            "subpartition p20230510sp19,\n" +
            "subpartition p20230510sp20,\n" +
            "subpartition p20230510sp21,\n" +
            "subpartition p20230510sp22,\n" +
            "subpartition p20230510sp23,\n" +
            "subpartition p20230510sp24,\n" +
            "subpartition p20230510sp25,\n" +
            "subpartition p20230510sp26,\n" +
            "subpartition p20230510sp27,\n" +
            "subpartition p20230510sp28,\n" +
            "subpartition p20230510sp29),\n" +
            "partition p20230511 values less than (1683734400) (\n" +
            "subpartition p20230511sp0,\n" +
            "subpartition p20230511sp1,\n" +
            "subpartition p20230511sp2,\n" +
            "subpartition p20230511sp3,\n" +
            "subpartition p20230511sp4,\n" +
            "subpartition p20230511sp5,\n" +
            "subpartition p20230511sp6,\n" +
            "subpartition p20230511sp7,\n" +
            "subpartition p20230511sp8,\n" +
            "subpartition p20230511sp9,\n" +
            "subpartition p20230511sp10,\n" +
            "subpartition p20230511sp11,\n" +
            "subpartition p20230511sp12,\n" +
            "subpartition p20230511sp13,\n" +
            "subpartition p20230511sp14,\n" +
            "subpartition p20230511sp15,\n" +
            "subpartition p20230511sp16,\n" +
            "subpartition p20230511sp17,\n" +
            "subpartition p20230511sp18,\n" +
            "subpartition p20230511sp19,\n" +
            "subpartition p20230511sp20,\n" +
            "subpartition p20230511sp21,\n" +
            "subpartition p20230511sp22,\n" +
            "subpartition p20230511sp23,\n" +
            "subpartition p20230511sp24,\n" +
            "subpartition p20230511sp25,\n" +
            "subpartition p20230511sp26,\n" +
            "subpartition p20230511sp27,\n" +
            "subpartition p20230511sp28,\n" +
            "subpartition p20230511sp29),\n" +
            "partition p20230512 values less than (1683820800) (\n" +
            "subpartition p20230512sp0,\n" +
            "subpartition p20230512sp1,\n" +
            "subpartition p20230512sp2,\n" +
            "subpartition p20230512sp3,\n" +
            "subpartition p20230512sp4,\n" +
            "subpartition p20230512sp5,\n" +
            "subpartition p20230512sp6,\n" +
            "subpartition p20230512sp7,\n" +
            "subpartition p20230512sp8,\n" +
            "subpartition p20230512sp9,\n" +
            "subpartition p20230512sp10,\n" +
            "subpartition p20230512sp11,\n" +
            "subpartition p20230512sp12,\n" +
            "subpartition p20230512sp13,\n" +
            "subpartition p20230512sp14,\n" +
            "subpartition p20230512sp15,\n" +
            "subpartition p20230512sp16,\n" +
            "subpartition p20230512sp17,\n" +
            "subpartition p20230512sp18,\n" +
            "subpartition p20230512sp19,\n" +
            "subpartition p20230512sp20,\n" +
            "subpartition p20230512sp21,\n" +
            "subpartition p20230512sp22,\n" +
            "subpartition p20230512sp23,\n" +
            "subpartition p20230512sp24,\n" +
            "subpartition p20230512sp25,\n" +
            "subpartition p20230512sp26,\n" +
            "subpartition p20230512sp27,\n" +
            "subpartition p20230512sp28,\n" +
            "subpartition p20230512sp29),\n" +
            "partition p20230513 values less than (1683907200) (\n" +
            "subpartition p20230513sp0,\n" +
            "subpartition p20230513sp1,\n" +
            "subpartition p20230513sp2,\n" +
            "subpartition p20230513sp3,\n" +
            "subpartition p20230513sp4,\n" +
            "subpartition p20230513sp5,\n" +
            "subpartition p20230513sp6,\n" +
            "subpartition p20230513sp7,\n" +
            "subpartition p20230513sp8,\n" +
            "subpartition p20230513sp9,\n" +
            "subpartition p20230513sp10,\n" +
            "subpartition p20230513sp11,\n" +
            "subpartition p20230513sp12,\n" +
            "subpartition p20230513sp13,\n" +
            "subpartition p20230513sp14,\n" +
            "subpartition p20230513sp15,\n" +
            "subpartition p20230513sp16,\n" +
            "subpartition p20230513sp17,\n" +
            "subpartition p20230513sp18,\n" +
            "subpartition p20230513sp19,\n" +
            "subpartition p20230513sp20,\n" +
            "subpartition p20230513sp21,\n" +
            "subpartition p20230513sp22,\n" +
            "subpartition p20230513sp23,\n" +
            "subpartition p20230513sp24,\n" +
            "subpartition p20230513sp25,\n" +
            "subpartition p20230513sp26,\n" +
            "subpartition p20230513sp27,\n" +
            "subpartition p20230513sp28,\n" +
            "subpartition p20230513sp29),\n" +
            "partition p20230514 values less than (1683993600) (\n" +
            "subpartition p20230514sp0,\n" +
            "subpartition p20230514sp1,\n" +
            "subpartition p20230514sp2,\n" +
            "subpartition p20230514sp3,\n" +
            "subpartition p20230514sp4,\n" +
            "subpartition p20230514sp5,\n" +
            "subpartition p20230514sp6,\n" +
            "subpartition p20230514sp7,\n" +
            "subpartition p20230514sp8,\n" +
            "subpartition p20230514sp9,\n" +
            "subpartition p20230514sp10,\n" +
            "subpartition p20230514sp11,\n" +
            "subpartition p20230514sp12,\n" +
            "subpartition p20230514sp13,\n" +
            "subpartition p20230514sp14,\n" +
            "subpartition p20230514sp15,\n" +
            "subpartition p20230514sp16,\n" +
            "subpartition p20230514sp17,\n" +
            "subpartition p20230514sp18,\n" +
            "subpartition p20230514sp19,\n" +
            "subpartition p20230514sp20,\n" +
            "subpartition p20230514sp21,\n" +
            "subpartition p20230514sp22,\n" +
            "subpartition p20230514sp23,\n" +
            "subpartition p20230514sp24,\n" +
            "subpartition p20230514sp25,\n" +
            "subpartition p20230514sp26,\n" +
            "subpartition p20230514sp27,\n" +
            "subpartition p20230514sp28,\n" +
            "subpartition p20230514sp29));";

    String sql7 = "CREATE TABLE `metric_data_daily` (\n"
            + "  `series_id` bigint(20) NOT NULL COMMENT '指标序列ID',\n"
            + "  `timestamp` bigint(20) NOT NULL COMMENT '时间戳，单位(秒)',\n"
            + "  `value` double NOT NULL COMMENT '值',\n"
            + "  PRIMARY KEY (`series_id`, `timestamp`)\n"
            + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.3.8' REPLICA_NUM = 1 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0 COMMENT = '监控天级别指标数据'\n"
            + " partition by range columns(`timestamp`) subpartition by hash(series_id)\n"
            + "(partition `DUMMY` values less than (0) (\n"
            + "subpartition `DUMMYsp0`,\n"
            + "subpartition `DUMMYsp1`,\n"
            + "subpartition `DUMMYsp2`,\n"
            + "subpartition `DUMMYsp3`,\n"
            + "subpartition `DUMMYsp4`,\n"
            + "subpartition `DUMMYsp5`,\n"
            + "subpartition `DUMMYsp6`,\n"
            + "subpartition `DUMMYsp7`,\n"
            + "subpartition `DUMMYsp8`,\n"
            + "subpartition `DUMMYsp9`,\n"
            + "subpartition `DUMMYsp10`,\n"
            + "subpartition `DUMMYsp11`,\n"
            + "subpartition `DUMMYsp12`,\n"
            + "subpartition `DUMMYsp13`,\n"
            + "subpartition `DUMMYsp14`,\n"
            + "subpartition `DUMMYsp15`,\n"
            + "subpartition `DUMMYsp16`,\n"
            + "subpartition `DUMMYsp17`,\n"
            + "subpartition `DUMMYsp18`,\n"
            + "subpartition `DUMMYsp19`,\n"
            + "subpartition `DUMMYsp20`,\n"
            + "subpartition `DUMMYsp21`,\n"
            + "subpartition `DUMMYsp22`,\n"
            + "subpartition `DUMMYsp23`,\n"
            + "subpartition `DUMMYsp24`,\n"
            + "subpartition `DUMMYsp25`,\n"
            + "subpartition `DUMMYsp26`,\n"
            + "subpartition `DUMMYsp27`,\n"
            + "subpartition `DUMMYsp28`,\n"
            + "subpartition `DUMMYsp29`))";

    @Test
    public void parseCreateTable0() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql0);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable1() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql1);
        assertEquals("test1", tableDefinition.getName());
        assertEquals(4, tableDefinition.getFields().size());
        assertEquals("id", tableDefinition.getFields().get(0).getName());
        assertEquals("name", tableDefinition.getFields().get(1).getName());
        assertEquals("OB的集群名称\\", tableDefinition.getFields().get(1).getComment());
        TableDefinition.Partition partition = tableDefinition.getPartition();
        assertNotNull(partition);
        assertEquals(1, partition.getFields().size());
        assertEquals("begin_interval_time", partition.getFields().get(0));
        assertEquals(3, partition.getRangeElements().size());

        TableDefinition.Partition subPartition = partition.getSubPartition();
        assertNotNull(subPartition);
        assertEquals(1, subPartition.getFields().size());
        assertEquals("collect_time", subPartition.getFields().get(0));
        assertEquals(3, subPartition.getRangeElements().size());

    }

    @Test
    public void parseCreateTable2() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql2);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable3() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql3);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable4() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql4);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable5() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql5);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable6() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql6);
        System.out.println(tableDefinition);
    }

    @Test
    public void parseCreateTable7() {
        OBTableParser obTableParser = new OBTableParser();
        TableDefinition tableDefinition = obTableParser.parseCreateTable(sql7);
        System.out.println(tableDefinition);
        assertEquals("RANGE", tableDefinition.getPartition().getType());
        assertEquals("HASH", tableDefinition.getPartition().getSubPartition().getType());
        assertEquals(Integer.valueOf(30), tableDefinition.getPartition().getSubPartition().getHashPartitionCount());
    }

}
