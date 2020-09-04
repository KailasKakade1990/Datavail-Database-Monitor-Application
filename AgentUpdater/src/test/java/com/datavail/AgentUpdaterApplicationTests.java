package com.datavail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringRunner.class)
//,locations = {"classpath:/src/main/resources/log4j2-spring.xml"}
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {AgentUpdaterApplication.class})
@ActiveProfiles("DeltaAgentUpdater") 
public class AgentUpdaterApplicationTests {

	@Test
	public void contextLoads() {
	}

}
