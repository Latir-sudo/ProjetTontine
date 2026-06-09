package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Membre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TontineMembreRepository extends JpaRepository<Membre,Integer> {

}
