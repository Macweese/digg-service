package se.digg.application.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.digg.application.model.User;

@Component
public class MockDataService
{
	private final UserService userService;

	@Autowired
	public MockDataService(UserService userService)
	{
		this.userService = userService;
	}

	@PostConstruct
	public void generateMockData()
	{
		// Example: create mock users
		// You should move the actual mock data logic from UserServiceImpl here.
		userService.createUser(new User("Kajsa Anka", "Vägen 13, 67421 Staden", "kajsa@acme.org", "070-0701100"));
		userService.createUser(new User("Kalle Anka", "Vägen 31, 67422 Staden", "kalle@acme.org", "070-0702200"));
		// Add more mock data as needed...
	}
}