package se.digg.application.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import se.digg.application.model.User;

@DataJpaTest
class UserRepositoryTest
{

	@Autowired
	private UserRepository userRepository;

	@Test
	void uniqueEmailConstraintIsEnforced()
	{
		var u1 = new User("Dup Name", "Addr 1", "dup@example.com", "111");
		var u2 = new User("Dup Name 2", "Addr 2", "dup@example.com", "222");

		userRepository.saveAndFlush(u1);

		assertThatThrownBy(() -> {
			userRepository.saveAndFlush(u2);
		}).isInstanceOf(DataIntegrityViolationException.class);
	}
}