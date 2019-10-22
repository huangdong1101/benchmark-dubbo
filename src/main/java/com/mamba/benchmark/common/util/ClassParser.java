package com.mamba.benchmark.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClassParser {

    public static Object[] castArgs(Method method, String... args) {
        int parameterCount = method.getParameterCount();
        if (method.getParameterCount() == 0) {
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
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterCount; i++) {
            arguments[i] = cast(args[i], parameterTypes[i]);
        }
        return arguments;
    }

    public static <T> T cast(Object obj, Type type) {
        if (obj == null) {
            return null;
        }
        if (type == String.class) {
            return (T) obj.toString();
        }
        if (type == byte.class || type == Byte.class) {
            return (T) TypeUtils.castToByte(obj);
        }
        if (type == short.class || type == Short.class) {
            return (T) TypeUtils.castToShort(obj);
        }
        if (type == int.class || type == Integer.class) {
            return (T) TypeUtils.castToInt(obj);
        }
        if (type == long.class || type == Long.class) {
            return (T) TypeUtils.castToLong(obj);
        }
        if (type == float.class || type == Float.class) {
            return (T) TypeUtils.castToFloat(obj);
        }
        if (type == double.class || type == Double.class) {
            return (T) TypeUtils.castToDouble(obj);
        }
        if (type == char.class || type == Character.class) {
            return (T) TypeUtils.castToChar(obj);
        }
        if (type == boolean.class || type == Boolean.class) {
            return (T) TypeUtils.castToBoolean(obj);
        }
        if (type == BigDecimal.class) {
            return (T) TypeUtils.castToBigDecimal(obj);
        }
        if (type == BigInteger.class) {
            return (T) TypeUtils.castToBigInteger(obj);
        }
        return JSON.parseObject(obj.toString(), type);
    }
}
