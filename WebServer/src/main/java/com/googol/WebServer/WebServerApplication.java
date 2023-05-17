package com.googol.WebServer;

import RMICon.WSInterface;
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

	/**
	 * Cria e incializa um objeto WebServerRMI, (responsavel pela comunicaçao com o Search Module)
	 * @return
	 * @throws RemoteException
	 */
	@Bean
	public WebServerRMI initRMI() throws RemoteException {
		WebServerRMI ws = new WebServerRMI();
		ws.getH().NewWebServer((WSInterface) ws);
		return ws;
	}
}
