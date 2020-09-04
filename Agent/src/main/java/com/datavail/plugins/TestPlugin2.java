package com.datavail.plugins;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

/**
 * @File_Desc:Agent Test Plugin
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class TestPlugin2 extends BasePlugin {

	private String name1;

	@Async
	public Future<String> sayHello() {
		return new AsyncResult<>("Hello");
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("Going to sleep Thread 2");
				Thread.sleep(1000);
				System.out.println("Awake Thread 2");
			}
		} catch (Exception ex) {
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
