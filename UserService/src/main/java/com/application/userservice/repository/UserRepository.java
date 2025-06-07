package com.application.userservice.repository;

import com.application.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneno(Long phoneno);

    Page<User> findAll(Pageable pageable);
    Page<User> findByNameContaining(String name, Pageable pageable);

}