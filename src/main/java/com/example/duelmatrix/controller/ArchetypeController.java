package com.example.duelmatrix.controller;

import com.example.duelmatrix.dto.ArchetypeCreateRequest;
import com.example.duelmatrix.dto.ArchetypeResponse;
import com.example.duelmatrix.pattern.command.CommandInvoker;
import com.example.duelmatrix.pattern.command.CreateArchetypeCommand;
import com.example.duelmatrix.service.ArchetypeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * アーキタイプ API の入口．
 */
@RestController
@RequestMapping("/api/archetypes")
public class ArchetypeController {

    private final ArchetypeService archetypeService;
    private final CommandInvoker commandInvoker;

    public ArchetypeController(ArchetypeService archetypeService, CommandInvoker commandInvoker) {
        this.archetypeService = archetypeService;
        this.commandInvoker = commandInvoker;
    }

    @GetMapping
    public List<ArchetypeResponse> getAllArchetypes() {
        return archetypeService.getAllArchetypes();
    }

    @PostMapping
    public ResponseEntity<ArchetypeResponse> createArchetype(
            @Valid @RequestBody ArchetypeCreateRequest request
    ) {
        CreateArchetypeCommand command = new CreateArchetypeCommand(archetypeService, request);
        ArchetypeResponse response = commandInvoker.invoke(command);

        return ResponseEntity
                .created(URI.create("/api/archetypes/" + response.getId()))
                .body(response);
    }
}
