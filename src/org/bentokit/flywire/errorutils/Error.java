package org.bentokit.flywire.errorutils;

import java.util.Collection;


/* Generic interface for describing errors for describing to the user. */
public interface Error {
	public boolean isSolveable();
	public Collection<? extends Solution> options();
	public String toString();
}

