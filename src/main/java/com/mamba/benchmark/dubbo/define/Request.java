package com.mamba.benchmark.dubbo.define;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mamba.benchmark.common.util.ClassParser;
import org.apache.dubbo.common.compiler.support.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class Request {

    private final String service;

    private final String method;

    private final Argument[] arguments;

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public Argument[] getArguments() {
        return arguments;
    }

    public Request(String service, String method, Argument... arguments) {
        if (StringUtils.isEmpty(service)) {
            throw new IllegalArgumentException("Empty service");
        }
        if (StringUtils.isEmpty(method)) {
            throw new IllegalArgumentException("Empty method");
        }
        this.service = service;
        this.method = method;
        this.arguments = arguments;
    }

    public static Request parse(String text) {
        JSONObject json = JSONObject.parseObject(text);
        String service = json.getString("service");
        if (StringUtils.isEmpty(service)) {
            throw new RuntimeException("Invalid invocation: " + text);
        }
        String method = json.getString("method");
        if (StringUtils.isEmpty(method)) {
            throw new RuntimeException("Invalid invocation: " + text);
        }
        JSONArray argumentArr = json.getJSONArray("arguments");
        if (CollectionUtils.isEmpty(argumentArr)) {
            return new Request(service, method);
        }
        Argument[] arguments = new Argument[argumentArr.size()];
        for (int i = 0; i < argumentArr.size(); i++) {
            JSONObject argument = argumentArr.getJSONObject(i);
            if (argument == null) {
                throw new RuntimeException("Invalid invocation: " + text);
            }
            String type = argument.getString("type");
            String value = argument.getString("value");
            if (StringUtils.isEmpty(type) || StringUtils.isEmpty(value)) {
                throw new RuntimeException("Invalid invocation: " + text);
            }
            arguments[i] = new Argument(type, value);
        }
        return new Request(service, method, arguments);
    }

    public static class Argument<T> {

        private final Class<T> type;

        private final String text;

        public Argument(String type, String text) {
            try {
                this.type = (Class<T>) ClassUtils.classForName(type);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Invalid class: " + type);
            }
            this.text = text;
        }

        public Class<?> getType() {
            return type;
        }

        public T getValue() {
            return ClassParser.parse(this.type, this.text);
        }
    }
}
