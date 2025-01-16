package com.kai.repository;


import com.kai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id NOT IN :userIds")
    List<User> findUsersNotInIds(@Param("userIds") List<Long> userIds);

    Optional<User> findByUsernameOrEmail(String identifier, String identifier1);

    Optional<User> findByEmail(String email);
}
