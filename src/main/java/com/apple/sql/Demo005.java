package com.apple.sql;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Demo005 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);
        tEnv.executeSql("create table clicks (`user` STRING, `url` STRING, `ts` BIGINT, `et` AS TO_TIMESTAMP(FROM_UNIXTIME(`ts` / 1000)), WATERMARK FOR `et` AS et - INTERVAL '5' SECOND) WITH('connector' = 'filesystem', 'path' = 'datas', 'format' = 'csv')");
        Table resultTbl = tEnv.sqlQuery("select * from clicks");
        tEnv.toDataStream(resultTbl).print();
        env.execute();
    }
}
