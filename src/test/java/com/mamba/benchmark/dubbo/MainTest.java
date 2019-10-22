package com.mamba.benchmark.dubbo;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void test1() throws Exception {
        Main.main("-q", "1", "-t", "60",
                "-consumer", MainTest.class.getClassLoader().getResource("consumer.xml").getFile(),
                "demoService.sayHi(java.lang.String:zhansan,java.lang.String:lisi)"
        );
        System.out.println(1);
    }

    @Test
    void test2() throws Exception {
        Main.main("-c", "1", "-t", "60",
                "-consumer", MainTest.class.getClassLoader().getResource("consumer.xml").getFile(),
                "demoService.sayHi(@" + MainTest.class.getClassLoader().getResource("arguments.txt").getFile() + ")"
        );
        System.out.println(1);
    }
}
