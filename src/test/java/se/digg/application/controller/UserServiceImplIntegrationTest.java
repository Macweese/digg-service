/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.controller;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import io.restassured.http.ContentType;
import java.util.Map;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.context.annotation.Import(se.digg.application.config.TestSecurityConfig.class)
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

	@Test
	void testSearchReturnsCreatedUser()
	{
		// Create a unique user
		String marker = "UniqueZed_" + System.currentTimeMillis();
		Long id =
			given()
				.contentType(ContentType.JSON)
				.body(Map.of(
					"name", marker,
					"address", "Somewhere 1",
					"email", marker.toLowerCase() + "@example.com",
					"telephone", "999-888"
				))
				.when()
				.post("")
				.then()
				.statusCode(201)
				.extract().jsonPath().getLong("id");

		// Search by part of the unique name
		given()
			.when()
			.get("/0/10/search/" + marker.substring(0, 6))
			.then()
			.statusCode(200)
			.body("totalElements", greaterThanOrEqualTo(1))
			.body("content.find { it.id == %s }.name", withArgs(id), equalTo(marker));
	}

	@Test
	void testPostUpsertUpdateNotFoundReturns404()
	{
		given()
			.contentType(ContentType.JSON)
			.body(Map.of(
				"id", 9_999_999,
				"name", "Ghost",
				"address", "Nowhere",
				"email", "ghost@example.com",
				"telephone", "000"
			))
			.when()
			.post("")
			.then()
			.statusCode(404);
	}

	@Test
	void testLegacyAddAndEditPathsWork()
	{
		// Create via /add
		Long id =
			given()
				.contentType(ContentType.JSON)
				.body(Map.of(
					"name", "Legacy Add",
					"address", "Road 99",
					"email", "legacy_add_" + System.currentTimeMillis() + "@example.com",
					"telephone", "111"
				))
				.when()
				.post("/add")
				.then()
				.statusCode(201)
				.body("id", notNullValue())
				.extract().jsonPath().getLong("id");

		// Update via /edit/{id}
		given()
			.contentType(ContentType.JSON)
			.body(Map.of(
				"id", id,
				"name", "Legacy Edit Updated",
				"address", "Road 100",
				"email", "legacy_add_" + System.currentTimeMillis() + "@example.com",
				"telephone", "222"
			))
			.when()
			.put("/edit/" + id)
			.then()
			.statusCode(200)
			.body("name", is("Legacy Edit Updated"));
	}

	@Test
	void testCorsPreflightPostOk()
	{
		given()
			.header("Origin", "http://localhost:5173")
			.header("Access-Control-Request-Method", "POST")
			.when()
			.options("")
			.then()
			.statusCode(200)
			.header("Access-Control-Allow-Origin", is("http://localhost:5173"));
	}
}