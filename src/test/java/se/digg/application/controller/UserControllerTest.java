package se.digg.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import se.digg.application.model.User;
import se.digg.application.service.UserService;

@WebMvcTest(UserController.class)
public class UserControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetAllUsers() throws Exception
	{
		// given
		List<User> users = Arrays.asList(
			new User("Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100"),
			new User("Kalle Anka", "Vägen 31, 67422 Staden", "kalle@acme.org", "070-0702200")
		);

		when(userService.getAllUsers()).thenReturn(users);

		// when + then
		mockMvc.perform(get("/digg/user"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			// verify data for user id=1
			.andExpect(jsonPath("$[0].name").value("Kajsa Anka"))
			.andExpect(jsonPath("$[0].address").value("Vägen 13, 67421 Staden"))
			.andExpect(jsonPath("$[0].email").value("kajsa@acme.org"))
			.andExpect(jsonPath("$[0].telephone").value("070-0701100"))
			// verify data for user id=2
			.andExpect(jsonPath("$[1].name").value("Kalle Anka"))
			.andExpect(jsonPath("$[1].address").value("Vägen 31, 67422 Staden"))
			.andExpect(jsonPath("$[1].email").value("kalle@acme.org"))
			.andExpect(jsonPath("$[1].telephone").value("070-0702200"));

		verify(userService).getAllUsers();
	}

	@Test
	public void testCreateUser() throws Exception
	{
		// givem
		User inputUser = new User("Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");
		User savedUser = new User(UUID.randomUUID(), "Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");

		when(userService.createUser(any(User.class))).thenReturn(savedUser);

		// when + then
		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(inputUser)))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(inputUser.getId()))
			.andExpect(jsonPath("$.name").value("Ludde Luddson"))
			.andExpect(jsonPath("$.address").value("Hittepåvägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("ludde@ludd.org"))
			.andExpect(jsonPath("$.telephone").value("070-0001100"));

		verify(userService).createUser(any(User.class));
	}

	@Test
	public void testGetUserById() throws Exception
	{
		// given
		UUID id = UUID.randomUUID();
		User user = new User(id, "Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100");

		when(userService.getUserById(id)).thenReturn(Optional.of(user));

		// when + then
		mockMvc.perform(get("/digg/user/" + id))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.name").value("Kajsa Anka"))
			.andExpect(jsonPath("$.address").value("Vägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("kajsa@acme.org"))
			.andExpect(jsonPath("$.telephone").value("070-0701100"));

		verify(userService).getUserById(id);
	}

	@Test
	public void testGetUserByIdNotFound() throws Exception
	{
		// given
		UUID id = UUID.randomUUID();
		when(userService.getUserById(id)).thenReturn(Optional.empty());

		// when + then
		mockMvc.perform(get("/digg/user/" + id))
			.andExpect(status().isNotFound());

		verify(userService).getUserById(id);
	}

	@Test
	public void testUpdateUser() throws Exception
	{
		// given
		User updatedUser = new User(any(UUID.class), "Updated Name", "Updated Address", "updated@email.com", "070-0001100");

		when(userService.updateUser(updatedUser.getId(), any(User.class))).thenReturn(Optional.of(updatedUser));

		// when + then
		mockMvc.perform(put("/digg/user/" + updatedUser.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedUser)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Updated Name"));

		verify(userService).updateUser(updatedUser.getId(), any(User.class));
	}

	@Test
	public void testDeleteUser() throws Exception
	{
		// given
		UUID id = UUID.randomUUID();
		when(userService.deleteUser(id)).thenReturn(true);

		// when + then
		mockMvc.perform(delete("/digg/user/" + id))
			.andExpect(status().isNoContent());

		verify(userService).deleteUser(id);
	}

	@Test
	public void testDeleteUserNotFound() throws Exception
	{
		// given
		UUID id = UUID.randomUUID();
		when(userService.deleteUser(id)).thenReturn(false);

		// when + then
		mockMvc.perform(delete("/digg/user/" + id))
			.andExpect(status().isNotFound());

		verify(userService).deleteUser(id);
	}

	@Test
	public void testCreateUserWithValidationError() throws Exception
	{
		// given - User with invalid data (empty name)
		User invalidUser = new User("", "Address", "invalid-email", "070-0001100");

		// when + then
		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidUser)))
			.andExpect(status().isBadRequest());

		verify(userService, never()).createUser(any(User.class));
	}
}