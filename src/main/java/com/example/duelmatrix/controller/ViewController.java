package com.example.duelmatrix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {
	
	@GetMapping("/archetypes")
	public String archetypesPage() {
		return "archetypes"; 
	};   // templates/archetypes.html を返す

	@GetMapping("/matches")
	public String matchesPage() {
		return "matches";
	};      // templates/matches.html を返す

	@GetMapping("/matrix")
	public String matrixPage() {
		return "matrix";
	};       // templates/matrix.html を返す
}
