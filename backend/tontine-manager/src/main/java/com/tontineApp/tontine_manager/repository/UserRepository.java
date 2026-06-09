package com.tontineApp.tontine_manager.repository;


import com.tontineApp.tontine_manager.dto.UserRequest;
import com.tontineApp.tontine_manager.dto.UserResponse;
import com.tontineApp.tontine_manager.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users,Integer> {

     Optional<Users> findByEmail(String email);
     boolean existsByEmail(String email);
     Optional<Users> findByTelephone(String telephone);

    @Query("SELECT DISTINCT u FROM Users u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Users> findByEmailWithRoles(@Param("email") String email);

}
