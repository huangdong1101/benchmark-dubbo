package com.mamba.benchmark.dubbo.reflect;

import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

class DefaultInvoker extends Invoker {

    private final Object service;

    private final Method method;

    public DefaultInvoker(Object service, Method method) {
        this.service = service;
        this.method = method;
    }

    @Override
    public CompletableFuture<?> invoke(Object... args) {
        return RpcContext.getContext().asyncCall(() -> this.method.invoke(this.service, args));
    }
}
