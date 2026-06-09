package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.TontineFilter;
import com.tontineApp.tontine_manager.dto.TontineResponse;
import com.tontineApp.tontine_manager.mapper.TontineMapper;
import com.tontineApp.tontine_manager.repository.TontineFilterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TontineFilterService {

    private final TontineFilterRepository tontineFilterRepository;
    private final TontineMapper tontineMapper;

    public List<TontineResponse> TontineFilters(TontineFilter tontineFilter) {
        return tontineFilterRepository.filter(tontineFilter).stream()
                .map(tontineMapper::toTontineResponse)
                .toList();
    }
}
