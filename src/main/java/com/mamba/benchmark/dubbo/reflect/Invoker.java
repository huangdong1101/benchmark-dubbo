package com.mamba.benchmark.dubbo.reflect;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class Invoker<T> {

    private final T service;

    private final Method method;

    private final Type[] parameterTypes;

    public Invoker(T service, Method method) {
        this.service = service;
        this.method = method;
        if (method.getParameterCount() == 0) {
            this.parameterTypes = new Type[0];
        } else {
            this.parameterTypes = method.getGenericParameterTypes();
        }
    }

    public Object[] castArgs(String... args) {
        int parameterCount = this.parameterTypes.length;
        if (parameterCount == 0) {
            if (args == null || args.length == 0) {
                return new Object[0];
            }
            throw new IllegalArgumentException("Invalid args. expect parameter count: 0, actual args size: " + args.length);
        }
        if (args == null) {
            throw new IllegalArgumentException("Null args. expect parameter count: " + parameterCount);
        }
        if (args.length != parameterCount) {
            throw new IllegalArgumentException("Invalid args. expect parameter count: " + parameterCount + ", actual args size: " + args.length);
        }
        Object[] arguments = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            arguments[i] = cast(args[i], this.parameterTypes[i]);
        }
        return arguments;
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

    private static Object cast(String s, Type type) {
        if (s == null) {
            return null;
        }
        if (type == String.class) {
            return s;
        }
        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(s);
        }
        if (type == short.class || type == Short.class) {
            return Short.valueOf(s);
        }
        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(s);
        }
        if (type == long.class || type == Long.class) {
            return Long.valueOf(s);
        }
        if (type == float.class || type == Float.class) {
            return Float.valueOf(s);
        }
        if (type == double.class || type == Double.class) {
            return Double.valueOf(s);
        }
        if (type == char.class || type == Character.class) {
            if (s.isEmpty()) {
                return null;
            }
            if (s.length() == 1) {
                return s.charAt(0);
            }
            String tmp = s.trim();
            if (tmp.isEmpty()) {
                return ' ';
            }
            if (tmp.length() == 1) {
                return tmp.charAt(0);
            }
            throw new IllegalArgumentException("Invalid char: " + s);
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(s);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(s);
        }
        if (type == BigInteger.class) {
            return new BigInteger(s);
        }
        return JSON.parseObject(s, type);
    }
}
