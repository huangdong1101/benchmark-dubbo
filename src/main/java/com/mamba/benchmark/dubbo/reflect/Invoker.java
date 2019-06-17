package com.mamba.benchmark.dubbo.reflect;

import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CompletableFuture;

public abstract class Invoker {

    public abstract CompletableFuture<?> invoke(Object... args);

    public static Invoker getInvoker(ApplicationContext context, String beanName, String methodName, Class<?>... parameterTypes) throws Exception {
        Object serviceBean = context.getBean(beanName);
        Class<?> serviceType = context.getType(beanName);
        if (serviceType.isAssignableFrom(GenericService.class)) {
            return new GenericInvoker((GenericService) serviceBean, methodName, parameterTypes);
        } else {
            return new DefaultInvoker(serviceBean, serviceType.getMethod(methodName, parameterTypes));
        }
    }
}
