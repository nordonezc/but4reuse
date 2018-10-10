package org.but4reuse.adapters.eclipse.argouml.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.utils.files.FileUtils;

/**
 * 
 * @author Nicolas Ordonez Chala
 * 
 */
public class BenchmarkFormat {

	/**
	 * Create results file content
	 * 
	 * @param actualFolder
	 *            containing txt files with the actual values
	 */
	public static String createResultsFile(File actualFolder) {
		
		StringBuilder sb = new StringBuilder();
		int abstractFeatures = 0;
		for (File f : actualFolder.listFiles()) {
			String name = f.getName().substring(0, f.getName().length() - ".txt".length());
			List<String> actualLines = FileUtils.getLinesOfFile(f);
			if (!actualLines.isEmpty()) {
				sb.append("\n");
			} else {
				abstractFeatures++;
			}
		}

		return sb.toString();
	}


}
