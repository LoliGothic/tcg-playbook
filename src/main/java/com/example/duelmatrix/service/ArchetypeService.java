package com.example.duelmatrix.service;

import com.example.duelmatrix.domain.Archetype;
import com.example.duelmatrix.dto.ArchetypeCreateRequest;
import com.example.duelmatrix.dto.ArchetypeResponse;
import com.example.duelmatrix.exception.ArchetypeAlreadyExistsException;
import com.example.duelmatrix.exception.ArchetypeNotFoundException;
import com.example.duelmatrix.pattern.factory.ArchetypeFactory;
import com.example.duelmatrix.repository.ArchetypeRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * アーキタイプの取得・登録に関する業務ロジック．
 */
@Service
public class ArchetypeService {

    private final ArchetypeRepository archetypeRepository;
    private final ArchetypeFactory archetypeFactory;

    public ArchetypeService(ArchetypeRepository archetypeRepository, ArchetypeFactory archetypeFactory) {
        this.archetypeRepository = archetypeRepository;
        this.archetypeFactory = archetypeFactory;
    }

    public List<ArchetypeResponse> getAllArchetypes() {
        return archetypeRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    public ArchetypeResponse createArchetype(ArchetypeCreateRequest request) {
        if (archetypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ArchetypeAlreadyExistsException();
        }

        Archetype archetype = archetypeFactory.create(request);
        Archetype savedArchetype = archetypeRepository.save(archetype);
        return toResponse(savedArchetype);
    }

    public Archetype findById(Long id) {
        return archetypeRepository.findById(id)
                .orElseThrow(ArchetypeNotFoundException::new);
    }

    private ArchetypeResponse toResponse(Archetype archetype) {
        return new ArchetypeResponse(
                archetype.getId(),
                archetype.getName(),
                archetype.getCreatedAt()
        );
    }
}
