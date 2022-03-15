package com.apple.playground;

import com.apple.utils.MyKafkaUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;

public class Play005 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env
                .addSource(MyKafkaUtils.getKafkaSource("", ""))
                .addSink(MyKafkaUtils.getKafkaProducer("")).setParallelism(3);

        env.execute();
    }
}
