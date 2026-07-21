package com.example.duelmatrix.dto;

import java.util.List;

public class MatrixResponse {
	
	List<MatrixRowResponse> rows;
	
	public MatrixResponse( List<MatrixRowResponse> rows) {
		this.rows = rows;
	}

}
