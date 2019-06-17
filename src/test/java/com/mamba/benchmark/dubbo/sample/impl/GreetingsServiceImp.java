package com.mamba.benchmark.dubbo.sample.impl;

import com.mamba.benchmark.dubbo.sample.face.GreetingsService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingsServiceImp implements GreetingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingsServiceImp.class);

    @Override
    public String sayHi(String name, String name2) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("traceId: {}", RpcContext.getContext().getAttachment("_RPC_TRACE_ID_"));
//        throw new RpcException(100, "hi," + name + " ;nice to meet you," + name2, new UnsupportedOperationException("say"));
        return "hi," + name + " ;nice to meet you," + name2;
    }

    @Override
    public String sayGoodBye(String name) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Goodbye," + name;
    }

    @Override
    public Integer add(int a, int b) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a + b;
    }

    @Override
    public String hello() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello world";
    }
}
