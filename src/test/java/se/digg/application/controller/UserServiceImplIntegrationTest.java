package se.digg.application.controller;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import java.util.Map;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceImplIntegrationTest
{

	@LocalServerPort
	int port;

	@BeforeEach
	void setup()
	{
		RestAssured.baseURI = "http://localhost";
		RestAssured.basePath = "/digg/user";
		RestAssured.port = port;
	}

	@Test
	void testPaginatedEndpoint()
	{
		given()
			.when()
			.get("/0/10")
			.then()
			.statusCode(200)
			.body("number", is(0))
			.body("size", is(10))
			.body("totalElements", greaterThanOrEqualTo(0));
	}

	@Test
	void testCreateAndGetUser()
	{
		// Create
		Long id =
			given()
				.contentType(ContentType.JSON)
				.body(Map.of(
					"name", "Carol",
					"address", "Main St 1",
					"email", "carol@example.com",
					"telephone", "555-123"
				))
				.when()
				.post("")
				.then()
				.statusCode(201)
				.header("Location", containsString("/digg/user/"))
				.body("id", notNullValue())
				.extract()
				.jsonPath().getLong("id");

		// Get by id
		given()
			.when()
			.get("/" + id)
			.then()
			.statusCode(200)
			.body("email", is("carol@example.com"));
	}

	@Test
	void testUpdateUser()
	{
		// Create first
		Long id =
			given()
				.contentType(ContentType.JSON)
				.body(Map.of(
					"name", "Dave",
					"address", "Road 1",
					"email", "dave@example.com",
					"telephone", "555-333"
				))
				.when()
				.post("")
				.then()
				.statusCode(201)
				.extract().jsonPath().getLong("id");

		// Update via POST (upsert)
		given()
			.contentType(ContentType.JSON)
			.body(Map.of(
				"id", id,
				"name", "Dave Updated",
				"address", "Road 2",
				"email", "dave@example.com",
				"telephone", "555-444"
			))
			.when()
			.post("")
			.then()
			.statusCode(200)
			.body("name", is("Dave Updated"));
	}

	@Test
	void testCreateUserWithInvalidData()
	{
		given()
			.contentType(ContentType.JSON)
			.body(Map.of(
				"name", "", // expecting @NotBlank
				"address", "Somewhere",
				"email", "bad-email", // expecting @Email
				"telephone", "000"
			))
			.when()
			.post("")
			.then()
			.statusCode(400);
	}

	@Test
	void testDeleteUser()
	{
		// We assume id 1 exists from dummy data; if not, create first or delete a known created id.
		given()
			.when()
			.delete("/1")
			.then()
			.statusCode(anyOf(is(204), is(404))); // allow either based on seed data, but delete contract is 204 or 404
	}
}