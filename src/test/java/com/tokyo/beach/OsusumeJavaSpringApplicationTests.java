package com.tokyo.beach;

import com.tokyo.beach.application.OsusumeJavaSpringApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OsusumeJavaSpringApplication.class)
@WebAppConfiguration
public class OsusumeJavaSpringApplicationTests {

	@Test
	public void contextLoads() {
	}

}
