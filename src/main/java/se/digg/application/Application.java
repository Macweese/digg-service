package se.digg.application;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
	info = @Info(
		title = "Digg Service API",
		version = "1.0.0",
		description = "Description ... REST API",
		contact = @Contact(
			name = "Contact person / department",
			email = "support@example.se"
		)
	)
)
public class Application
{
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
}
