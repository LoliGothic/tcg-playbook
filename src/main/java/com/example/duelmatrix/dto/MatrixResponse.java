package com.example.duelmatrix.dto;

import java.util.List;

public class MatrixResponse {
	
	private final List<MatrixRowResponse> rows;

	public MatrixResponse( List<MatrixRowResponse> rows) {
		this.rows = rows;
	}

	/** マトリクスの各行（API_SPEC の {@code rows}）．Jackson がこの getter 経由で出力する． */
	public List<MatrixRowResponse> getRows() {
		return rows;
	}

}
