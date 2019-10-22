package com.mamba.benchmark.dubbo.reflect;

import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Method;
import java.util.Arrays;

class GenericInvoker extends Invoker<GenericService> {

    private final String[] parameterTypes;

    public GenericInvoker(GenericService service, Method method) {
        super(service, method);
        if (method.getParameterCount() == 0) {
            this.parameterTypes = null;
        } else {
            this.parameterTypes = Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new);
        }
    }

    @Override
    public Object invoke(Object... args) throws GenericException {
        return this.getService().$invoke(this.getMethod().getName(), this.parameterTypes, args);
    }
}
