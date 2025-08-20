package se.digg.application.controller;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import se.digg.application.model.Customer;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CustomerServiceIntegrationTest
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
	void testGetAllCustomers()
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
	void testCreateAndGetCustomer()
	{
		Customer newCustomer = new Customer("Test Customer", "Test Address 123", "test@example.com", "070-0001100");

		// Create customer
		int customerId = given()
			.contentType(ContentType.JSON)
			.body(newCustomer)
			.when()
			.post()
			.then()
			.statusCode(201)
			.body("name", equalTo("Test Customer"))
			.body("email", equalTo("test@example.com"))
			.extract()
			.path("id");

		// Get created customer
		given()
			.when()
			.get("/{id}", customerId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Test Customer"))
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
			.body("customers.size()", lessThanOrEqualTo(5))
			.body("currentPage", equalTo(0))
			.body("totalElements", greaterThan(0));
	}

	@Test
	void testUpdateCustomer()
	{
		// Create customer
		Customer newCustomer = new Customer("Original Name", "Original Address", "original@example.com", "000-0000000");

		int customerId = given()
			.contentType(ContentType.JSON)
			.body(newCustomer)
			.when()
			.post()
			.then()
			.statusCode(201)
			.extract()
			.path("id");

		// Update the customer
		Customer updatedCustomer = new Customer("Updated Name", "Updated Address", "updated@example.com", "111-1111111");

		given()
			.contentType(ContentType.JSON)
			.body(updatedCustomer)
			.when()
			.put("/{id}", customerId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Updated Name"))
			.body("address", equalTo("Updated Address"))
			.body("email", equalTo("updated@example.com"))
			.body("telephone", equalTo("111-1111111"))
		;
	}

	@Test
	void testDeleteCustomer()
	{
		// Create customer
		Customer newCustomer = new Customer("To Delete", "Delete Address", "delete@example.com", "070-DELETE");

		int customerId = given()
			.contentType(ContentType.JSON)
			.body(newCustomer)
			.when()
			.post()
			.then()
			.statusCode(201)
			.extract()
			.path("id");

		// Delete the customer
		given()
			.when()
			.delete("/{id}", customerId)
			.then()
			.statusCode(204);

		// Verify customer is deleted
		given()
			.when()
			.get("/{id}", customerId)
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
	void testCreateCustomerWithInvalidData()
	{
		Customer invalidCustomer = new Customer("", "", "invalid-email", "");

		given()
			.contentType(ContentType.JSON)
			.body(invalidCustomer)
			.when()
			.post()
			.then()
			.statusCode(400);
	}
}