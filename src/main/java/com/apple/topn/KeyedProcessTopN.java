package com.apple.topn;

import com.apple.bean.Event;
import com.apple.chapter06.UrlViewCount;
import com.apple.sources.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;

public class KeyedProcessTopN {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        SingleOutputStreamOperator<Event> pvStream = env
                .addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO).withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                    @Override
                    public long extractTimestamp(Event event, long l) {
                        return event.timestamp;
                    }
                }));

        SingleOutputStreamOperator<UrlViewCount> uvcStream = pvStream
                .keyBy(r -> r.url)
                .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .aggregate(new CountAgg(), new WindowResult());

        uvcStream.keyBy(r -> r.windowEnd).process(new TopN(2)).print();

        env.execute();
    }

    public static class CountAgg implements AggregateFunction<Event, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(Event event, Long aLong) {
            return aLong + 1L;
        }

        @Override
        public Long getResult(Long aLong) {
            return aLong;
        }

        @Override
        public Long merge(Long aLong, Long acc1) {
            return null;
        }
    }

    public static class WindowResult extends ProcessWindowFunction<Long, UrlViewCount, String, TimeWindow> {

        @Override
        public void process(String s, ProcessWindowFunction<Long, UrlViewCount, String, TimeWindow>.Context context, Iterable<Long> elements, Collector<UrlViewCount> out) throws Exception {
            out.collect(new UrlViewCount(s, elements.iterator().next(), context.window().getStart(), context.window().getEnd()));
        }
    }

    public static class TopN extends KeyedProcessFunction<Long, UrlViewCount, String> {

        private final Integer threshold;
        private ListState<UrlViewCount> urlViewCountListState;

        public TopN(Integer threshold) {
            this.threshold = threshold;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            urlViewCountListState = getRuntimeContext().getListState(new ListStateDescriptor<UrlViewCount>("list-state", UrlViewCount.class));
        }

        @Override
        public void processElement(UrlViewCount value, KeyedProcessFunction<Long, UrlViewCount, String>.Context ctx, Collector<String> out) throws Exception {
            urlViewCountListState.add(value);
            ctx.timerService().registerEventTimeTimer(value.windowEnd + 1);
        }

        @Override
        public void onTimer(long timestamp, KeyedProcessFunction<Long, UrlViewCount, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
            super.onTimer(timestamp, ctx, out);

            ArrayList<UrlViewCount> urlViewCountArrayList = new ArrayList<>();
            for(UrlViewCount url : urlViewCountListState.get()) {
                urlViewCountArrayList.add(url);
            }

            urlViewCountListState.clear();

            // 排序
            urlViewCountArrayList.sort(new Comparator<UrlViewCount>() {
                @Override
                public int compare(UrlViewCount o1, UrlViewCount o2) {
                    return o2.count.intValue() - o1.count.intValue();
                }
            });

            // 取前三名，构建输出结果
            StringBuilder result = new StringBuilder();
            result.append("========================================\n");
            for (int i = 0; i < this.threshold; i++) {
                UrlViewCount UrlViewCount = urlViewCountArrayList.get(i);
                result
                        .append("浏览量No." + (i + 1) + " ")
                        .append("url：" + UrlViewCount.url + " ")
                        .append("浏览量：" + UrlViewCount.count + " ")
                        .append("窗口结束时间：" + new Timestamp(timestamp - 1) + "\n");
            }
            result
                    .append("========================================\n\n\n");
            out.collect(result.toString());

        }
    }
}
