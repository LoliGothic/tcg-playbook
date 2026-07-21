package com.example.duelmatrix.dto;

public class MatrixRowResponse {
	
	public Long myArchetypeId;
	public String myArchetypeName;
	public Long opponentArchetypeId;
	public String opponentArchetypeName;
	public int wins;
	public int losses;
	public int total;
	public double winRate;
	
	public MatrixRowResponse(Long myId, String myName, Long oppoId, String oppoName,int wins, int losses, int total, double winRate){
		this.myArchetypeId = myId;
		this.myArchetypeName = myName;
		this.opponentArchetypeId = oppoId;
		this.opponentArchetypeName = oppoName;
		this.wins = wins;
		this.losses = losses;
		this.total = total;
		this.winRate = winRate;
	
	}

}
