package ticket.server.start;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ticket.server.config.TicketServerConfig;

public class TicketServerStart {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(TicketServerConfig.class);
		ctx.refresh();
	}
}
