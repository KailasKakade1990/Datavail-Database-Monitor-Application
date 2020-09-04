package com.datavail.plugins;
/**
 * @File_Desc:Agent T
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :TestPlugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
import org.springframework.scheduling.annotation.AsyncResult;
import java.util.concurrent.Future;


public class TestPlugin extends BasePlugin {

    private String name1;


    public Future<String> sayHello(){
        return new AsyncResult<>("Hello");
    }


    @Override
    public void run() {

        try {
            while (true) {
                System.out.println("Going to sleep Thread 1");
                Thread.sleep(1000);
                System.out.println("Awake Thread 1");
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }
}
