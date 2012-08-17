package org.bentokit.flywire.errorutils;

public class ErrorSolver {
	boolean autoSolve; //if only one solution exists, do it without asking the user.
	
	public ErrorSolver(boolean autoSolve) {
		this.autoSolve = autoSolve;
	}
	
	public void solve(Exception e) {
		
	}
}
