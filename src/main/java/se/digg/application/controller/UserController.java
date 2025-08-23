package se.digg.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import se.digg.application.events.UserEvent;
import se.digg.application.model.User;
import se.digg.application.service.UserServiceImpl;

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
	private UserServiceImpl userServiceImpl;

	public UserController(UserServiceImpl userServiceImpl)
	{
		this.userServiceImpl = userServiceImpl;
	}

	@GetMapping("/{page}/{size}")
	@Operation(summary = "Retrieve paged users", description = "Get all users with pagination")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<Page<User>> getUsers(
		@PathVariable int page,
		@PathVariable int size)
	{
		log.info("REST call: GET /digg/user/{}/{}", page, size);
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userServiceImpl.getUsers(pageable);

		return ResponseEntity.ok(userPage);
	}

	@GetMapping("/{page}/{size}/search/{query}")
	@Operation(summary = "Retrieve paged search", description = "Search users with a pagination")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<Page<User>> queryUsers(
		@PathVariable int page,
		@PathVariable int size,
		@PathVariable String query)
	{
		log.info("REST call: POST /digg/user/{}/{}/search/{}", page, size, query);
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userServiceImpl.queryUsers(query, pageable);

		return ResponseEntity.ok(userPage);
	}

	@GetMapping
	@Operation(summary = "Get all users", description = "Retrieve ALL users, un-paged")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<List<User>> getAllUsers(
		@RequestParam(required = false) String query)
	{
		return ResponseEntity.ok(userServiceImpl.getAllUsers());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User found"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<User> getUserById(@Parameter(description = "User ID") @PathVariable Long id)
	{
		log.info("REST call: GET /digg/user/{}", id);
		Optional<User> user = userServiceImpl.getUserById(id);

		return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/add")
	@Operation(summary = "Create a new user", description = "Add a new user to the storage")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "User created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	public ResponseEntity<User> createUser(@Valid @RequestBody User user)
	{
		log.info("REST call: POST /digg/user with data: {}", user);
		User createdUser = userServiceImpl.createUser(user);

		messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.ADD));
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}

	@PutMapping("/edit/{id}")
	@Operation(summary = "Update user", description = "Update an existing user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User updated successfully"),
		@ApiResponse(responseCode = "404", description = "User not found"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	public User updateUser(@Parameter(description = "User ID") @PathVariable Long id, @Valid @RequestBody User user)
	{
		log.info("REST call: PUT /digg/user/{} with data: {}", id, user);

		Optional<User> updatedUser = userServiceImpl.updateUser(id, user);
		if (updatedUser.isPresent())
		{
			messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.EDIT));
			return updatedUser.get();
		}
		else
		{
			return null;
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete user", description = "Delete a user from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "User deleted successfully"),
		@ApiResponse(responseCode = "404", description = "User not found")})
	public void deleteUser(@PathVariable Long id)
	{
		log.info("REST call: DELETE /digg/user/{}", id);
		userServiceImpl.deleteUser(id);
		messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.DELETE));
	}
}