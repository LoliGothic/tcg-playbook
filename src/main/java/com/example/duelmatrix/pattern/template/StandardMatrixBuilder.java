package com.example.duelmatrix.pattern.template;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.duelmatrix.domain.Archetype;
import com.example.duelmatrix.domain.MatchRecord;
import com.example.duelmatrix.dto.MatrixRowResponse;
import com.example.duelmatrix.repository.ArchetypeRepository;
import com.example.duelmatrix.repository.MatchRepository;

@Component
public class StandardMatrixBuilder extends AbstractMatrixBuilder{
	
	// A所有の共有Repositoryを直接インジェクション（DI）する
    private final ArchetypeRepository archetypeRepository;
    private final MatchRepository matchRepository;
    
    public StandardMatrixBuilder(ArchetypeRepository archetypeRepository, 
            MatchRepository matchRepository) {
		this.archetypeRepository = archetypeRepository;
		this.matchRepository = matchRepository;
	}

	@Override
	protected List<Archetype> loadArchetypes() {
		// TODO 自動生成されたメソッド・スタブ
		return archetypeRepository.findAll();
	}

	@Override
	protected List<MatchRecord> loadMatches() {
		// TODO 自動生成されたメソッド・スタブ
		return matchRepository.findAll();
	}

	@Override
	protected MatrixRowResponse buildRow(Archetype me, Archetype opponent, List<MatchRecord> matches) {
		// TODO 自動生成されたメソッド・スタブ
		int wins = 0;
		int losses = 0;
		int total = 0;
		double winRate = 0;
		
		for(MatchRecord record:matches) {
			if(record.getWinnerArchetypeId().equals(me.getId())) {
				if(record.getLoserArchetypeId().equals(opponent.getId())){
					wins++;
				}
			}
		}
		
		for(MatchRecord record:matches) {
			if(record.getWinnerArchetypeId().equals(opponent.getId())) {
				if(record.getLoserArchetypeId().equals(me.getId())){
					losses++;
				}
			}
		}
		
		total = wins + losses;
		
		if (total == 0) {
	        throw new IllegalStateException("対戦データが1件も存在しないため、マトリクスを生成できません．");
	    }
		
		winRate = (double) wins/total *100;
		
		return new MatrixRowResponse(me.getId(),me.getName(),opponent.getId(),opponent.getName(),wins,losses,total,winRate);
	}

}
