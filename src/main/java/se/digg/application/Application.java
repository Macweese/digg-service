/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
@OpenAPIDefinition(
	info = @Info(
		title = "Digg",
		version = "1.0.0",
		description = "DIGGar du vår tjänst? ;)",
		contact = @Contact(
			name = "Contact person / department",
			email = "support@example.se"
		)
	)
)
public class Application
{
	private static ApplicationEventPublisher eventPublisher;

	@Transactional
	public static void main(String[] args)
	{
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		log.info("Spring Boot application started!");

		eventPublisher = ctx;
		markApplicationReady();

		registerGracefulShutdownHook(ctx);
	}

	private static void markApplicationReady()
	{
		AvailabilityChangeEvent.publish(eventPublisher, new Object(), ReadinessState.ACCEPTING_TRAFFIC);
		System.out.println("Application is now ready to accept traffic");
	}

	private static void registerGracefulShutdownHook(ConfigurableApplicationContext context)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			log.debug("Starting graceful shutdown process...");

			// Mark application as not ready
			AvailabilityChangeEvent.publish(eventPublisher, new Object(), ReadinessState.REFUSING_TRAFFIC);
			log.debug("Application is now refusing traffic");

			// Allow time for load balancer to detect the change
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				log.warn("Shutdown wait interrupted", e);
			}

			context.close();
			log.info("Graceful shutdown completed");
		}));
	}
}
