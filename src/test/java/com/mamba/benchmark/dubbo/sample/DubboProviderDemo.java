package com.mamba.benchmark.dubbo.sample;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DubboProviderDemo {

    public static void main(String... args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("provider.xml");
        context.start();
        synchronized (DubboProviderDemo.class) {
            try {
                DubboProviderDemo.class.wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
