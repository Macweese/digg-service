/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.digg.application.model.User;

public interface UserRepository extends JpaRepository<User, Long>
{
	List<User> findByNameContainingIgnoreCase(String name);

	@Query("SELECT u FROM User u WHERE "
		+ "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
		+ "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR "
		+ "LOWER(u.address) LIKE LOWER(CONCAT('%', :query, '%')) OR "
		+ "LOWER(u.telephone) LIKE LOWER(CONCAT('%', :query, '%'))")
	Page<User> queryUsers(@Param("query") String query, Pageable pageable);

	Optional<User> findByEmail(String email);
}
