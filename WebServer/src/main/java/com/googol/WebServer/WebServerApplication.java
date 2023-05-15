package com.googol.WebServer;

import RMICon.WebServerRMI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.rmi.RemoteException;

@SpringBootApplication
public class WebServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebServerApplication.class, args);
	}

	@Bean
	public WebServerRMI initRMI() throws RemoteException {
		return new WebServerRMI();
	}
}
