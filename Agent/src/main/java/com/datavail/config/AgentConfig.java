package com.datavail.config;
/**
 * @File_Desc:Agent Config
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentConfig
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ "file:config/datavail-config.xml" })
public class AgentConfig {
	private static String AgentConfig = "AgentConfig";
	protected static Logger logger = LogManager.getLogger(AgentConfig.class);

	AgentConfig() {
		logger.info("AgentConfig-datavail-config.xml" + AgentConfig);
	}

}