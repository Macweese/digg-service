package se.digg.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
import se.digg.application.api.PageResponse;
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

	// Paged list: numeric regex avoids collisions with {id}
	@GetMapping("/{page:\\d+}/{size:\\d+}")
	@Operation(summary = "Retrieve paged users", description = "Get all users with pagination")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<PageResponse<User>> getUsers(
		@PathVariable int page,
		@PathVariable int size)
	{
		log.info("REST call: GET /digg/user/{}/{}", page, size);
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userServiceImpl.getUsers(pageable);
		return ResponseEntity.ok(PageResponse.fromPage(userPage));
	}

	// Paged search
	@GetMapping("/{page:\\d+}/{size:\\d+}/search/{query}")
	@Operation(summary = "Retrieve paged search", description = "Search users with a pagination")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<PageResponse<User>> queryUsers(
		@PathVariable int page,
		@PathVariable int size,
		@PathVariable String query)
	{
		log.info("REST call: GET /digg/user/{}/{}/search/{}", page, size, query);
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userServiceImpl.queryUsers(query, pageable);
		return ResponseEntity.ok(PageResponse.fromPage(userPage));
	}

	// Unpaged, full list
	@GetMapping
	@Operation(summary = "Get all users", description = "Retrieve ALL users, un-paged")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved users")
	public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String query)
	{
		return ResponseEntity.ok(userServiceImpl.getAllUsers());
	}

	// Get by id (numeric)
	@GetMapping("/{id:\\d+}")
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

	// POST upsert (primary): create → 201 + Location; update → 200, 404 if id not found
	@PostMapping("")
	@Operation(summary = "Create or update user", description = "Create (no id) or update (with id) a user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "User created successfully"),
		@ApiResponse(responseCode = "200", description = "User updated successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid user data"),
		@ApiResponse(responseCode = "404", description = "User not found for update")
	})
	public ResponseEntity<User> saveUserUpsert(@Valid @RequestBody User user)
	{
		if (user.getId() == null)
		{
			log.info("REST call: POST /digg/user (create) with data: {}", user);
			User created = userServiceImpl.createUser(user);
			messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.ADD));
			URI location = URI.create("/digg/user/" + created.getId());
			return ResponseEntity.created(location).header(HttpHeaders.LOCATION, location.toString()).body(created);
		}
		else
		{
			log.info("REST call: POST /digg/user (update) with data: {}", user);
			Optional<User> updated = userServiceImpl.updateUser(user.getId(), user);
			if (updated.isPresent())
			{
				messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.EDIT));
				return ResponseEntity.ok(updated.get());
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	// Keep legacy create endpoint: /add (returns Location too)
	@PostMapping("/add")
	@Operation(summary = "Create a new user", description = "Add a new user to the storage")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "User created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	public ResponseEntity<User> createUser(@Valid @RequestBody User user)
	{
		log.info("REST call: POST /digg/user/add with data: {}", user);
		User createdUser = userServiceImpl.createUser(user);
		messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.ADD));
		URI location = URI.create("/digg/user/" + createdUser.getId());
		return ResponseEntity.created(location).header(HttpHeaders.LOCATION, location.toString()).body(createdUser);
	}

	// Update aliases: /edit/{id} and /{id}
	@PutMapping("/edit/{id:\\d+}")
	@Operation(summary = "Update user", description = "Update an existing user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User updated successfully"),
		@ApiResponse(responseCode = "404", description = "User not found"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	public ResponseEntity<User> updateUserLegacy(@Parameter(description = "User ID") @PathVariable Long id,
												 @Valid @RequestBody User user)
	{
		return doUpdate(id, user);
	}

	@PutMapping("/{id:\\d+}")
	@Operation(summary = "Update user", description = "Update an existing user")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User updated successfully"),
		@ApiResponse(responseCode = "404", description = "User not found"),
		@ApiResponse(responseCode = "400", description = "Invalid user data")
	})
	public ResponseEntity<User> updateUser(@Parameter(description = "User ID") @PathVariable Long id,
										   @Valid @RequestBody User user)
	{
		return doUpdate(id, user);
	}

	private ResponseEntity<User> doUpdate(Long id, User user)
	{
		log.info("REST call: PUT /digg/user/{} with data: {}", id, user);
		Optional<User> updatedUser = userServiceImpl.updateUser(id, user);
		if (updatedUser.isPresent())
		{
			messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.EDIT));
			return ResponseEntity.ok(updatedUser.get());
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	// DELETE returns 204 on success, 404 on missing; emits event only when deleted
	@DeleteMapping("/{id:\\d+}")
	@Operation(summary = "Delete user", description = "Delete a user from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "User deleted successfully"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<Void> deleteUser(@PathVariable Long id)
	{
		log.info("REST call: DELETE /digg/user/{}", id);
		boolean deleted = userServiceImpl.deleteUser(id);
		if (deleted)
		{
			messagingTemplate.convertAndSend("/topic/users", Map.of("event", UserEvent.DELETE));
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
}