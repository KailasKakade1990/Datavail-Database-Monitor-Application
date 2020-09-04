package com.datavail.plugins;

/**
 * @File_Desc:Agent Base Plugin
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :BasePlugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.lang.Runnable;
import java.lang.reflect.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BasePlugin implements CommandLineRunner, Runnable {

	protected static Logger logger = LogManager.getLogger(BasePlugin.class);

	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Override
	public void run(String... strings) throws Exception {
		TaskExecutor executor = taskExecutor();
		Runnable instance = this.getClass().newInstance();
		copy(this, instance);
		executor.execute(instance);
	}

	public static <T1 extends Object, T2 extends Object> void copy(T1 entity, T2 entity2)
			throws IllegalAccessException, NoSuchFieldException {
		Class<? extends Object> copy1 = entity.getClass();
		Class<? extends Object> copy2 = entity2.getClass();

		Field[] fromFields = copy1.getDeclaredFields();
		Field[] toFields = copy2.getDeclaredFields();

		Object value = null;

		for (Field field : fromFields) {
			Field field1 = copy2.getDeclaredField(field.getName());
			
			field.setAccessible(true);
			value = field.get(entity);
			field1.setAccessible(true);
			field1.set(entity2, value);

		}
	}
}
