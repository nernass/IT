package com.example.examplerest.repository;

import com.example.examplerest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

//@Repository

public interface UserRepository extends JpaRepository<User, Integer>, QuerydslPredicateExecutor<User> {

//    @Modifying
//    @Query(value = "select ", nativeQuery = true)
    Optional<User> findByEmail(String email);
    
}
