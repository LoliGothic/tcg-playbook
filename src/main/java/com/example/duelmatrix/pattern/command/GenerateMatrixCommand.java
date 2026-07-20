package com.example.duelmatrix.pattern.command;

import org.springframework.stereotype.Component;

import com.example.duelmatrix.dto.MatrixResponse;
import com.example.duelmatrix.pattern.template.AbstractMatrixBuilder;
import com.example.duelmatrix.pattern.template.StandardMatrixBuilder;

@Component
public class GenerateMatrixCommand implements Command<MatrixResponse>{
	/**
	 *
	 * @author 立野浩太郎
	 * 
	 */
	
	public final AbstractMatrixBuilder builder ;
	
	public GenerateMatrixCommand(StandardMatrixBuilder builder) {
		this.builder = builder;
	}
	
	@Override
	public MatrixResponse execute() {
		// TODO 自動生成されたメソッド・スタブ
		return builder.build();
	}

}
