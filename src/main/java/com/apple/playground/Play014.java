package com.apple.playground;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class Play014 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        OutputTag<TouchEvent> outputTag = new OutputTag<TouchEvent>("late data") {
        };

        SingleOutputStreamOperator<Tuple2<String, Long>> resultDS = env
                .socketTextStream("localhost", 7777)
                .map(new MapFunction<String, TouchEvent>() {
                    @Override
                    public TouchEvent map(String line) throws Exception {
                        String[] arrs = line.split(",");
                        return TouchEvent.of(arrs[0], Long.valueOf(arrs[1]));
                    }
                })
                .assignTimestampsAndWatermarks(WatermarkStrategy.<TouchEvent>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<TouchEvent>() {
                    @Override
                    public long extractTimestamp(TouchEvent touchEvent, long l) {
                        return touchEvent.ts;
                    }
                }))
                .keyBy(r -> r.user).window(TumblingEventTimeWindows.of(Time.seconds(5))).sideOutputLateData(outputTag).process(new ProcessWindowFunction<TouchEvent, Tuple2<String, Long>, String, TimeWindow>() {
//                    ValueState<Long> visitCnt;


                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
//                        visitCnt = getRuntimeContext().getState(new ValueStateDescriptor<Long>("visit count", Long.class));
                    }

                    @Override
                    public void process(String s, ProcessWindowFunction<TouchEvent, Tuple2<String, Long>, String, TimeWindow>.Context context, Iterable<TouchEvent> elements, Collector<Tuple2<String, Long>> out) throws Exception {
//                        if (visitCnt.value() == null) {
//                            visitCnt.update(0L);
//                        }
                        Long cnt = 0L;
                        for (TouchEvent i : elements) {
                            cnt++;
                        }

//                        visitCnt.update(cnt);

                        out.collect(Tuple2.of(s, cnt));
                    }
                });

resultDS.print(">>>");
resultDS.getSideOutput(outputTag).print("***");
        env.execute("");
    }
}
