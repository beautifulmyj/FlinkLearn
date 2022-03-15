package com.apple.playground;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.apple.utils.MyKafkaUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class Play006 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        OutputTag<String> outputTag = new OutputTag<String>("dirty_json") {};

        env
                .socketTextStream("localhost", 7777)
                .process(new ProcessFunction<String, JSONObject>() {
                    @Override
                    public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                        try {
                            JSONObject jsonObject = JSON.parseObject(value);
                            out.collect(jsonObject);
                        } catch (JSONException e) {
                            ctx.output(outputTag, "dirty json");
                        }
                    }
                })
                .keyBy(jsonObject -> jsonObject.get("name"))
                .map(new RichMapFunction<JSONObject, String>() {
                    private ValueState<JSONObject> visitorState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        visitorState = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("visitorState", JSONObject.class));
                    }

                    @Override
                    public String map(JSONObject jsonObject) throws Exception {
                        return jsonObject.toJSONString();
                    }
                })
                .addSink(MyKafkaUtils.getKafkaProducer(""));

        env.execute();
    }
}
