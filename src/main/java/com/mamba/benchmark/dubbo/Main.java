package com.mamba.benchmark.dubbo;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.mamba.benchmark.common.executor.PressureExecutor;
import com.mamba.benchmark.common.pressure.Pressure;
import com.mamba.benchmark.dubbo.conf.RequestConf;
import com.mamba.benchmark.dubbo.generator.InvariantTaskGenerator;
import com.mamba.benchmark.dubbo.reflect.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"-c"}, description = "concurrency")
    private String concurrency;

    @Parameter(names = {"-q"}, description = "throughput")
    private String throughput;

    @Parameter(names = {"-t"}, description = "timelimit", required = true)
    private int timelimit;

    @Parameter(names = {"-consumer", "--consumer"}, description = "Consumer context", required = true)
    private File consumer;

    @Parameter(description = "Request config", required = true, converter = RequestConfConverter.class)
    private RequestConf request;

    public void run() throws Exception {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext(new FileSystemResource(this.consumer));
        try {
            context.start();
            this.run(context);
        } finally {
            context.stop();
        }
    }

    private void run(ApplicationContext context) throws Exception {
        try (PressureExecutor executor = this.getExecutor(context)) {
            LOGGER.info("PressureExecutor will start in 1 second!");
            executor.start(1);
            LOGGER.info("PressureExecutor will stop in 10 second!");
            TimeUnit.SECONDS.sleep(10);
        }
        LOGGER.info("Thrift Benchmark Completed!");
    }

    private PressureExecutor<Runnable> getExecutor(ApplicationContext context) throws Exception {
        if (this.timelimit < 10) {
            throw new IllegalArgumentException("Invalid argument: timelimit=" + this.timelimit);
        }
        if (this.concurrency == null) {
            if (this.throughput == null) {
                throw new IllegalArgumentException("Invalid argument: concurrency is null, throughput is null");
            }
            Pressure pressure = Pressure.parse(this.throughput, this.timelimit);
            IntFunction<List<Runnable>> generator = this.getGenerator(context, true);
            return PressureExecutor.throughput(generator, pressure::currentQuantity);
        } else {
            if (this.throughput != null) {
                throw new IllegalArgumentException("Invalid argument: concurrency=" + this.concurrency + ", throughput=" + throughput);
            }
            Pressure pressure = Pressure.parse(this.concurrency, this.timelimit);
            IntFunction<List<Runnable>> generator = this.getGenerator(context, false);
            return PressureExecutor.concurrency(generator, pressure::currentQuantity);
        }
    }

    private IntFunction<List<Runnable>> getGenerator(ApplicationContext context, boolean async) throws Exception {
        Invoker invoker = Invoker.getInvoker(context, this.request.getService(), this.request.getMethod(), this.request.getParameterTypes());
        return new InvariantTaskGenerator(invoker, async, this.request.getArguments());
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }

    private static class RequestConfConverter implements IStringConverter<RequestConf> {
        @Override
        public RequestConf convert(String s) {
            try {
                return new RequestConf(s);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
