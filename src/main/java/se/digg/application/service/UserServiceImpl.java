package se.digg.application.service;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.digg.application.model.User;
import se.digg.application.repository.UserRepository;

@Slf4j
@Service
public class UserServiceImpl implements UserService
{
	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository)
	{
		this.userRepository = userRepository;
	}

	@Override
	public User createUser(User user)
	{
		log.debug("Creating new user: User={}", user);
		return userRepository.save(user);
	}

	@Override
	public Optional<User> getUserById(Long id)
	{
		log.debug("Fetching user with ID: id={}", id);
		return userRepository.findById(id);
	}

	@Override
	public Page<User> getUsers(Pageable pageable)
	{
		log.debug("Fetching paged users: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
		return userRepository.findAll(pageable);
	}

	public List<User> getAllUsers()
	{
		log.debug("Fetching all users");
		return userRepository.findAll();
	}

	@Override
	public Page<User> queryUsers(String query, Pageable pageable)
	{
		log.debug("Querying for '{}'", query);
		// Allow whitespace queries
		if (query == null || query.isEmpty())
		{
			return userRepository.findAll(pageable);
		}

		return userRepository.queryUsers(query, pageable);
	}

	@Override
	public Optional<User> updateUser(Long id, User user)
	{
		log.debug("Updating user with ID: id={}", id);

		return userRepository.findById(id)
			.map(u ->
			{
				u.setName(user.getName());
				u.setEmail(user.getEmail());
				u.setAddress(user.getAddress());
				u.setTelephone(user.getTelephone());
				log.debug("User updated");
				return userRepository.save(u);
			});
	}

	@Override
	public boolean deleteUser(Long id)
	{
		log.info("Attempting to delete user with ID: id={}", id);
		if (userRepository.existsById(id))
		{
			userRepository.deleteById(id);
			return true;
		}
		return false;
	}
}