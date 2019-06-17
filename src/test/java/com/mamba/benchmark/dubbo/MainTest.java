package com.mamba.benchmark.dubbo;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void test() throws Exception {
        Main.main("-consumer", MainTest.class.getClassLoader().getResource("consumer.xml").getFile(),
                "-invocation", MainTest.class.getClassLoader().getResource("invocation.json").getFile(),
                "-t",
                "-quantity", "10",
                "-duration", "60"
        );
        System.out.println(1);
    }
}
