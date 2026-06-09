package com.tontineApp.tontine_manager.repository;


import com.tontineApp.tontine_manager.model.Cotisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Integer> {

    List<Cotisation> findByMembre_Id(Integer id);
    List<Cotisation> findByMembre_TontineId(Integer tontineId);
}
