package ticket.devinf.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import ticket.devinf.server.TicketServer;

@Configuration
@PropertySource({ "classpath:/server.properties" })
@ComponentScan({ "ticket.server.config", "ticket.server.server" })
public class TicketServerConfig {

	@Autowired
	private Environment env;

	@Bean
	public TicketServer ticketServer() {
		int port = Integer.parseInt(env.getRequiredProperty("port"));
		int readTimeOut = Integer.parseInt(env.getRequiredProperty("readTimeOut"));
		int eventExecutor = Integer.parseInt(env.getRequiredProperty("eventExecutor"));
		return new TicketServer(port, readTimeOut, eventExecutor);
	}

}
