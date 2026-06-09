package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.enumeration.StatutAdhesion;
import com.tontineApp.tontine_manager.model.Adhesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdhesionRepository extends JpaRepository<Adhesion,Integer> {

    public List<Adhesion> findByStatutAndTontine_Id(StatutAdhesion statutAdhesion, Integer tontine);
    public Optional<Adhesion> findByUser_IdAndTontine_Id(Integer user_id, Integer tontine);
    boolean existsByUser_IdAndTontine_IdAndStatut(Integer idUser,Integer idTontine,StatutAdhesion statutAdhesion);
}
