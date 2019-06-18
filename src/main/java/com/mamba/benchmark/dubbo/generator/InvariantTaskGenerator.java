package com.mamba.benchmark.dubbo.generator;

import com.mamba.benchmark.dubbo.define.Request;
import com.mamba.benchmark.dubbo.reflect.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

public class InvariantTaskGenerator implements IntFunction<List<Runnable>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvariantTaskGenerator.class);

    private final Invoker invoker;

    private final boolean async;

    private final Object[] arguments;

    public InvariantTaskGenerator(Invoker invoker, boolean async, Object... arguments) {
        this.invoker = invoker;
        this.arguments = arguments;
        this.async = async;
    }

    @Override
    public List<Runnable> apply(int num) {
        List<Runnable> tasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            tasks.add(() -> execute(this.async, this.invoker, this.arguments));
        }
        return tasks;
    }

    private static void execute(boolean async, Invoker invoker, Object... args) {
        CompletableFuture<?> future = invoker.invoke(args);
        if (!async) {
            future.join();
        }
    }

    public static InvariantTaskGenerator newInstance(ApplicationContext context, Request request, boolean async) throws Exception {
        String service = request.getService();
        String method = request.getMethod();
        Request.Argument[] arguments = request.getArguments();
        if (arguments == null || arguments.length == 0) {
            return new InvariantTaskGenerator(Invoker.getInvoker(context, service, method), async);
        }
        Object[] argumentValues = new String[arguments.length];
        Class<?>[] argumentTypes = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Request.Argument argument = arguments[i];
            argumentTypes[i] = argument.getType();
            argumentValues[i] = argument.getValue();
        }
        return new InvariantTaskGenerator(Invoker.getInvoker(context, service, method, argumentTypes), async, argumentValues);
    }
}
