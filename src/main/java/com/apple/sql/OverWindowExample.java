package com.apple.sql;

import com.apple.bean.Event;
import com.apple.sources.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

import static org.apache.flink.table.api.Expressions.$;

public class OverWindowExample {
    public static void main(String[] args) throws Exception{
        // 创建环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 获取SOURCE，并指定TIMESTAMP，生成WATERMARK
        SingleOutputStreamOperator<Event> streamByEnv = env
                .addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(
                                        (SerializableTimestampAssigner<Event>) (event, l) -> event.timestamp)
                );
        // 创建表环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 流转表，指定时间属性
        Table tableFromstreamByEnv = tableEnv
                .fromDataStream(
                        streamByEnv,
                        $("user"),
                        $("url"),
                        $("timestamp").rowtime().as("ts")
                );
        // 创建视图
        tableEnv.createTemporaryView("clicks", tableFromstreamByEnv);
        // 定义子查询，进行窗口聚合，得到包含窗口信息、用户以及访问次数的结果表
        String subQuery =
                "SELECT user,count(url) AS cnt,window_start AS win_on,window_end AS win_off " +
                        "FROM TABLE(" +
                        "TUMBLE(TABLE clicks, DESCRIPTOR(ts), INTERVAL '5' SECOND)" +
                        ") " +
                        "GROUP BY user, window_start, window_end";
        // SQL —— TOP-N
        /*
         * 虽然 Flink SQL 明确指出开窗函数中的 ORDER BY 后只能接时间字段，
         * 但是 Flink 为了能够处理排序需求，单独对此类需求做了优化。
         * 也就是说，下列的 SQL 语句只能按照此写法进行书写，
         * 即先用子查询 【SELECT *, OVER(... ORDER BY cnt DESC) AS ... FROM ...】后，
         * 再 【SELECT ... FROM (子查询) WHERE ...】
         */
        String topNSQL =
                "SELECT * " +
                        "FROM (" +
                        "SELECT " +
                        "*," +
                        "ROW_NUMBER() OVER(" +
                        "PARTITION BY win_on, win_off " +
                        "ORDER BY cnt DESC" +
                        ") AS row_num " +
                        "FROM (" + subQuery + ")" +
                        ")" +
                        "WHERE row_num <= 3";
        // 得到结果表
        Table resultTable = tableEnv.sqlQuery(topNSQL);
        // 创建输出表
        tableEnv.executeSql(
                "CREATE TABLE output(" +
                        "`user` STRING, " +
                        "`cnt` BIGINT, " +
                        "`win_on` TIMESTAMP, " +
                        "`win_off` TIMESTAMP, " +
                        "`rank` BIGINT" +
                        ")" +
                        "WITH(" +
                        "'connector' = 'print'" +
                        ")"
        );
        // 执行并打印
        resultTable.executeInsert("output");
    }
}
