package ticket.devinf.start;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ticket.devinf.config.TicketServerConfig;

public class TicketServerStart {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(TicketServerConfig.class);
		ctx.refresh();
	}
}
