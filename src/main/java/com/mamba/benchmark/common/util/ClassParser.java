package com.mamba.benchmark.common.util;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClassParser<T> {

    private Class<T> type;

    private Function<String, T> mapper;

    private ClassParser(Class<T> type, Function<String, T> mapper) {
        this.type = type;
        this.mapper = mapper;
    }

    public Class<T> getType() {
        return type;
    }

    public T parse(String s) {
        return this.mapper.apply(s);
    }

    public static <T> T parse(Class<T> type, String s) {
        ClassParser parser = COMMON_PARSERS.get(type.getName());
        if (parser == null) {
            return JSON.parseObject(s, type);
        } else {
            return (T) parser.parse(s);
        }
    }

    private static final ClassParser[] BASIC_TYPES = {
            new ClassParser(byte.class, (Function<String, Byte>) Byte::parseByte),
            new ClassParser(short.class, (Function<String, Short>) Short::parseShort),
            new ClassParser(int.class, (Function<String, Integer>) Integer::parseInt),
            new ClassParser(long.class, (Function<String, Long>) Long::parseLong),
            new ClassParser(float.class, (Function<String, Float>) Float::parseFloat),
            new ClassParser(double.class, (Function<String, Double>) Double::parseDouble),
            new ClassParser(char.class, (Function<String, Character>) ClassParser::parseChar),
            new ClassParser(boolean.class, (Function<String, Boolean>) Boolean::parseBoolean)
    };

    private static final ClassParser[] COMMON_TYPES = {
            new ClassParser(Byte.class, (Function<String, Byte>) Byte::valueOf),
            new ClassParser(Short.class, (Function<String, Short>) Short::valueOf),
            new ClassParser(Integer.class, (Function<String, Integer>) Integer::valueOf),
            new ClassParser(Long.class, (Function<String, Long>) Long::valueOf),
            new ClassParser(Float.class, (Function<String, Float>) Float::valueOf),
            new ClassParser(Double.class, (Function<String, Double>) Double::valueOf),
            new ClassParser(Character.class, (Function<String, Character>) ClassParser::toCharacter),
            new ClassParser(Boolean.class, (Function<String, Boolean>) Boolean::valueOf),
            new ClassParser(String.class, Function.identity()),
    };

    private static final Map<String, ClassParser> COMMON_PARSERS = new HashMap<>();

    static {
        for (ClassParser parser : BASIC_TYPES) {
            COMMON_PARSERS.put(parser.getType().getName(), parser);
        }
        for (ClassParser parser : COMMON_TYPES) {
            COMMON_PARSERS.put(parser.getType().getName(), parser);
        }
    }

    private static char parseChar(String s) {
        if (s == null || s.isEmpty()) {
            throw new NullPointerException("Empty char!");
        }
        if (s.length() > 1) {
            throw new IllegalArgumentException("Invalid char: '" + s + "'");
        }
        return s.charAt(0);
    }

    private static Character toCharacter(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        if (s.length() > 1) {
            throw new IllegalArgumentException("Invalid char: '" + s + "'");
        }
        return s.charAt(0);
    }
}
