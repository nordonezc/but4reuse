package org.but4reuse.feature.location.lsi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	/**
	 * Some e.g.: - Classic http://www1.se.cuhk.edu.hk/~seem5680/lecture/LSI-Eg.pdf
	 * - Standford
	 * https://nlp.stanford.edu/IR-book/html/htmledition/latent-semantic-indexing-1.html
	 * Based on the featureList and the adapted Model
	 * 
	 * @author Nicolas Ordonez Chala
	 */
	@Override
	public List<LocatedFeature> locateFeatures(FeatureList featureList, AdaptedModel adaptedModel,
			IProgressMonitor monitor) {

		List<LocatedFeature> locatedFeatures;

		try {
			locatedFeatures = new ArrayList<LocatedFeature>();
			ArrayList<Block> featureBlocks = (ArrayList<Block>) getBlocks(adaptedModel);
			List<List<String>> documents = blocksToListList(featureBlocks);

			if (documents.size() == 0)
				return locatedFeatures;

			// For each feature in the feature list
			for (Feature f : featureList.getOwnedFeatures()) {

				// Get the array of similarity
				LSITechnique lsiTechnique = new LSITechnique(documents);
				double vecSimilarity[] = lsiTechnique.applyLSI(getFeatureWordsAsList(f), documents);

				// For each block
				for (int i = 0; i < featureBlocks.size(); i++) {
					Block b = featureBlocks.get(i);

					// vecSimilarity in the i position refers to the Block i
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
		} catch (Exception e) {
			locatedFeatures = null;
		}
		return locatedFeatures;
	}

	/**
	 * Get Blocks based on the AdaptedModel
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param adaptedModel
	 * @return ArrayList<Block>
	 */
	static public List<Block> getBlocks(AdaptedModel adaptedModel) {
		List<Block> answer;
		try {
			answer = new ArrayList<Block>();
			for (Block b : adaptedModel.getOwnedBlocks()) {
				answer.add(b);
			}
		} catch (Exception e) {
			answer = null;
		}
		return answer;
	}

	/**
	 * Get the Blocks as a List of Maps
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param featureBlocks
	 * @return ArrayList<HashMap<String, Integer>>
	 */
	static public List<Map<String, Integer>> blocksToListMap(List<Block> featureBlocks) {
		List<Map<String, Integer>> answer;

		try {
			answer = new ArrayList<Map<String, Integer>>();
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
		} catch (Exception e) {
			answer = null;
		}

		return answer;
	}

	/*
	 * Get the Blocks as a List of List
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param featureBlocks
	 * @return ArrayList<HashMap<String, Integer>>
	 */
	static public List<List<String>> blocksToListList(List<Block> featureBlocks) {
		List<List<String>> answer;

		try {
			answer = new ArrayList<List<String>>();
			for (Block b : featureBlocks) {
				for (IElement e : AdaptedModelHelper.getElementsOfBlock(b)) {
					List<String> documents = ((AbstractElement) e).getWords();
					for (String d : documents) {
						d = d.toLowerCase();
					}
					answer.add(documents);
				}
			}

			answer = null;
		} catch (Exception e) {
			answer = null;
		}

		return answer;
	}

	/**
	 * It will give the words from the feature
	 * 
	 * @param f The feature
	 * @return A HashMap with the words from the description and feature name. For
	 *         each words we have how many times it was found
	 */
	static public Map<String, Integer> getFeatureWordsAsMap(Feature f) {

		HashMap<String, Integer> featureWords = new HashMap<String, Integer>();
		if (f.getName() != null) {
			StringTokenizer tk = new StringTokenizer(f.getName(), " :!?*+�&~\"#'{}()[]-|`_\\^�,.;/�");

			while (tk.hasMoreTokens()) {
				String tmp = tk.nextToken().toLowerCase();
				if (featureWords.containsKey(tmp))
					featureWords.put(tmp, featureWords.get(tmp) + 1);
				else
					featureWords.put(tmp, 1);
			}
		}

		// We gather and count words from the feature description
		if (f.getDescription() != null) {
			StringTokenizer tk = new StringTokenizer(f.getDescription(), " :!?*+�&~\"#'{}()[]-|`_\\^�,.;/�");
			while (tk.hasMoreTokens()) {

				String tmp = tk.nextToken().toLowerCase();
				if (featureWords.containsKey(tmp))
					featureWords.put(tmp, featureWords.get(tmp) + 1);
				else
					featureWords.put(tmp, 1);
			}
		}

		return featureWords;
	}

	/**
	 * It will give the words from the feature as a List
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param f The feature
	 * @return A HashMap with the words from the description and feature name. For
	 *         each words we have how many times it was found
	 */
	static public List<String> getFeatureWordsAsList(Feature f) {

		List<String> answer;

		try {
			answer = new ArrayList<String>();
			// Check if feature is not null
			if (f.getName() != null) {

				// Split by special caracters
				StringTokenizer tk = new StringTokenizer(f.getName(), " :!?*+�&~\"#'{}()[]-|`_\\^�,.;/�");

				// Check the array of words
				while (tk.hasMoreTokens()) {
					// get word and transform it to lower case
					String tmp = tk.nextToken().toLowerCase();
					// add it to the answer
					answer.add(tmp);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			answer = null;
		}

		return answer;
	}

}
