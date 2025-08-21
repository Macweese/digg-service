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
		description = "REST API",
		contact = @Contact(
			name = "Service Department",
			email = "support@example.com"
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


	I have a project that is structured like this:

	spring-vue/
	├── backend/          # Spring Boot project
	│   ├── src/main/java/se/digg/application/
	│   │       │                 ├── controller/
	│   │       │                 │   ├── CustomerController.java
	│   │       │                 │   └── HealthController.java
	│   │       │                 ├── cors/
	│   │       │                 │   └── CorsConfig.java
	│   │       │                 ├── model/
	│   │       │                 │   └── Customer.java
	│   │       │                 ├── service/
	│   │       │                 │   └── CustomerService.java
	│   │       │                 └── Application.java
	│   │       └── resources/
	│   │           ├── application.properties
	│   │           └── resources-test.properties
	│   │
	│   └── pom.xml
	└── frontend/         # Vue 3 project
	    ├── src/
	    │   ├── App.vue   # These are served at localhost:5173 <--- I want the the old front end to be served by these
	    │   └── main.js   # These are served at localhost:5173 <--- I want the the old front end to be served by these
	    ├── node_modules/...auto generated files from npm
	    ├── index.html    # These are the previous files for the front end
	    ├── main.js       # These are the previous files for the front end
	    ├── package.json
	    ├── package-lock.json
	    ├── vite.config.js
	    ├── style.css     # These are the previous files for the front end
	    └── index.html