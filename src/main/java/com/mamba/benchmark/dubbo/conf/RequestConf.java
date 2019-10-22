package com.mamba.benchmark.dubbo.conf;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import org.apache.dubbo.common.compiler.support.ClassUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RequestConf {

    private final String service;

    private final String method;

    private final Class<?>[] parameterTypes;

    private final String[] arguments;

    public RequestConf(String str) throws Exception {
        //demoService.sayHi(java.lang.String:zhansan,java.lang.String:lisi)
        int se = str.indexOf('.'); //end index of service bean
        if (se < 1) {
            throw new RuntimeException("Invalid invocation: " + str);
        }
        int me = str.indexOf('(', se + 1); //end index of method
        if (me < 1) {
            throw new RuntimeException("Invalid invocation: " + str);
        }
        int ae = str.indexOf(')', se + 1); //end index of args
        if (ae < 0) {
            throw new RuntimeException("Invalid invocation: " + str);
        }
        this.service = str.substring(0, se);
        this.method = str.substring(se + 1, me);

        String argumentsText = str.substring(me + 1, ae).trim();
        Stream<String> argumentsStream;
        if (argumentsText.charAt(0) == '@') {
            File argumentsFile = new File(argumentsText.substring(1));
            argumentsStream = Files.readLines(argumentsFile, Charsets.UTF_8).stream();
        } else {
            argumentsStream = StreamSupport.stream(Splitter.on(',').split(argumentsText).spliterator(), false);
        }
        List<String> argumentTexts = argumentsStream.map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        this.parameterTypes = new Class<?>[argumentTexts.size()];
        this.arguments = new String[argumentTexts.size()];
        for (int i = 0; i < argumentTexts.size(); i++) {
            String text = argumentTexts.get(i);
            int shards = text.indexOf(':');
            String type = text.substring(0, shards).trim();
            this.arguments[i] = text.substring(shards + 1).trim();
            this.parameterTypes[i] = ClassUtils.classForName(type);
        }
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public Class<?>[] getParameterTypes() {
        return cloneArray(this.parameterTypes);
    }

    public String[] getArguments() {
        return cloneArray(this.arguments);
    }

    private static <T> T[] cloneArray(T[] array) {
        if (array == null || array.length == 0) {
            return array;
        } else {
            return array.clone();
        }
    }
}
