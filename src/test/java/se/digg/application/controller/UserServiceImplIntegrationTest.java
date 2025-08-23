package se.digg.application.controller;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import se.digg.application.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserServiceImplIntegrationTest
{
	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp()
	{
		RestAssured.port = port;
		RestAssured.basePath = "/digg/user";
	}

	@Test
	void testGetAllUsers()
	{
		given()
			.when()
			.get()
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("size()", greaterThan(0));
	}

	@Test
	void testCreateAndGetUser()
	{
		User newUser = new User("Test User", "Test Address 123", "test@example.com", "070-0001100");

		// Create user
		int userId = given()
			.contentType(ContentType.JSON)
			.body(newUser)
			.when()
			.post()
			.then()
			.statusCode(201)
			.body("name", equalTo("Test User"))
			.body("email", equalTo("test@example.com"))
			.extract()
			.path("id");

		// Get created user
		given()
			.when()
			.get("/{id}", userId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Test User"))
			.body("address", equalTo("Test Address 123"))
			.body("email", equalTo("test@example.com"))
			.body("telephone", equalTo("070-0001100"));
	}

	@Test
	void testPaginatedEndpoint()
	{
		given()
			.param("page", 0)
			.param("size", 5)
			.when()
			.get("/paginated")
			.then()
			.statusCode(200)
			.body("users.size()", lessThanOrEqualTo(5))
			.body("currentPage", equalTo(0))
			.body("totalElements", greaterThan(0));
	}

	@Test
	void testUpdateUser()
	{
		// Create user
		User newUser = new User("Original Name", "Original Address", "original@example.com", "000-0000000");

		int userId = given()
			.contentType(ContentType.JSON)
			.body(newUser)
			.when()
			.post()
			.then()
			.statusCode(201)
			.extract()
			.path("id");

		// Update the user
		User updatedUser = new User("Updated Name", "Updated Address", "updated@example.com", "111-1111111");

		given()
			.contentType(ContentType.JSON)
			.body(updatedUser)
			.when()
			.put("/{id}", userId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Updated Name"))
			.body("address", equalTo("Updated Address"))
			.body("email", equalTo("updated@example.com"))
			.body("telephone", equalTo("111-1111111"))
		;
	}

	@Test
	void testDeleteUser()
	{
		// Create user
		User newUser = new User("To Delete", "Delete Address", "delete@example.com", "070-DELETE");

		int userId = given()
			.contentType(ContentType.JSON)
			.body(newUser)
			.when()
			.post()
			.then()
			.statusCode(201)
			.extract()
			.path("id");

		// Delete the user
		given()
			.when()
			.delete("/{id}", userId)
			.then()
			.statusCode(204);

		// Verify user is deleted
		given()
			.when()
			.get("/{id}", userId)
			.then()
			.statusCode(404);
	}

	@Test
	void testHealthEndpoint()
	{
		RestAssured.basePath = "";

		given()
			.when()
			.get("/health")
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"));
	}

	@Test
	void testCreateUserWithInvalidData()
	{
		User invalidUser = new User("", "", "invalid-email", "");

		given()
			.contentType(ContentType.JSON)
			.body(invalidUser)
			.when()
			.post()
			.then()
			.statusCode(400);
	}
}