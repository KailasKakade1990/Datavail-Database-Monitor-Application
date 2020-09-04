package com.datavail;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Agent Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :March-03-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
@SpringBootApplication
@EnableAutoConfiguration
@Profile({ "DeltaAgent", "Default" })
@PropertySource("classpath:application.properties")
public class AgentApplication extends AsyncConfigurerSupport {
	protected static Logger logger = LogManager.getLogger(AgentApplication.class);

	public static void main(String[] args) {

		try {
			SpringApplication.run(AgentApplication.class, args);
			// LOG.trace("context: " + context);

			Properties properties = getConfigurationProperty();
			String linuxpassword = properties.getProperty("linuxpassword");
			// System.out.println("Inside Agent::2");
			CommonPluginMessages cpm = new CommonPluginMessages();
			String cmd1 = "service deltaagentupdater status |grep 'Not running'";
			// System.out.println("Inside Agent:: 3");

			String Status = cpm.RunLinuxGrepCommand(cmd1);
			// System.out.println("StatusStatus agent1" + Status);
			Status = Status.trim();
			if (!Status.contains("Error:") && !Status.equals("")) {
				// System.out.println("Inside Agent::4");
				// System.out.println("StatusStatus agent2" + Status);
				startAgentUpdaterService(linuxpassword);
			}
			// System.out.println("StatusStatus agent3" + Status);
		} catch (Exception ex) {
			logger.info("Agent Exception occurs " + ex.toString());

		}
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		try {
			executor.setCorePoolSize(2);
			executor.setMaxPoolSize(2);
			executor.setQueueCapacity(500);
			executor.setThreadNamePrefix("DatavailAgent-");
			executor.initialize();
			return executor;
		} catch (Exception ex) {
			logger.info("Executor Exception occurs " + ex.toString());
		}
		return null;
	}

	public static boolean startAgentUpdaterService(String linuxPassword) throws IOException, InterruptedException {
		String executeCmd = "echo '" + linuxPassword + "' | sudo -S service deltaagentupdater start";
		Process runtimeProcess = Runtime.getRuntime().exec(new String[] { "bash", "-c", executeCmd });
		runtimeProcess.waitFor();

		if (runtimeProcess.exitValue() != 0) {
			// updaterlogger.info("Error while executing start script");
			System.out.println("Error while executing start script");
			return false;
		} else {
			// updaterlogger.info("Agent Started");
			System.out.println("AgentUpdater Started");
			return true;
		}
	}

	public static Properties getConfigurationProperty() throws InvalidPropertiesFormatException, IOException {
		File file = new File("config.xml");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.loadFromXML(fileInput);
		fileInput.close();
		return properties;
	}
}
