package se.digg.application.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se.digg.application.model.User;

public interface UserService
{
	Page<User> getUsers(Pageable pageable);

	List<User> getAllUsers();

	Page<User> queryUsers(String query, Pageable pageable);

	Optional<User> getUserById(Long id);

	User createUser(User user);

	Optional<User> updateUser(Long id, User user);

	boolean deleteUser(Long id);
}
