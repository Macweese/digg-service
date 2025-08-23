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
import java.util.UUID;
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
import se.digg.application.model.User;
import se.digg.application.service.UserService;

@Slf4j
@RestController
@RequestMapping("/digg/user")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "User", description = "User management API")
public class UserController
{
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private UserService userService;

	@Operation(summary = "Get all users", description = "Retrieve all users from the system")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers()
	{
		log.info("REST call: GET /digg/user");
		List<User> users = userService.getAllUsers();
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
		return ResponseEntity.ok(users);
	}

	@Operation(summary = "Get users with pagination", description = "Retrieve users with pagination support")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
		@ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
	})
	@GetMapping("/paginated")
	public ResponseEntity<Map<String, Object>> getUsersPaginated(
		@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size)
	{

		log.info("REST call: GET /digg/user/paginated?page={}&size={}", page, size);

		if (page < 0 || size <= 0)
		{
			return ResponseEntity.badRequest().build();
		}

		List<User> users = userService.getUsersPaginated(page, size);
		int totalCount = userService.getTotalCount();
		int totalPages = (int) Math.ceil((double) totalCount / size);

		Map<String, Object> response = new HashMap<>();
		response.put("users", users);
		response.put("currentPage", page);
		response.put("totalPages", totalPages);
		response.put("totalElements", totalCount);
		response.put("hasNext", page < totalPages - 1);
		response.put("hasPrevious", page > 0);
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Create a new user", description = "Add a new user to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "User created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	@PostMapping
	public ResponseEntity<User> createUser(@Valid @RequestBody User user)
	{
		log.info("REST call: POST /digg/user with data: {}", user);
		User createdUser = userService.createUser(user);
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

	@Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User found"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@Parameter(description = "User ID") @PathVariable UUID id)
	{
		log.info("REST call: GET /digg/user/{}", id);
		Optional<User> user = userService.getUserById(id);
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
		return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Update user", description = "Update an existing user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User updated successfully"),
		@ApiResponse(responseCode = "404", description = "User not found"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(
		@Parameter(description = "User ID") @PathVariable UUID id,
		@Valid @RequestBody User user)
	{
		log.info("REST call: PUT /digg/user/{} with data: {}", id, user);

		Optional<User> updatedUser = userService.updateUser(id, user);
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
		return updatedUser.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Delete user", description = "Delete a user from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "User deleted successfully"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable UUID id)
	{
		log.info("REST call: DELETE /digg/user/{}", id);
		boolean deleted = userService.deleteUser(id);
		messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
		return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
}