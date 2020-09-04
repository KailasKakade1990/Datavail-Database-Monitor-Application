package com.datavail;

import com.datavail.plugins.TestPlugin;

/**
 * @File_Desc:Agent Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.Future;

public class MyRunner implements CommandLineRunner {

    private String name;

    @Override
    public void run(String... args) throws Exception {
        

        TestPlugin tp = new TestPlugin();
        Future<String> hello = tp.sayHello();       

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}