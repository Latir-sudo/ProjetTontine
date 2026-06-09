package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.TontineRequest;
import com.tontineApp.tontine_manager.dto.TontineResponse;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TontineMapper {

    private final MembreRepository membreRepository;

    public TontineRequest toTontineRequest(Tontine tontine) {
        TontineRequest response = new TontineRequest();
        response.setNomTontine(tontine.getNomTontine());
        response.setDescriptionTontine(tontine.getDescriptionTontine());
        response.setPolitiqueTontine(tontine.getPolitiqueTontine());
        response.setMontant(tontine.getMontant());
        response.setFrequence(tontine.getFrequence());
        response.setDateCreation(tontine.getDateCreation());
        response.setRegion(tontine.getRegionTontine());
        response.setCategorie(tontine.getCategorieTontine());
        return response;
    }

    public Tontine toTontine(TontineRequest tontineRequest) {
        Tontine tontine = new Tontine();
        tontine.setDescriptionTontine(tontineRequest.getDescriptionTontine());
        tontine.setNomTontine(tontineRequest.getNomTontine());
        tontine.setPolitiqueTontine(tontineRequest.getPolitiqueTontine());
        tontine.setFrequence(tontineRequest.getFrequence());
        tontine.setDateCreation(tontineRequest.getDateCreation());
        tontine.setMontant(tontineRequest.getMontant());
        tontine.setCategorieTontine(tontineRequest.getCategorie());
        tontine.setRegionTontine(tontineRequest.getRegion());
        tontine.setNombreMax(tontineRequest.getNombreMax());
        return tontine;
    }

    public TontineResponse toTontineResponse(Tontine tontine) {
        TontineResponse tontineResponse = new TontineResponse();
        tontineResponse.setId(tontine.getId());
        tontineResponse.setFrequence(tontine.getFrequence());
        tontineResponse.setMontant(tontine.getMontant());
        tontineResponse.setNomTontine(tontine.getNomTontine());
        tontineResponse.setDescriptionTontine(tontine.getDescriptionTontine());
        tontineResponse.setCategorie(tontine.getCategorieTontine());
        tontineResponse.setRegion(tontine.getRegionTontine());
        tontineResponse.setStatutTontine(tontine.getStatutTontine());
        tontineResponse.setNombreMax(tontine.getNombreMax());
        tontineResponse.setNombreMembres(Math.toIntExact(membreRepository.countByTontine_Id(tontine.getId())));
        // ✅ Gérer le cas où admin est null (sécurité)
        if (tontine.getAdmin() != null) {
            tontineResponse.setIdAdmin(tontine.getAdmin().getId());
            tontineResponse.setNomAdmin(tontine.getAdmin().getNom());
            tontineResponse.setPrenomAdmin(tontine.getAdmin().getPrenom());
            tontineResponse.setTelephoneAdmin(tontine.getAdmin().getTelephone());

        } else {
            tontineResponse.setIdAdmin(null);
            tontineResponse.setNomAdmin(null);
            tontineResponse.setPrenomAdmin(null);
            tontineResponse.setTelephoneAdmin(null);

        }

        return tontineResponse;
    }
}
