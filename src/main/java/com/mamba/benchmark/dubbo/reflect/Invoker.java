package com.mamba.benchmark.dubbo.reflect;

import com.mamba.benchmark.common.util.ClassParser;
import lombok.Getter;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class Invoker<T> {

    private final T service;

    private final Method method;

    public Invoker(T service, Method method) {
        this.service = service;
        this.method = method;
    }

    public Object[] castArgs(String... args) {
        return ClassParser.castArgs(this.method, args);
    }

    public abstract Object invoke(Object... args) throws Exception;

    public CompletableFuture<?> asyncCall(Object... args) {
        return RpcContext.getContext().asyncCall(() -> this.invoke(args));
    }

    public static Invoker getInvoker(ApplicationContext context, String beanName, String methodName, Class<?>... parameterTypes) throws Exception {
        Object serviceBean = context.getBean(beanName);
        Class<?> serviceType = context.getType(beanName);
        if (serviceType.isAssignableFrom(GenericService.class)) {
            ReferenceConfig reference = context.getBean("&".concat(beanName), ReferenceConfig.class);
            Map<String, String> metaData = reference.getMetaData();
            String interfaceName = metaData.get("interface");
            Class<?> interfaceType = context.getClassLoader().loadClass(interfaceName);
            Method method = interfaceType.getMethod(methodName, parameterTypes);
            return new GenericInvoker((GenericService) serviceBean, method);
        } else {
            Method method = serviceType.getMethod(methodName, parameterTypes);
            return new DefaultInvoker<>(serviceBean, method);
        }
    }
}
