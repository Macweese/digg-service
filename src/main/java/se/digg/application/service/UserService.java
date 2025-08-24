/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
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
