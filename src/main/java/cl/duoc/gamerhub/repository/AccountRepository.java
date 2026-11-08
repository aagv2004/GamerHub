package cl.duoc.gamerhub.repository;

import cl.duoc.gamerhub.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
