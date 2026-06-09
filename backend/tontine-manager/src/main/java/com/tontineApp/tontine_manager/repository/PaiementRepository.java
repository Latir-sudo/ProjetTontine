package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    // ========== RECHERCHES PAR MEMBRE ==========

    /**
     * Récupère tous les paiements d'un membre via ses cotisations
     * Chemin: Paiement → Cotisation → Membre
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.id = :membreId ORDER BY p.datePaiement DESC")
    List<Paiement> findByMembreIdOrderByDatePaiementDesc(@Param("membreId") Integer membreId);

    /**
     * Récupère les paiements validés d'un membre
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = true")
    List<Paiement> findValidesByMembreId(@Param("membreId") Integer membreId);

    /**
     * Récupère les paiements en attente d'un membre (valide = false)
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = false")
    List<Paiement> findEnAttenteByMembreId(@Param("membreId") Integer membreId);

    /**
     * Compte les paiements validés d'un membre
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = true")
    Integer countValidesByMembreId(@Param("membreId") Integer membreId);

    /**
     * Compte les paiements en attente d'un membre (valide = false)
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = false")
    Integer countEnAttenteByMembreId(@Param("membreId") Integer membreId);

    /**
     * Compte les paiements échoués d'un membre (valide = false)
     * (identique à en attente selon votre logique)
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = false")
    Integer countEchouesByMembreId(@Param("membreId") Integer membreId);

    /**
     * Vérifie si un membre a des paiements validés
     */
    @Query("SELECT COUNT(p) > 0 FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = true")
    boolean existsValidesByMembreId(@Param("membreId") Integer membreId);


    // ========== RECHERCHES PAR TONTINE ==========

    /**
     * Récupère tous les paiements d'une tontine
     * Chemin: Paiement → Cotisation → Membre → Tontine
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId ORDER BY p.datePaiement DESC")
    List<Paiement> findByTontineIdOrderByDatePaiementDesc(@Param("tontineId") Integer tontineId);

    /**
     * Récupère les paiements validés d'une tontine
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.valide = true ORDER BY p.datePaiement DESC")
    List<Paiement> findValidesByTontineId(@Param("tontineId") Integer tontineId);

    /**
     * Récupère les paiements en attente d'une tontine
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.valide = false ORDER BY p.datePaiement DESC")
    List<Paiement> findEnAttenteByTontineId(@Param("tontineId") Integer tontineId);

    /**
     * Compte les paiements validés d'une tontine
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.valide = true")
    Integer countValidesByTontineId(@Param("tontineId") Integer tontineId);

    /**
     * Compte les paiements en attente d'une tontine
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.valide = false")
    Integer countEnAttenteByTontineId(@Param("tontineId") Integer tontineId);

    /**
     * Récupère le montant total des paiements validés pour une tontine
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.valide = true")
    Long sumMontantValidesByTontineId(@Param("tontineId") Integer tontineId);


    // ========== RECHERCHES PAR COTISATION ==========

    /**
     * Récupère tous les paiements d'une cotisation spécifique
     */
    List<Paiement> findByCotisation_Id(Integer cotisationId);

    /**
     * Récupère les paiements validés d'une cotisation
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.id = :cotisationId AND p.valide = true")
    List<Paiement> findValidesByCotisationId(@Param("cotisationId") Integer cotisationId);

    /**
     * Récupère les paiements en attente d'une cotisation
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.id = :cotisationId AND p.valide = false")
    List<Paiement> findEnAttenteByCotisationId(@Param("cotisationId") Integer cotisationId);

    /**
     * Compte les paiements d'une cotisation
     */
    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.cotisation.id = :cotisationId")
    Integer countByCotisationId(@Param("cotisationId") Integer cotisationId);

    /**
     * Calcule le montant total payé pour une cotisation (paiements validés)
     */
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.cotisation.id = :cotisationId AND p.valide = true")
    Long sumMontantValidesByCotisationId(@Param("cotisationId") Integer cotisationId);


    // ========== RECHERCHES PAR STATUT ==========

    /**
     * Récupère tous les paiements validés
     */
    @Query("SELECT p FROM Paiement p WHERE p.valide = true ORDER BY p.datePaiement DESC")
    List<Paiement> findAllValides();

    /**
     * Récupère tous les paiements en attente
     */
    @Query("SELECT p FROM Paiement p WHERE p.valide = false ORDER BY p.datePaiement DESC")
    List<Paiement> findAllEnAttente();

    /**
     * Récupère les paiements par mode de paiement
     */
    @Query("SELECT p FROM Paiement p WHERE p.modePaiement = :mode ORDER BY p.datePaiement DESC")
    List<Paiement> findByModePaiement(@Param("mode") String mode);

    /**
     * Récupère les paiements entre deux dates
     */
    @Query("SELECT p FROM Paiement p WHERE p.datePaiement BETWEEN :startDate AND :endDate ORDER BY p.datePaiement DESC")
    List<Paiement> findByDatePaiementBetween(@Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);

    /**
     * Récupère les paiements d'une période spécifique pour une tontine
     */
    @Query("SELECT p FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId AND p.datePaiement BETWEEN :startDate AND :endDate ORDER BY p.datePaiement DESC")
    List<Paiement> findByTontineIdAndDateBetween(@Param("tontineId") Integer tontineId, @Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);


    // ========== RECHERCHES AVEC PROJECTIONS ==========

    /**
     * Récupère les IDs des paiements validés d'un membre
     */
    @Query("SELECT p.id FROM Paiement p WHERE p.cotisation.membre.id = :membreId AND p.valide = true")
    List<Integer> findValidesIdsByMembreId(@Param("membreId") Integer membreId);

    /**
     * Récupère les références des paiements d'une tontine
     */
    @Query("SELECT p.reference FROM Paiement p WHERE p.cotisation.membre.tontine.id = :tontineId")
    List<String> findReferencesByTontineId(@Param("tontineId") Integer tontineId);


    // ========== MÉTHODES D'EXISTENCE ==========

    /**
     * Vérifie si un paiement existe avec une référence donnée
     */
    boolean existsByReference(String reference);

    /**
     * Vérifie si une cotisation a des paiements validés
     */
    @Query("SELECT COUNT(p) > 0 FROM Paiement p WHERE p.cotisation.id = :cotisationId AND p.valide = true")
    boolean existsValidesByCotisationId(@Param("cotisationId") Integer cotisationId);

    /**
     * Vérifie si un membre a effectué au moins un paiement
     */
    @Query("SELECT COUNT(p) > 0 FROM Paiement p WHERE p.cotisation.membre.id = :membreId")
    boolean existsByMembreId(@Param("membreId") Integer membreId);


}