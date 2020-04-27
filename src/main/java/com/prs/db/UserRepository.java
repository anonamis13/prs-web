package com.prs.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prs.business.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByUserNameAndPassword(String userName, String password);
}
