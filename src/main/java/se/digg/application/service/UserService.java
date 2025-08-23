package se.digg.application.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.digg.application.model.User;

@Service
@Slf4j
public class UserService
{
	private final Map<UUID, User> users = new ConcurrentHashMap<>();

	public UserService()
	{
		// Populate with some dummy data ~~ for demo + testing
		generateDummyData();
	}

	public List<User> getAllUsers()
	{
		log.info("Fetching all users. Total count: {}", users.size());
		return new ArrayList<>(users.values());
	}

	public List<User> getUsersPaginated(int page, int size)
	{
		log.info("Fetching users - page: {}, size: {}", page, size);

		List<User> allUsers = new ArrayList<>(users.values());
		int start = page * size;
		int end = Math.min(start + size, allUsers.size());

		if (start >= allUsers.size())
		{
			return new ArrayList<>();
		}

		return allUsers.subList(start, end);
	}

	public User createUser(User user)
	{
		user.setId(UUID.randomUUID());
		users.put(user.getId(), user);

		log.info("Created new user with ID: {}", user.getId());
		return user;
	}

	public Optional<User> getUserById(UUID id)
	{
		log.info("Fetching user with ID: {}", id);
		return Optional.ofNullable(users.get(id));
	}

	public Optional<User> updateUser(UUID id, User userUpdate)
	{
		if (users.get(id) != null)
		{
			userUpdate.setId(id);
			users.put(id, userUpdate);
			log.info("Updated user with ID: {}", id);
			return Optional.of(userUpdate);
		}
		log.warn("User with ID {} not found for update", id);
		return Optional.empty();
	}

	public boolean deleteUser(UUID id)
	{
		if (users.remove(id) != null)
		{
			log.info("Deleted user with ID: {}", id);
			return true;
		}
		log.warn("User with ID {} not found for deletion", id);
		return false;
	}

	public int getTotalCount()
	{
		return users.size();
	}
}