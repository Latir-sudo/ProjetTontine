package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.MembreRequest;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class MembreMapper {
        private final UserRepository userRepository;
        private final TontineRepository tontineRepository;
        public MembreMapper(UserRepository userRepository, TontineRepository tontineRepository) {
            this.userRepository = userRepository;
            this.tontineRepository = tontineRepository;
        }

    public MembreRequest toMembreRequest(Membre membre) {
        MembreRequest membreRequest = new MembreRequest();
        membreRequest.setIdUser(membre.getUser().getId());
        membreRequest.setIdTontine(membre.getTontine().getId());

        return membreRequest;
    }

    public Membre toMembre(MembreRequest membreRequest) {
        Membre membre = new Membre();
        membre.setUser(userRepository.findById(membreRequest.getIdUser()).orElseThrow(()->new RessourceNotFoundException("Membre not found")));
        membre.setTontine(tontineRepository.findById(membreRequest.getIdTontine()).orElseThrow(()->new RessourceNotFoundException("Tontine not found")));
        membre.setDateAdhesion(LocalDate.now());

        return membre;
    }

}
