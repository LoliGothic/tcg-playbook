package com.example.duelmatrix.pattern.command;

import com.example.duelmatrix.dto.ArchetypeCreateRequest;
import com.example.duelmatrix.dto.ArchetypeResponse;
import com.example.duelmatrix.service.ArchetypeService;

/**
 * アーキタイプ作成操作を表す Command．
 */
public class CreateArchetypeCommand implements Command<ArchetypeResponse> {

    private final ArchetypeService archetypeService;
    private final ArchetypeCreateRequest request;

    public CreateArchetypeCommand(ArchetypeService archetypeService, ArchetypeCreateRequest request) {
        this.archetypeService = archetypeService;
        this.request = request;
    }

    @Override
    public ArchetypeResponse execute() {
        return archetypeService.createArchetype(request);
    }
}
