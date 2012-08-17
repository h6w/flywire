package org.bentokit.flywire.util;

import java.io.File;

public interface FileListener {
	public void fileChanged(File file);
	public void fileDeleted(File file);
}
