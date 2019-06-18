package com.mamba.benchmark.dubbo;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mamba.benchmark.common.executor.PressureExecutor;
import com.mamba.benchmark.common.pressure.Custom;
import com.mamba.benchmark.common.pressure.Fixed;
import com.mamba.benchmark.common.pressure.Gradient;
import com.mamba.benchmark.common.pressure.Pressure;
import com.mamba.benchmark.dubbo.generator.InvariantTaskGenerator;
import com.mamba.benchmark.dubbo.define.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"-consumer"}, description = "Consumer context path", required = true)
    private File consumer;

    @Parameter(names = {"-req", "-request"}, description = "Request config path", required = true)
    private File request;

    @Parameter(names = {"-t"}, description = "throughput")
    private boolean throughput;

    @Parameter(names = {"-c"}, description = "concurrency")
    private boolean concurrency;

    @Parameter(names = {"-quantity"})
    private Integer quantity;

    @Parameter(names = {"-duration"})
    private Integer duration;

    @Parameter(names = {"-rampup", "-ramp-up"})
    private Integer rampup;

    @Parameter(names = {"-initialQuantity", "-initial-quantity"})
    private Integer initialQuantity;

    @Parameter(names = {"-finalQuantity", "-final-quantity"})
    private Integer finalQuantity;

    @Parameter(names = {"-incrementPerStep", "-increment-per-step"})
    private Integer incrementPerStep;

    @Parameter(names = {"-durationPerStep", "-duration-per-step"})
    private Integer durationPerStep;

    @Parameter(names = {"-quantities"})
    private List<String> quantities;

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
    }

    private PressureExecutor<Runnable> getExecutor(ApplicationContext context) throws Exception {
        if (this.concurrency == this.throughput) {
            throw new IllegalArgumentException("Invalid argument: concurrency=" + this.concurrency + ", throughput=" + throughput);
        }
        boolean concurrency = this.concurrency;
        Pressure pressure = this.getPressure();
        IntFunction<List<Runnable>> generator = this.getGenerator(context, this.throughput);
        if (concurrency) {
            return PressureExecutor.concurrency(generator, pressure::currentQuantity);
        } else {
            return PressureExecutor.throughput(generator, pressure::currentQuantity);
        }
    }

    private IntFunction<List<Runnable>> getGenerator(ApplicationContext context, boolean async) throws Exception {
        String requestStr = Files.toString(this.request, Charsets.UTF_8);
        Request request = Request.parse(requestStr);
        return InvariantTaskGenerator.newInstance(context, request, async);
    }

    private Pressure getPressure() {
        if (this.quantity != null && this.duration != null) {
            if (this.rampup == null) {
                return new Fixed(this.quantity, this.duration);
            } else {
                return new Fixed(this.quantity, this.duration, this.rampup);
            }
        }
        if (this.initialQuantity != null && this.finalQuantity != null && this.incrementPerStep != null && this.durationPerStep != null) {
            return new Gradient(this.initialQuantity, this.finalQuantity, this.incrementPerStep, this.durationPerStep);
        }
        if (!CollectionUtils.isEmpty(this.quantities) && this.durationPerStep != null) {
            return new Custom(this.quantities.stream().mapToInt(Integer::parseInt).toArray(), this.durationPerStep);
        }
        throw new IllegalArgumentException("Invalid argument for init pressure");
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }
}
