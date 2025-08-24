package se.digg.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import se.digg.application.events.UserEvent;
import se.digg.application.model.User;
import se.digg.application.service.UserServiceImpl;

@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserServiceImpl userServiceImpl;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	private User sampleUser;

	@Captor
	private ArgumentCaptor<Object> payloadCaptor;

	@BeforeEach
	void setUp() {
		sampleUser = new User();
		sampleUser.setId(1L);
		sampleUser.setName("Alice");
		sampleUser.setEmail("alice@example.com");
		sampleUser.setTelephone("123456");
		sampleUser.setAddress("Bellmans Gränd 17, 11717 Stockholm");
	}

	@Test
	public void testGetAllUsers() throws Exception {
		// given
		List<User> users = Arrays.asList(
			new User("Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100"),
			new User("Kalle Anka", "Vägen 31, 67422 Staden", "kalle@acme.org", "070-0702200")
		);

		when(userServiceImpl.getAllUsers()).thenReturn(users);

		// when + then
		mockMvc.perform(get("/digg/user"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$[0].name").value("Kajsa Anka"))
			.andExpect(jsonPath("$[0].address").value("Vägen 13, 67421 Staden"))
			.andExpect(jsonPath("$[0].email").value("kajsa@acme.org"))
			.andExpect(jsonPath("$[0].telephone").value("070-0701100"))
			.andExpect(jsonPath("$[1].name").value("Kalle Anka"))
			.andExpect(jsonPath("$[1].address").value("Vägen 31, 67422 Staden"))
			.andExpect(jsonPath("$[1].email").value("kalle@acme.org"))
			.andExpect(jsonPath("$[1].telephone").value("070-0702200"));

		verify(userServiceImpl).getAllUsers();
	}

	@Test
	public void testCreateUser() throws Exception {
		// given
		User inputUser = new User("Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");
		User savedUser = new User(2L, "Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");

		when(userServiceImpl.createUser(any(User.class))).thenReturn(savedUser);

		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(inputUser)))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/digg/user/2"))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(2L))
			.andExpect(jsonPath("$.name").value("Ludde Luddson"))
			.andExpect(jsonPath("$.address").value("Hittepåvägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("ludde@ludd.org"))
			.andExpect(jsonPath("$.telephone").value("070-0001100"));

		verify(userServiceImpl).createUser(any(User.class));
	}

	@Test
	public void testGetUserById() throws Exception {
		User user = new User(2L, "Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100");
		when(userServiceImpl.getUserById(2L)).thenReturn(Optional.of(user));

		mockMvc.perform(get("/digg/user/2"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.name").value("Kajsa Anka"))
			.andExpect(jsonPath("$.address").value("Vägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("kajsa@acme.org"))
			.andExpect(jsonPath("$.telephone").value("070-0701100"));

		verify(userServiceImpl).getUserById(2L);
	}

	@Test
	public void testGetUserByIdNotFound() throws Exception {
		when(userServiceImpl.getUserById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/digg/user/999"))
			.andExpect(status().isNotFound());

		verify(userServiceImpl).getUserById(999L);
	}

	@Test
	public void testUpdateUser() throws Exception {
		// given
		Long id = 1L;
		User updatedUser = new User(id, "Updated Name", "Updated Address", "updated@email.com", "070-0001100");
		when(userServiceImpl.updateUser(eq(id), any(User.class))).thenReturn(Optional.of(updatedUser));

		mockMvc.perform(put("/digg/user/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedUser)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Updated Name"));

		verify(userServiceImpl).updateUser(eq(id), any(User.class));
	}

	@Test
	public void testDeleteUser() throws Exception {
		when(userServiceImpl.deleteUser(1L)).thenReturn(true);

		mockMvc.perform(delete("/digg/user/1"))
			.andExpect(status().isNoContent());

		verify(userServiceImpl).deleteUser(1L);
	}

	@Test
	public void testDeleteUserNotFound() throws Exception {
		when(userServiceImpl.deleteUser(1L)).thenReturn(false);

		mockMvc.perform(delete("/digg/user/1"))
			.andExpect(status().isNotFound());

		verify(userServiceImpl).deleteUser(1L);
	}

	@Test
	public void testCreateUserWithValidationError() throws Exception {
		// invalid payload (expects validation annotations on User)
		User invalidUser = new User("", "Address", "invalid-email", "070-0001100");

		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidUser)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void getUsers_paged_ok() throws Exception {
		when(userServiceImpl.getUsers(PageRequest.of(0, 10)))
			.thenReturn(new PageImpl<>(List.of(sampleUser), PageRequest.of(0, 10), 1));

		mockMvc.perform(MockMvcRequestBuilders.get("/digg/user/0/10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", is(notNullValue())));
	}

	@Test
	void queryUsers_paged_ok() throws Exception {
		when(userServiceImpl.queryUsers(eq("alice"), any()))
			.thenReturn(new PageImpl<>(List.of(sampleUser), PageRequest.of(0, 10), 1));

		mockMvc.perform(MockMvcRequestBuilders.get("/digg/user/0/10/search/alice"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", is(notNullValue())));
	}

	@Test
	void getUserById_ok_whenExists() throws Exception {
		when(userServiceImpl.getUserById(1L)).thenReturn(Optional.of(sampleUser));

		mockMvc.perform(MockMvcRequestBuilders.get("/digg/user/1"))
			.andExpect(status().isOk());
	}

	@Test
	void createUser_emitsEvent_andReturns201() throws Exception {
		when(userServiceImpl.createUser(any(User.class))).thenReturn(sampleUser);

		mockMvc.perform(MockMvcRequestBuilders.post("/digg/user/add")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleUser)))
			.andExpect(status().isCreated());

		verify(messagingTemplate).convertAndSend(eq("/topic/users"), payloadCaptor.capture());
		assertEventPayload(payloadCaptor.getValue(), UserEvent.ADD.name());
	}

	@Test
	void updateUser_emitsEvent_andReturns200() throws Exception {
		when(userServiceImpl.updateUser(eq(1L), any(User.class)))
			.thenReturn(Optional.of(sampleUser));

		mockMvc.perform(MockMvcRequestBuilders.put("/digg/user/edit/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(sampleUser)))
			.andExpect(status().isOk());

		verify(messagingTemplate).convertAndSend(eq("/topic/users"), payloadCaptor.capture());
		assertEventPayload(payloadCaptor.getValue(), UserEvent.EDIT.name());
	}

	@Test
	void deleteUser_emitsEvent_andReturns2xx() throws Exception {
		when(userServiceImpl.deleteUser(1L)).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.delete("/digg/user/1"))
			.andExpect(status().isNoContent());

		verify(messagingTemplate).convertAndSend(eq("/topic/users"), payloadCaptor.capture());
		assertEventPayload(payloadCaptor.getValue(), UserEvent.DELETE.name());
	}

	@Test
	void cors_preflight_allowsFrontendOrigin() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.options("/digg/user/1")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "DELETE"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}

	@SuppressWarnings("unchecked")
	private void assertEventPayload(Object payload, String expectedEvent) {
		if (payload instanceof Map<?, ?> map) {
			Object eventVal = map.get("event");
			if (eventVal == null) {
				throw new AssertionError("Missing 'event' key in payload map");
			}
			if (eventVal instanceof Enum<?> e) {
				if (!expectedEvent.equals(e.name())) {
					throw new AssertionError("Expected enum event " + expectedEvent + " but got " + e.name());
				}
			} else if (eventVal instanceof String s) {
				if (!expectedEvent.equals(s)) {
					throw new AssertionError("Expected string event " + expectedEvent + " but got " + s);
				}
			} else {
				throw new AssertionError("Unsupported event value type: " + eventVal.getClass());
			}
		} else {
			throw new AssertionError("Payload was not a Map. Got: " + (payload == null ? "null" : payload.getClass()));
		}
	}
}