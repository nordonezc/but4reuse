package org.but4reuse.feature.location.lsi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.feature.location.IFeatureLocation;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.eclipse.core.runtime.IProgressMonitor;

public class FeatureLocationLSIImpl implements IFeatureLocation {

	//feature location technique applying LSI starts here
	/**
	 * Get Blocks based on the AdaptedModel
	 * @author Nicolas Ordonez Chala
	 * @param adaptedModel
	 * @return ArrayList<Block>
	 */
	static public ArrayList<Block> getBlocks(AdaptedModel adaptedModel){
		ArrayList<Block> answer;
		try {
			answer = new ArrayList<Block>();
			for (Block b : adaptedModel.getOwnedBlocks()) {
				answer.add(b);
			}
		} catch(Exception e) {
			answer = null;
		}
		return answer;
	}
	
	/**
	 * Get the Blocks as a HashMap
	 * @author Nicolas Ordonez Chala
	 * @param featureBlocks
	 * @return ArrayList<HashMap<String, Integer>>
	 */
	static public ArrayList<HashMap<String, Integer>> blocksToArray(ArrayList<Block> featureBlocks){
		ArrayList<HashMap<String, Integer>> answer;
		
		try {
			answer = new ArrayList<HashMap<String,Integer>>();
			for (Block b : featureBlocks) {
				HashMap<String, Integer> d = new HashMap<String, Integer>();
				for (IElement e : AdaptedModelHelper.getElementsOfBlock(b)) {
					List<String> words = ((AbstractElement) e).getWords();
					for (String w : words) {
						String tmp = w.toLowerCase();
						if (d.containsKey(tmp))
							d.put(tmp, d.get(tmp) + 1);
						else
							d.put(tmp, 1);
					}
				}
				answer.add(d);
			}
			
			answer = null;
		}catch(Exception e) {
			answer = null;
		}
		
		return answer;
	}
	
	/**
	 * Some e.g.:
	 * - Classic http://www1.se.cuhk.edu.hk/~seem5680/lecture/LSI-Eg.pdf
	 * - Standford https://nlp.stanford.edu/IR-book/html/htmledition/latent-semantic-indexing-1.html
	 * Based on the featureList and the adapted Model
	 * @author Nicolas Ordonez Chala
	 */
	@Override
	public List<LocatedFeature> locateFeatures(FeatureList featureList, AdaptedModel adaptedModel,
			IProgressMonitor monitor) {

		List<LocatedFeature> locatedFeatures;
		
		try {
			locatedFeatures = new ArrayList<LocatedFeature>();
			ArrayList<Block> featureBlocks = getBlocks(adaptedModel);
			ArrayList<HashMap<String, Integer>> documents = blocksToArray(featureBlocks);

			if (documents.size() == 0)
				return locatedFeatures;

			//For each feature in the feature list
			for (Feature f : featureList.getOwnedFeatures()) {
				
				// Create the query
				double vecQ[] = LSITechnique.createQuery(documents, getFeatureWords(f));
				
				// Create the matrix of documents
				double vecD[][] = LSITechnique.createMatrix(documents);
				
				// Get the array of similarity
				double vecSimilarity[] = LSITechnique.applyLSI(vecQ, vecD);

				// For each block
				for (int i = 0; i < featureBlocks.size(); i++) {
					Block b = featureBlocks.get(i);
					
					//vecSimilarity in the i position refers to the Block i
					double dSimilarity = vecSimilarity[i];
					
					// It is necessary to get the answer between 0 and 1
					// cos give results between -1 and 1
					// sum 1 to the similarity then results are between 0 and 2
					dSimilarity++;
					
					// divide by 2 the similarity then results are between 0 and 1
					dSimilarity /= 2;
					
					//
					locatedFeatures.add(new LocatedFeature(f, b, dSimilarity));
				}

			}
		}catch(Exception e) {
			locatedFeatures = null;
		}
		return locatedFeatures;
	}

	/**
	 * It will give the words from the feature
	 * 
	 * @param f
	 *            The feature
	 * @return A HashMap with the words from the description and feature name.
	 *         For each words we have how many times it was found
	 */
	static public HashMap<String, Integer> getFeatureWords(Feature f) {
		/*
		 * We gather and count words from the feature name
		 */
		HashMap<String, Integer> featureWords = new HashMap<String, Integer>();
		if (f.getName() != null) {
			StringTokenizer tk = new StringTokenizer(f.getName(), " :!?*+²&~\"#'{}()[]-|`_\\^°,.;/§");

			while (tk.hasMoreTokens()) {
				String tmp = tk.nextToken().toLowerCase();
				if (featureWords.containsKey(tmp))
					featureWords.put(tmp, featureWords.get(tmp) + 1);
				else
					featureWords.put(tmp, 1);
			}
		}

		/*
		 * We gather and count words from the feature description
		 */
		if (f.getDescription() != null) {
			StringTokenizer tk = new StringTokenizer(f.getDescription(), " :!?*+²&~\"#'{}()[]-|`_\\^°,.;/§");
			while (tk.hasMoreTokens()) {

				String tmp = tk.nextToken().toLowerCase();
				if (featureWords.containsKey(tmp))
					featureWords.put(tmp, featureWords.get(tmp) + 1);
				else
					featureWords.put(tmp, 1);
			}
		}
		/*
		 * We return all words found
		 */
		return featureWords;
	}


}
