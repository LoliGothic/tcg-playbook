package com.example.duelmatrix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.duelmatrix.dto.MatrixResponse;
import com.example.duelmatrix.pattern.command.CommandInvoker;
import com.example.duelmatrix.pattern.command.GenerateMatrixCommand;

@RestController
public class MatrixController {
	
	public final GenerateMatrixCommand slot;
	public final CommandInvoker invoker;
	
	public MatrixController(GenerateMatrixCommand slot, CommandInvoker invoker) {
		this.slot = slot;
		this.invoker = invoker;
	}
	
	@GetMapping("/api/matrix")
	public MatrixResponse getMatrix() {
		return this.invoker.invoke(slot);
	};
	
}
