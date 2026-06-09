package com.tontineApp.tontine_manager.repository;


import com.tontineApp.tontine_manager.model.Tontine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TontineRepository extends JpaRepository<Tontine,Integer> {
    List<Tontine> findByDateCreation(LocalDate dateCreation);
    List<Tontine> findByRegionTontine(String region);
    List<Tontine> findByCategorieTontine(String categorie);
    List<Tontine> findByMontant(Integer montant);
    List<Tontine> findByFrequence(String frequence);

    @Query("SELECT DISTINCT m.tontine FROM Membre m WHERE m.user.id = :userId")
    List<Tontine> findTontinesByUserId(@Param("userId") Integer userId);


}
