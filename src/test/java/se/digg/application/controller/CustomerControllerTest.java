package se.digg.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import se.digg.application.model.Customer;
import se.digg.application.service.CustomerService;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CustomerService customerService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetAllCustomers() throws Exception
	{
		// given
		List<Customer> customers = Arrays.asList(
			new Customer(1L, "Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100"),
			new Customer(2L, "Kalle Anka", "Vägen 31, 67422 Staden", "kalle@acme.org", "070-0702200")
		);

		when(customerService.getAllCustomers()).thenReturn(customers);

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

		verify(customerService).getAllCustomers();
	}

	@Test
	public void testCreateCustomer() throws Exception
	{
		// givem
		Customer inputCustomer = new Customer("Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");
		Customer savedCustomer = new Customer(1L, "Ludde Luddson", "Hittepåvägen 13, 67421 Staden", "ludde@ludd.org", "070-0001100");

		when(customerService.createCustomer(any(Customer.class))).thenReturn(savedCustomer);

		// when + then
		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(inputCustomer)))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.name").value("Ludde Luddson"))
			.andExpect(jsonPath("$.address").value("Hittepåvägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("ludde@ludd.org"))
			.andExpect(jsonPath("$.telephone").value("070-0001100"));

		verify(customerService).createCustomer(any(Customer.class));
	}

	@Test
	public void testGetCustomerById() throws Exception
	{
		// given
		Customer customer = new Customer(1L, "Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100");

		when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));

		// when + then
		mockMvc.perform(get("/digg/user/1"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.name").value("Kajsa Anka"))
			.andExpect(jsonPath("$.address").value("Vägen 13, 67421 Staden"))
			.andExpect(jsonPath("$.email").value("kajsa@acme.org"))
			.andExpect(jsonPath("$.telephone").value("070-0701100"));

		verify(customerService).getCustomerById(1L);
	}

	@Test
	public void testGetCustomerByIdNotFound() throws Exception
	{
		// given
		when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

		// when + then
		mockMvc.perform(get("/digg/user/999"))
			.andExpect(status().isNotFound());

		verify(customerService).getCustomerById(999L);
	}

	@Test
	public void testUpdateCustomer() throws Exception
	{
		// given
		Customer updatedCustomer = new Customer(1L, "Updated Name", "Updated Address", "updated@email.com", "070-0001100");

		when(customerService.updateCustomer(anyLong(), any(Customer.class))).thenReturn(Optional.of(updatedCustomer));

		// when + then
		mockMvc.perform(put("/digg/user/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedCustomer)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Updated Name"));

		verify(customerService).updateCustomer(anyLong(), any(Customer.class));
	}

	@Test
	public void testDeleteCustomer() throws Exception
	{
		// given
		when(customerService.deleteCustomer(1L)).thenReturn(true);

		// when + then
		mockMvc.perform(delete("/digg/user/1"))
			.andExpect(status().isNoContent());

		verify(customerService).deleteCustomer(1L);
	}

	@Test
	public void testDeleteCustomerNotFound() throws Exception
	{
		// given
		when(customerService.deleteCustomer(999L)).thenReturn(false);

		// when + then
		mockMvc.perform(delete("/digg/user/999"))
			.andExpect(status().isNotFound());

		verify(customerService).deleteCustomer(999L);
	}

	@Test
	public void testCreateCustomerWithValidationError() throws Exception
	{
		// given - Customer with invalid data (empty name)
		Customer invalidCustomer = new Customer("", "Address", "invalid-email", "070-0001100");

		// when + then
		mockMvc.perform(post("/digg/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidCustomer)))
			.andExpect(status().isBadRequest());

		verify(customerService, never()).createCustomer(any(Customer.class));
	}
}