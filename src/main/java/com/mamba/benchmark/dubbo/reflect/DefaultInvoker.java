package com.mamba.benchmark.dubbo.reflect;

import java.lang.reflect.Method;

class DefaultInvoker<T> extends Invoker<T> {

    public DefaultInvoker(T service, Method method) {
        super(service, method);
    }

    @Override
    public Object invoke(Object... args) throws Exception {
        return this.getMethod().invoke(this.getService(), args);
    }
}
