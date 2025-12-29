package miron.gaskov.racelog.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByLogin(String login);
    List<User> findByRoleAndLoginContainingIgnoreCase(UserRole role, String query);
}
