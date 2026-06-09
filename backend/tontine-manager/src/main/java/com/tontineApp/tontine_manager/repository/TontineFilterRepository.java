package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.dto.TontineFilter;
import com.tontineApp.tontine_manager.dto.TontineResponse;
import com.tontineApp.tontine_manager.mapper.TontineMapper;
import com.tontineApp.tontine_manager.model.Tontine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.List;

@Repository
public class TontineFilterRepository {
    @PersistenceContext
    private EntityManager em;


    public List<Tontine> filter(TontineFilter tontineFilter) {
        CriteriaBuilder cb =  em.getCriteriaBuilder(); // construction du query
        CriteriaQuery<Tontine> query = cb.createQuery(Tontine.class);
        Root<Tontine> tontine = query.from(Tontine.class); // la table interrogée

        List<Predicate> predicates = new ArrayList<>();

        if(tontineFilter.getRegion() != null && !tontineFilter.getRegion().isBlank()){
            predicates.add(cb.like(tontine.get("regionTontine"), "%"+tontineFilter.getRegion()+"%"));
        }
        if(tontineFilter.getMontant()!=null){
            predicates.add(cb.equal(tontine.get("montant"), tontineFilter.getMontant()));
        }
        if(tontineFilter.getFrequence()!=null && !tontineFilter.getFrequence().isBlank()){
            predicates.add(cb.like(tontine.get("frequence"), "%"+tontineFilter.getFrequence()+"%"));
        }
        if(tontineFilter.getCategorie()!=null && !tontineFilter.getCategorie().isBlank()){
            predicates.add(cb.like(tontine.get("categorieTontine"), "%"+tontineFilter.getCategorie()+"%"));
        }

        query.select(tontine).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(tontine.get("id")));
        return em.createQuery(query).getResultList();
    }

}
