package com.vm.skeleton.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vm.skeleton.entity.User;

public interface UserDetailRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
}
