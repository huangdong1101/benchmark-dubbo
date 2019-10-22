package com.mamba.benchmark.dubbo.generator;

import com.mamba.benchmark.dubbo.reflect.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

public class InvariantTaskGenerator implements IntFunction<List<Runnable>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvariantTaskGenerator.class);

    private final Invoker invoker;

    private final boolean async;

    private final Object[] arguments;

    public InvariantTaskGenerator(Invoker invoker, boolean async, String... arguments) {
        this.invoker = Objects.requireNonNull(invoker);
        this.async = async;
        if (arguments == null || arguments.length == 0) {
            this.arguments = null;
        } else {
            this.arguments = invoker.castArgs(arguments);
        }
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
        CompletableFuture<?> future = invoker.asyncCall(args);
        if (!async) {
            future.join();
        }
    }
}
