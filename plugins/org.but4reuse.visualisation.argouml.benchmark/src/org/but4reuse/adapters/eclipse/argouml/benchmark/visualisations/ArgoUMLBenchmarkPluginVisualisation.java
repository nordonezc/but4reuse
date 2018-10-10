package org.but4reuse.adapters.eclipse.argouml.benchmark.visualisations;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.BlockElement;
import org.but4reuse.adaptedmodel.ElementWrapper;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IDependencyObject;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.javajdt.elements.FieldElement;
import org.but4reuse.adapters.javajdt.elements.ImportElement;
import org.but4reuse.adapters.javajdt.elements.MethodBodyElement;
import org.but4reuse.adapters.javajdt.elements.MethodElement;
import org.but4reuse.adapters.javajdt.elements.TypeElement;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.feature.location.LocatedFeaturesManager;
import org.but4reuse.feature.location.LocatedFeaturesUtils;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.utils.emf.EMFUtils;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.but4reuse.visualisation.IVisualisation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * List ArgoUML SPL Visualisation
 * 
 * @author Nicolas Ordonez Chala
 */
public class ArgoUMLBenchmarkPluginVisualisation implements IVisualisation {

	FeatureList featureList;
	AdaptedModel adaptedModel;

	@Override
	public void prepare(FeatureList featureList, AdaptedModel adaptedModel, Object extra, IProgressMonitor monitor) {
		this.featureList = featureList;
		this.adaptedModel = adaptedModel;
	}

	@Override
	public void show() {
		// Onlye if the featureList have been filled previously
		if (featureList != null && featureList.getName() != null) {

			// Get the artefact model
			IResource res = EMFUtils
					.getIResource(adaptedModel.getOwnedAdaptedArtefacts().get(0).getArtefact().eResource());

			// Create the file reference of the artefact model
			File artefactModelFile = WorkbenchUtils.getFileFromIResource(res);

			// Get your results package
			File yourResults = generateFolderFromArtefactModel(artefactModelFile, false);

			// Generate the files according to the ground truth
			generateGroundTruth(yourResults);

			// Refresh
			WorkbenchUtils.refreshIResource(res.getParent());
		}
	}

	/**
	 * Create the folder where it will be saved the answer.
	 * 
	 * @author Nicolas Ordoñez Chala
	 * @param artefactModelFile
	 * @param argoUMLSPLBenchmark - if true, then it will be save into the structure
	 *                            of argoUMLSPL Benchmark. Otherwise saved in the
	 *                            parent folder
	 * @return
	 */
	private File generateFolderFromArtefactModel(File artefactModelFile, boolean argoUMLSPLBenchmark) {
		File answer = null;
		try {
			if (argoUMLSPLBenchmark)
				// Look into the structure of argoUML for the folder yourResult
				answer = new File(artefactModelFile.getParentFile().getParentFile().getParentFile().getParentFile(),
						"yourResults");
			else
				// Create the folder inside the parent director of the artefact model
				answer = new File(artefactModelFile.getParent(), "benchmarkResults");

			// If not existe then create
			if (!answer.exists())
				answer.mkdir();
			else {
				FileUtils.deleteFile(answer);
				answer.mkdir();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * For each feature in the feature List get the elements and create the
	 * necessary files to visualize the files in the argoUMLSPL Benchmark structure
	 * 
	 * @author Nicolas Ordoñez Chala
	 * @param yourResults - Folder where will be created the txt files per feature
	 */
	private void generateGroundTruth(File yourResults) {
		// Get the located features
		List<LocatedFeature> locatedFeatures = LocatedFeaturesManager.getLocatedFeatures();

		// Put the calculated feature locations in one file per feature
		for (Feature feature : featureList.getOwnedFeatures()) {

			// Create the txt file object for each feature
			File file = new File(yourResults, feature.getId() + ".txt");

			// Create set to be sure that it wont save repetitive elements
			Set<String> textList = new LinkedHashSet<String>();

			// Get the block where each feature is present
			List<Block> blocksFeature = LocatedFeaturesUtils.getBlocksOfFeature(locatedFeatures, feature);
			List<IElement> elementsBlock = AdaptedModelHelper.getElementsOfBlocks(blocksFeature);
			textList = generateSetOfValuableElements(elementsBlock);
			StringBuilder textOfBlock = setToStringBuilder(textList);

			// Get all the elements of the located feature
			List<IElement> elementsLocatedFeatures = LocatedFeaturesUtils.getElementsOfFeature(locatedFeatures,
					feature);
			textList = generateSetOfValuableElements(elementsLocatedFeatures);
			StringBuilder textOfFeatures = setToStringBuilder(textList, textOfBlock);

			try {
				FileUtils.writeFile(file, textOfFeatures.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author Nicolas Ordoñez Chala
	 * @param List of elements
	 * @return
	 */
	private Set<String> generateSetOfValuableElements(List<IElement> elements) {
		Set<String> answer = new LinkedHashSet<String>();
		try {
			for (IElement e : elements) {
				// Get the class
				if (e instanceof TypeElement) {
					answer.add(((TypeElement) e).id);
				}
				// Get the method
				else if (e instanceof MethodElement) {
					answer.add(((MethodElement) e).id);
				}
				// Get the method body
				else if (e instanceof MethodBodyElement) {
					answer.add(((MethodElement) ((MethodBodyElement) e).getDependencies().get("methodBody").get(0)).id);
				}
				// Get the import element
				else if (e instanceof ImportElement) {
					answer.add(((ImportElement) e).id);
				}
				// Get variable declaration
				else if (e instanceof FieldElement) {
					answer.add(((FieldElement) e).id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * Represent the set of string as a StringBuilder
	 * 
	 * @author Nicolas Ordoñez Chala
	 * @param set of string
	 * @return StringBuilder where each element of the set is represented into a
	 *         line
	 */
	private StringBuilder setToStringBuilder(Set<String> set) {
		StringBuilder answer = new StringBuilder();
		try {
			// Check if set is not empty to make the trasnformation
			if (!set.isEmpty()) {
				// Look into the set for each word
				for (String s : set)
					// Each element of the set will be a new line
					answer.append(s + "\n");

				// Remove last empty line added \n
				answer.setLength(answer.length() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * Append to an existed StringBuilder the set of string
	 * 
	 * @author Nicolas Ordoñez Chala
	 * @param set      of string
	 * @param appendTo already created StringBuilder
	 */
	private StringBuilder setToStringBuilder(Set<String> set, StringBuilder appendTo) {
		StringBuilder answer = appendTo;
		try {
			// Check if set is not empty to make the trasnformation
			if (!set.isEmpty()) {
				// Look into the set for each word
				for (String s : set)
					// Each element of the set will be a new line
					answer.append(s + "\n");

				// Remove last empty line added \n
				answer.setLength(answer.length() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * Generate set with the relevant clauses for the argoUMlBenchmark from blocks
	 * 
	 * @author Nicolas Ordoñez Chala
	 * @param blocksFeature
	 * @param isBlock       - to differentiate from generateSetOfValuableElements.
	 * @return
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private Set<String> generateSetOfValuableElements(List<Block> blocksFeature, boolean isBlock) {
		Set<String> answer = new LinkedHashSet<String>();
		try {
			for (Block block : blocksFeature) {
				for (BlockElement be : block.getOwnedBlockElements()) {
					for (ElementWrapper ew : be.getElementWrappers()) {
						IElement ie = (IElement) ew.getElement();
						Map<String, List<IDependencyObject>> dependency = ie.getDependencies();
						for (String key : dependency.keySet()) {
							for (IDependencyObject ido : dependency.get(key)) {
								answer.add(ido.getDependencyObjectText().split(": ")[1]);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}
}
