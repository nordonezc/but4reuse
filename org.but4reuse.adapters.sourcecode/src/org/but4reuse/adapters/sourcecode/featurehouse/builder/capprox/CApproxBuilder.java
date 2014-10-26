package org.but4reuse.adapters.sourcecode.featurehouse.builder.capprox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import org.but4reuse.adapters.sourcecode.featurehouse.builder.ArtifactBuilder;
import org.but4reuse.adapters.sourcecode.featurehouse.cide.gparser.OffsetCharStream;
import org.but4reuse.adapters.sourcecode.featurehouse.cide.gparser.ParseException;
import org.but4reuse.adapters.sourcecode.featurehouse.de.ovgu.cide.fstgen.ast.FSTNonTerminal;
import org.but4reuse.adapters.sourcecode.featurehouse.tmp.generated_capprox.CApproxParser;


public class CApproxBuilder extends ArtifactBuilder {
    private final static String[] suffixArray = { ".c", ".h" };

    public CApproxBuilder() {
	super(suffixArray);
    }

    public void processNode(FSTNonTerminal parent, StringTokenizer st,
	    File inputFile) throws FileNotFoundException, ParseException {
	/*
	 * type depends on file-ending:
	 * 	".*\.c" : "C-File
	 * 	".*\.h" : "H-File
	 */
	String type = inputFile.getName().replaceAll("(.*)\\.(.*)", "$2")
		.toUpperCase() + "-File";
	
	FSTNonTerminal rootDocument = new FSTNonTerminal(type, st
		.nextToken());
	parent.addChild(rootDocument);
	CApproxParser p = new CApproxParser(new OffsetCharStream(
		new FileInputStream(inputFile)));
	p.TranslationUnit(false);
	rootDocument.addChild(p.getRoot());
	// System.err.println(p.getRoot().toString());
    }
}
