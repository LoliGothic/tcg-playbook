package com.example.duelmatrix.pattern.factory;

import com.example.duelmatrix.domain.Archetype;
import com.example.duelmatrix.dto.ArchetypeCreateRequest;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * アーキタイプ Entity の生成を担う Factory．
 */
@Component
public class ArchetypeFactory {

    public Archetype create(ArchetypeCreateRequest request) {
        return new Archetype(request.getName(), LocalDateTime.now());
    }
}
