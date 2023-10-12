package com.client.openbanking;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 박지영
 * @date 2023. 8. 25.
 * @description ajp 설정
 */
@Configuration
public class ContainerConfig {
	
	@Value("${tomcat.ajp.protocol}")
	String ajpProtocol;
	
	@Value("${tomcat.ajp.port}")
	int ajpPort;
	
	@Value("${tomcat.ajp.address}")
	String address;
	
	@Value("${tomcat.ajp.allowedRequestAttributesPattern}")
	String allowedRequestAttributesPattern;
	
	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createAjpConnector());
		
		return tomcat;
	}
	
	private Connector createAjpConnector() {
		Connector ajpConnector = new Connector(ajpProtocol);
		
		ajpConnector.setPort(ajpPort);
		ajpConnector.setSecure(false);
		ajpConnector.setAllowTrace(false);
		ajpConnector.setScheme("http");
		ajpConnector.setProperty("address", address);
		ajpConnector.setProperty("allowedRequestAttributesPattern", allowedRequestAttributesPattern);
		((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler()).setSecretRequired(false);
		
		return ajpConnector;
	}

}