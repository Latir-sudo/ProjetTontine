package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.model.Tontine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<Membre, Integer> {
    boolean existsByTontineAndUser(Tontine tontine, Users user);
    Optional<Membre> findByTontine_IdAndUser_Id(Integer idTontine,Integer idUser);
    public List<Membre> findAllByUser_Id(Integer id);
    public List<Membre> findAllByTontine_Id(Integer id);
    long countByTontine_Id(Integer tontineId);
    boolean existsByTontine_IdAndUser_Id(Integer tontineId, Integer userId);

}
