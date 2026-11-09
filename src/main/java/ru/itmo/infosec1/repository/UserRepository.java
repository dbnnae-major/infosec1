package ru.itmo.infosec1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.infosec1.model.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
