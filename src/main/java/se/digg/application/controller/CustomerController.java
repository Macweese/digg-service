package se.digg.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.digg.application.model.Customer;
import se.digg.application.service.CustomerService;

@Slf4j
@RestController
@RequestMapping("/digg/user")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Customer", description = "Customer management API")
public class CustomerController
{
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private CustomerService customerService;

	@Operation(summary = "Get all customers", description = "Retrieve all customers from the system")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
	@GetMapping
	public ResponseEntity<List<Customer>> getAllCustomers()
	{
		log.info("REST call: GET /digg/user");
		List<Customer> customers = customerService.getAllCustomers();
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());
		return ResponseEntity.ok(customers);
	}

	@Operation(summary = "Get customers with pagination", description = "Retrieve customers with pagination support")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved customers"),
		@ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
	})
	@GetMapping("/paginated")
	public ResponseEntity<Map<String, Object>> getCustomersPaginated(
		@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size)
	{

		log.info("REST call: GET /digg/user/paginated?page={}&size={}", page, size);

		if (page < 0 || size <= 0)
		{
			return ResponseEntity.badRequest().build();
		}

		List<Customer> customers = customerService.getCustomersPaginated(page, size);
		int totalCount = customerService.getTotalCount();
		int totalPages = (int) Math.ceil((double) totalCount / size);

		Map<String, Object> response = new HashMap<>();
		response.put("customers", customers);
		response.put("currentPage", page);
		response.put("totalPages", totalPages);
		response.put("totalElements", totalCount);
		response.put("hasNext", page < totalPages - 1);
		response.put("hasPrevious", page > 0);
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Create a new customer", description = "Add a new customer to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Customer created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid customer data")
	})
	@PostMapping
	public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer)
	{
		log.info("REST call: POST /digg/user with data: {}", customer);
		Customer createdCustomer = customerService.createCustomer(customer);
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
	}

	@Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Customer found"),
		@ApiResponse(responseCode = "404", description = "Customer not found")
	})
	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomerById(@Parameter(description = "Customer ID") @PathVariable Long id)
	{
		log.info("REST call: GET /digg/user/{}", id);
		Optional<Customer> customer = customerService.getCustomerById(id);
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());
		return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Update customer", description = "Update an existing customer")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Customer updated successfully"),
		@ApiResponse(responseCode = "404", description = "Customer not found"),
		@ApiResponse(responseCode = "400", description = "Invalid customer data")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(
		@Parameter(description = "Customer ID") @PathVariable Long id,
		@Valid @RequestBody Customer customer)
	{
		log.info("REST call: PUT /digg/user/{} with data: {}", id, customer);

		Optional<Customer> updatedCustomer = customerService.updateCustomer(id, customer);
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());
		return updatedCustomer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Delete customer", description = "Delete a customer from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Customer not found")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@Parameter(description = "Customer ID") @PathVariable Long id)
	{
		log.info("REST call: DELETE /digg/user/{}", id);
		boolean deleted = customerService.deleteCustomer(id);
		messagingTemplate.convertAndSend("/topic/customers", customerService.getAllCustomers());
		return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
}