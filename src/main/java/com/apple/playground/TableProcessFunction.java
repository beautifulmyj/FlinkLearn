//package com.apple.playground;
//
//import com.alibaba.fastjson.JSONObject;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.functions.ProcessFunction;
//import org.apache.flink.streaming.api.functions.RichProcessFunction;
//import org.apache.flink.util.Collector;
//import org.apache.flink.util.OutputTag;
//
//public class TableProcessFunction extends RichProcessFunction<JSONObject, JSONObject> {
//
//    private OutputTag<JSONObject> outputTag;
//
//    public TableProcessFunction(OutputTag<JSONObject> outputTag) {
//        this.outputTag = outputTag;
//    }
//
//    @Override
//    public void open(Configuration parameters) throws Exception {
//        super.open(parameters);
//    }
//
//    @Override
//    public void processElement(JSONObject value, ProcessFunction<JSONObject, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
//
//    }
//
//    @Override
//    public void close() throws Exception {
//        super.close();
//    }
//}
