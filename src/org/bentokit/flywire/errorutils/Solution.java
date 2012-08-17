package org.bentokit.flywire.errorutils;

/* Generic interface for offering solutions to errors. */
public abstract class Solution {
	public abstract String toString();
	public abstract boolean solve();
}
