package se.digg.application.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import se.digg.application.model.Customer;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerService
{
	private final Map<Long, Customer> customers = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	public CustomerService()
	{
		// Populate with some dummy data ~~ for demo + testing
		generateDummyData();
	}

	public List<Customer> getAllCustomers()
	{
		log.info("Fetching all customers. Total count: {}", customers.size());
		return new ArrayList<>(customers.values());
	}

	public List<Customer> getCustomersPaginated(int page, int size)
	{
		log.info("Fetching customers - page: {}, size: {}", page, size);

		List<Customer> allCustomers = getAllCustomers();
		int start = page * size;
		int end = Math.min(start + size, allCustomers.size());

		if (start >= allCustomers.size())
		{
			return new ArrayList<>();
		}

		return allCustomers.subList(start, end);
	}

	public Customer createCustomer(Customer customer)
	{
		Long id = idGenerator.getAndIncrement();
		customer.setId(id);
		customers.put(id, customer);
		log.info("Created new customer with ID: {}", id);
		return customer;
	}

	public Optional<Customer> getCustomerById(Long id)
	{
		log.info("Fetching customer with ID: {}", id);
		return Optional.ofNullable(customers.get(id));
	}

	public Optional<Customer> updateCustomer(Long id, Customer customerUpdate)
	{
		Customer existingCustomer = customers.get(id);
		if (existingCustomer != null)
		{
			customerUpdate.setId(id);
			customers.put(id, customerUpdate);
			log.info("Updated customer with ID: {}", id);
			return Optional.of(customerUpdate);
		}
		log.warn("Customer with ID {} not found for update", id);
		return Optional.empty();
	}

	public boolean deleteCustomer(Long id)
	{
		Customer removed = customers.remove(id);
		if (removed != null)
		{
			log.info("Deleted customer with ID: {}", id);
			return true;
		}
		log.warn("Customer with ID {} not found for deletion", id);
		return false;
	}

	public int getTotalCount()
	{
		return customers.size();
	}

	private void generateDummyData()
	{
		final String[] firstNames = {"Alice", "Bob", "Kalle", "Knatte", "Fnatte", "Tjatte", "Annika", "Amanda", "Frida",
			"Alex", "Sofia", "Emma", "Oliver", "Ida", "Hugo", "Elsa", "Tobias", "Ben", "Lucas", "Liam", "Klara",
			"Noah", "Wilma", "Adam", "Vera", "Emil", "Agnes", "Oscar", "Måns", "François", "Søren", "Jean-Pierre"};

		final String[] lastNames = {"Anka", "Ludd", "Pigg", "Andersson", "Johansson", "Karlsson", "Nilsson", "Eriksson", "Larsson",
			"Olsson", "Persson", "Svensson", "Gustafsson", "Pettersson", "Jonsson", "Jansson", "Hansson",
			"Bengtsson", "Jönsson", "Lindberg", "Frantzén", "Ziębównski", "O'Connor", "Müller"};

		final String[] streets = {"Sveavägen", "Fiskvägen", "Odengatan", "S:t Eriksgatan", "Skogsbacken", "Tre Kronors väg", "Hamngatan", "Lugnets Allé", "Mjölnarvägen"};

		final String[] cities = {"Stockholm", "Göteborg", "Malmö", "Uppsala", "Västerås", "Örebro", "Linköping", "Helsingborg", "Jönköping", "Norrköping"};

		Random random = new Random();
		// Simple regex for disallowed characters in local email prefix
		final String emailAddressCharRegex = "\\p{M}|[^\\w-.]";

		for (int i = 0; i < 50; i++)
		{
			String firstName = firstNames[random.nextInt(firstNames.length)];
			String lastName = lastNames[random.nextInt(lastNames.length)];
			String name = firstName + " " + lastName;

			String street = streets[random.nextInt(streets.length)];
			int streetNumber = random.nextInt(99) + 1;
			String postalCode = String.format("%d", 100_00 + random.nextInt(900_00));
			String city = cities[random.nextInt(cities.length)];
			String address = street + " " + streetNumber + ", " + postalCode + " " + city;

			// Sanitize names for address' local prefix
			String email = Normalizer.normalize(firstName.toLowerCase() + "." + lastName.toLowerCase(), Normalizer.Form.NFD)
				.replaceAll(emailAddressCharRegex, "")+ "@example.com";
			String telephone = "070" + " xxx xx xx"/* +  String.format("%d", 100_00_00 + random.nextInt(900_00_00))*/;

			createCustomer(new Customer(name, address, email, telephone));
		}

		log.info("Generated {} dummy data", customers.size());
	}
}