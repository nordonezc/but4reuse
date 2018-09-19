package org.but4reuse.feature.location.lsi.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.but4reuse.feature.location.lsi.activator.Activator;
import org.but4reuse.feature.location.lsi.location.preferences.LSIPreferencePage;
import org.but4reuse.featurelist.Feature;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class LSITechnique {

	//LSI stars here
	/**
	 * Apply the LSI technique based on inputs
	 * @author Nicolas Ordonez Chala
	 * @param query - weights
	 * @param documents - weights
	 * @return List of similarities of the query for each document
	 */
	static public double[] applyLSI(double[] query, double[][] documents) {
		
		double answer[]; 
		
		try {
		//Step 1
		//Get the input
		//...
		
		//Step 2
		//Decompose matrix a from the set of documents
		//Use Jama Matrix Library 
		//https://math.nist.gov/javanumerics/jama/doc/
		
		//Another Java Libraries
		//https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/
		
		Matrix a = new Matrix(documents);
		SingularValueDecomposition svd = a.svd();
		// A = U*S*(V^T)
		Matrix u = svd.getU();
		Matrix s = svd.getS();
		Matrix v = svd.getV();
		
		//Step 3
		//Implement low rank (k) approximation where K
		
		// Get the value of the filter
		double dim = Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
		int nbDim = Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED) ?
				(int) dim : ((int)dim * s.getRowDimension());
				
		// We check if the matrix is not to small, and update the number of
		// dimensions
		nbDim = Math.min(nbDim, s.getRowDimension());
		
		/*
		 * Here, rand-reduce The the U and S matrix are sorted by singular
		 * value the highest to the smallest so we just remove the last rows
		 * and columns.
		 */
		Matrix sk = s.getMatrix(0, nbDim - 1, 0, nbDim - 1);
		Matrix uk = u.getMatrix(0, u.getRowDimension() - 1, 0, nbDim - 1);
		Matrix vk = v.getMatrix(0, v.getRowDimension() - 1, 0, nbDim - 1);

		//Step 4
		//Get coor of each document
		//Equal to vk
		
		//Step 5
		//Find new query vector q=(q^T)*uk*(sk^-1)
		Matrix q = new Matrix(query, query.length);
		q = sk.inverse().times(uk).times(q.transpose());
		double q_array[] = q.getColumnPackedCopy();
		
		//Step 6 Rank in decresing order of query-document cosine similarities. 
		answer = new double[vk.getColumnDimension()];
		
		
		for(int i=0; i<vk.getColumnDimension(); i++) {
			//Get each d matrix from vk
			Matrix di = vk.getMatrix(0, vk.getRowDimension()-1, i, i);
			double di_array[] = di.getColumnPackedCopy();
			
			//cosine similarity between di and q
			answer[i] = cosine(di_array, q_array);
		}
		
		}catch(Exception e) {
			answer = null;
		}
		
		return answer;
		
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

	/**
	 * We count all different words In the matrix we must have for each
	 * words how many times it was found in the document even if it's 0
	 * @author Nicolas Ordonez Chala
	 * @param documents
	 * @return
	 */
	static public ArrayList<String> countDifferentWords (ArrayList<HashMap<String, Integer>> documents){
		ArrayList<String> words = new ArrayList<String>();
		
		try {
			// Search inside each document
			for (HashMap<String, Integer> t : documents) {
				// Search inside each word
				for (String s : t.keySet()) {
					// Check if s word is not in the returning list
					if (!words.contains(s)) {
						words.add(s);
					}
				}
			}


			/*
			 * We sort the words list ( From LSI not necessary but made generally)
			 */
			Collections.sort(words);
			
		} catch(Exception e) {
			
		}
		
		return words;
	}
	
	/**
	 * We count all different words In the matrix we must have for each
	 * words how many times it was found in the document even if it's 0
	 * @author Nicolas Ordonez Chala
	 * @param documents
	 * @return
	 */
	static public ArrayList<String> countUniqueWords (ArrayList<List<String>> documents){
		ArrayList<String> words = new ArrayList<String>();
		
		try {
			// Search inside each document
			for (List<String> t : documents) {
				// Search inside each word
				for (String s : t) {
					// Check if s word is not in the returning list
					if (!words.contains(s)) {
						words.add(s);
					}
				}
			}


			/*
			 * We sort the words list ( From LSI not necessary but made generally)
			 */
			Collections.sort(words);
			
		} catch(Exception e) {
			
		}
		
		return words;
	}

	/**
	 * We assign to each cell in the matrix the weight assigned 
	 * in the document for each word
	 * @author Nicolas Ordonez Chala
	 * @param words - List of possible words that are inside the document
	 * @param documents - List of documents
	 * @return Matrix where each column is each document and the rows have the 
	 * weight inside the document
	 */
	static public double[][] createWeightMatrix(ArrayList<String> words, ArrayList<HashMap<String, Integer>> documents){

		// Matrix where 
		// words.size() is the quantity of words that are available as rows 
		// list.size() is the number of documents as columns 
		double weight[][] = new double [words.size()][documents.size()];
		
		try {
			// Count the number of the column
			int i = 0;
			// Search inside each document
			for (HashMap<String, Integer> d : documents) {
				// Search inside each word available
				for (String w : words) {
					// If the word is present in the list of words
					if (d.containsKey(w))
						// Add the ocurrency?
						weight[words.indexOf(w)][i] = d.get(w);
					else
						// If a words isn't in the HashMap it means that the word
						// did appear in the document so we see it time
						weight[words.indexOf(w)][i] = 0;
				}
				i++;
			}
		} catch (Exception e) {
			
		}
		
		return weight;
	}
	
	/**
	 * We assign to each cell in the matrix the number of occurrences of each word 
	 * in the document for each word
	 * @author Nicolas Ordonez Chala
	 * @param words - List of possible words that are inside the document
	 * @param documents - List of documents
	 * @return Matrix where each column is each document and the rows have the 
	 * weight inside the document
	 */
	static public double[][] createWeightMatrixByNumberOfOccurrences(ArrayList<String> words, ArrayList<HashMap<String, Integer>> documents){

		// Matrix where 
		// words.size() is the quantity of words that are available as rows 
		// list.size() is the number of documents as columns 
		double weight[][] = new double [words.size()][documents.size()];
		
		try {
			// Count the number of the column
			int i = 0;
			// Search inside each document
			for (HashMap<String, Integer> d : documents) {
				// Search inside each word available
				for (String w : words) {
					// If the word is present in the list of words
					if (d.containsKey(w))
						// Add the ocurrency
						weight[words.indexOf(w)][i]++;
					else
						// If a words isn't in the HashMap it means that the word
						// did appear in the document so we see it time
						weight[words.indexOf(w)][i] = 0;
				}
				i++;
			}
		} catch (Exception e) {
			
		}
		
		return weight;
	}
	
	/**
	 * Get the weight of the query
	 * @param list
	 * @param map
	 * @return An array
	 */
	static public double[] createQuery(ArrayList<HashMap<String, Integer>> list, HashMap<String, Integer> map) {
		
		double answer[] = null;
		try {
			ArrayList<String> words = countDifferentWords(list);
			int cpt = words.size();

			if (cpt == 0)
				return null;
			
			ArrayList<HashMap<String,Integer>> arrayQuery = new ArrayList<>();
			arrayQuery.add(map);
			double tab[][] = createWeightMatrix(words, arrayQuery);
			answer = tab[0];
			
		}catch (IndexOutOfBoundsException e) {
			
		}

		return answer;
	}

	/**
	 * A Matrix which represents the words found in several documents In rows
	 * there are how many times a word is found in each document In columns
	 * there are how many times each words is found in a document
	 * 
	 * @param list
	 *            HashMap which contains for each document, words found and how
	 *            many times each word was found.
	 * @return A matrix
	 */
	public static double[][] createMatrix(ArrayList<HashMap<String, Integer>> list) {

		double matrix[][];
		
		try {
			ArrayList<String> words = countDifferentWords(list);
			int cpt = words.size();
			
			if (cpt == 0)
				return null;

			matrix = createWeightMatrix(words, list);
		} catch(NullPointerException exception) {
			matrix = null;
		}
		
		return matrix;
	}

	/**
	 * Calculate the cosine between two vector
	 * https://en.wikipedia.org/wiki/Cosine_similarity
	 * @param u
	 *            The vector U
	 * @param v
	 *            The vector V
	 * @return The cosine
	 */
	public static double cosine(double u[], double v[]) {
		/*
		 * the formula for cosine between vector U and V is ( U * V ) / ( ||U||
		 * * ||V|| )
		 */

		double scalaire = 0.0;
		for (int i = 0; i < u.length; i++)
			scalaire += u[i] * v[i];

		double normeU = 0.0;
		for (int i = 0; i < u.length; i++)
			normeU += u[i] * u[i];
		normeU = Math.sqrt(normeU);

		double normeV = 0.0;
		for (int i = 0; i < v.length; i++)
			normeV += v[i] * v[i];
		normeV = Math.sqrt(normeV);

		double val = scalaire / (normeU * normeV);

		if (Double.isNaN(val))
			val = -1;
		return val;
	}

}
