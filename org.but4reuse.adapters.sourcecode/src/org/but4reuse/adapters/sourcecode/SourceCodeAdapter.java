package org.but4reuse.adapters.sourcecode;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.sourcecode.adapter.CLanguage;
import org.but4reuse.adapters.sourcecode.adapter.JavaLanguage;
import org.but4reuse.adapters.sourcecode.adapter.LanguageConfigurator;
import org.but4reuse.adapters.sourcecode.adapter.ReadFSTProduct;
import org.but4reuse.adapters.sourcecode.adapter.Sop2FST;
import org.but4reuse.adapters.sourcecode.featurehouse.builder.ArtifactBuilder;
import org.but4reuse.adapters.sourcecode.featurehouse.builder.capprox.CApproxBuilder;
import org.but4reuse.adapters.sourcecode.featurehouse.builder.java.JavaBuilder;
import org.but4reuse.adapters.sourcecode.featurehouse.de.ovgu.cide.fstgen.ast.FSTNode;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

public class SourceCodeAdapter implements IAdapter {

	public static final String TYPE = "java";
	static ArtifactBuilder builder = null;

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		// check if there is at least one java class
		File file = FileUtils.getFile(uri);
		return FileUtils.containsFileWithExtension(file, "java");
	}

	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {

		/*
		 * Identify the language
		 */

		// type = uri.getExtension();

		if (TYPE.equals("java")) {

			LanguageConfigurator.LANGUAGE = new JavaLanguage();
			builder = new JavaBuilder();
		}

		if (TYPE.equals("c")) {

			builder = new CApproxBuilder();
			LanguageConfigurator.LANGUAGE = new CLanguage();
		}
		File file = FileUtils.getFile(uri);
		String dirVariant = file.getAbsolutePath();

		/*
		 * Step1: Read the input product variant.
		 */

		System.out.println("@ SPLIC Step 1 : Reading Input Products");
		System.out.println("@------------");
		List<IElement> artefact = null;
		ReadFSTProduct rp1 = new ReadFSTProduct();
		try {
			rp1.readProduct(dirVariant);
			artefact = rp1.getArtefact();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// handleBodies(rp1.getBody(), j);

		return artefact;
	}

	// TODO other protocols, platform at least!
	public static File getDirectory(URI uri) {

		File file = null;
		if (uri.getScheme().equals("file")) {
			file = new File(uri);
		}
		return file;
	}

	@Override
	public void construct(URI uri, List<IElement> cps, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

		Sop2FST ss = new Sop2FST();
		List<FSTNode> nodesS = ss.toFST(cps);

		String absPath = FileUtils.getFile(uri).getAbsolutePath();
		System.out.println("         @Code Generation for Feature :" + absPath);
		System.out.println("		                          @-------------------");
		for (FSTNode n : nodesS) {

			// if (n instanceof FSTNonTerminal){//String featName=f.getId();
			LanguageConfigurator.getLanguage().generateCode(n, absPath, null);
			// }

		}

	}

}
