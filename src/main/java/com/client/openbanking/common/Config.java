package com.client.openbanking.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 이명학
 * @date 2023. 8. 3.
 * @description restTemplate
 */
@Configuration
public class Config {
	@Bean
	public RestTemplate restTemplate() throws Exception {
		return new RestTemplate();
	}
}
