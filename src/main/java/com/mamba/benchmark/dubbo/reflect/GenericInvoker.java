package com.mamba.benchmark.dubbo.reflect;

import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

class GenericInvoker extends Invoker {

    private final GenericService service;

    private final String method;

    private final String[] parameterTypes;

    public GenericInvoker(GenericService service, String method, Class<?>... parameterTypes) {
        this.service = service;
        this.method = method;
        this.parameterTypes = (parameterTypes == null || parameterTypes.length == 0) ? new String[0] : Arrays.stream(parameterTypes).map(Class::getName).toArray(String[]::new);
    }

    @Override
    public CompletableFuture<?> invoke(Object... args) {
        return RpcContext.getContext().asyncCall(() -> this.service.$invoke(this.method, this.parameterTypes, args));
    }
}
