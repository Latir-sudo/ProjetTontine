package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository

public interface RoleRepository extends JpaRepository<Role,Integer> {

    public Optional<Role> findByNomRole(String name);
}
