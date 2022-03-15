package com.apple.playground;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class Play011 {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set(RestOptions.ENABLE_FLAMEGRAPH, true);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env
                .socketTextStream("localhost", 7777)
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        for (String word : line.split(" ")) {
                            collector.collect(Tuple2.of(word, 1));
                        }
                    }
                })
                .keyBy(r -> r.f0)
                .sum(1)
                        .print("7777: ");
        env
                .socketTextStream("localhost", 8888)
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        for (String word : line.split(" ")) {
                            collector.collect(Tuple2.of(word, 1));
                        }
                    }
                })
                .keyBy(r -> r.f0)
                .sum(1)
                .print("8888: ");
        env.execute();
    }
}
