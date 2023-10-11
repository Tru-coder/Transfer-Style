package com.example.transferstylerebuildmaven.repositories;

import com.example.transferstylerebuildmaven.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

}
