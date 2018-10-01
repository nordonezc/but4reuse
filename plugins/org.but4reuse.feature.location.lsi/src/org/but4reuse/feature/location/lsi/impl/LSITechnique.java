package org.but4reuse.feature.location.lsi.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

// Use Jama Matrix Library
// https://math.nist.gov/javanumerics/jama/doc/

// Another Java Libraries
// https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/

// Apache Common Math Library
// http://commons.apache.org/proper/commons-math/apidocs/index.html

//Java Matrix performance comparison
// http://lessthanoptimal.github.io/Java-Matrix-Benchmark/runtime/2013_10_Corei7v2600/
public class LSITechnique {

	// What kind of Low rank approximation want to apply
	public static final int FIXED = 0;
	public static final int PERCENTAGE = 1;

	// Sort the terms
	public static final int ALPHABETICAL_SORT = 1;
	public static final int PREDIFINED_SORT = 0;

	// Decide if sort or not the words
	private static int sortTypeTerms;

	// Singular Value Descomposition Matrix
	private static List<String> terms;
	private SingularValueDecomposition svd;

	// Double[] documents and query
	private static double[][] documentsMatrix;
	private static double[] queryMatrix;

	// Lower Rank Descomposition (lra) Value and Type
	private static int lowerRankApproximationFinal;

	/**
	 * Recommended to just one set of documents
	 * 
	 * @param documents
	 * @param lraType
	 * @param lraValue
	 * @param sortTermsFromDocuments
	 */
	public LSITechnique(List<List<String>> documents, int lraType, double lraValue, int sortTermsFromDocuments) {

		// Step 1 Get and sort terms
		sortTypeTerms = sortTermsFromDocuments;
		terms = getTermsFromList(documents);
		sortTerms();

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = createDoubleMatrixFromList(terms, documents, true);
		Matrix a = new Matrix(documentsMatrix);
		svd = a.svd();

		// Step 3 Calculate the Low K Final Value
		Matrix s = svd.getS();
		calculateLowKFinalValue(s, lraValue, lraType);
	}

	/**
	 * Recommended to just one set of documents
	 * 
	 * @param documents
	 * @param lraType
	 * @param lraValue
	 * @param sortTermsFromDocuments
	 */
	public LSITechnique(List<List<String>> documents, int lraType, double lraValue, int sortTermsFromDocuments,
			List<String> givenTerms) {

		// Step 1 Get and sort terms
		sortTypeTerms = 0;
		if (givenTerms != null) {
			terms = givenTerms;
			for (String t : terms) {
				t = t.toLowerCase();
			}
		} else {
			terms = getTermsFromList(documents);
		}
		sortTerms();

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = createDoubleMatrixFromList(terms, documents, true);
		Matrix a = new Matrix(documentsMatrix);
		svd = a.svd();

		// Step 3 Calculate the Low K Final Value
		calculateLowKFinalValue(svd.getS(), lraValue, lraType);
	}

	/**
	 * Recommended to just one set of documents
	 * 
	 * @param documents
	 * @param lraType
	 * @param lraValue
	 */
	public LSITechnique(List<List<String>> documents, int lraType, double lraValue) {
		// Step 1 Get and sort terms
		sortTypeTerms = ALPHABETICAL_SORT;
		terms = getTermsFromList(documents);
		sortTerms();

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = createDoubleMatrixFromList(terms, documents, true);
		Matrix a = new Matrix(documentsMatrix);
		svd = a.svd();

		// Step 3 Calculate the Low K Final Value
		calculateLowKFinalValue(svd.getS(), lraValue, lraType);
	}

	/**
	 * Recommended to just one set of documents
	 * 
	 * @param documents
	 */
	public LSITechnique(List<List<String>> documents) {
		// Step 1 Get and sort terms
		sortTypeTerms = ALPHABETICAL_SORT;
		terms = getTermsFromList(documents);
		sortTerms();

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = createDoubleMatrixFromList(terms, documents, true);
		Matrix a = new Matrix(documentsMatrix);
		svd = a.svd();

		// Step 3 Calculate the Low K Final Value
		lowerRankApproximationFinal = svd.getS().getRowDimension();
	}

	/**
	 * Used when it is needed to different documents along the procedure
	 */
	public LSITechnique() {
		// Step 1 Get and sort terms
		sortTypeTerms = PREDIFINED_SORT;

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = null;
		svd = null;

		// Step 3 Calculate the Low K Final Value
		lowerRankApproximationFinal = 0;
	}

	/**
	 * LowerKType is FIXED by default in this constructor
	 * 
	 * @param lowerKValue
	 */
	public LSITechnique(int lowerKValue) {
		// Step 1 Get and sort terms
		sortTypeTerms = PREDIFINED_SORT;

		// Step 2 Get the double matrix and calculate SVG
		queryMatrix = null;
		documentsMatrix = null;
		svd = null;

		// Step 3 Calculate the Low K Final Value
		lowerRankApproximationFinal = lowerKValue;
	}

	// Based on query as double

	/**
	 * Apply the LSI technique based on inputs
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query                       - weights
	 * @param documents                   - weights
	 * @param lowerRankApproximationValue
	 * @param lowerRankApproximationType
	 * @return List of similarities of the query for each document
	 */
	public double[] applyLSI(double[] query, int lowerRankApproximationType, double lowerRankApproximationValue) {

		double answer[];

		try {
			// Step 1
			// Get the input
			// ...

			// Step 2
			// Decompose matrix a from the set of documents

			// A = U*S*(V^T)
			// Done in the constructor
			if (svd == null)
				return null;

			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();

			// Step 3
			calculateLowKFinalValue(s, lowerRankApproximationValue, lowerRankApproximationType);

			// if lowerk is greater than max possible option. Select s row dimension by
			// default
			int lowerKApprox = (int) Math.min(lowerRankApproximationFinal, s.getRowDimension());

			// Get the value of the filter
			// double dim =
			// Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
			// int nbDim =
			// Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED)
			// ? (int) dim
			// : ((int) dim * s.getRowDimension());

			/*
			 * Here, rand-reduce The the U and S matrix are sorted by singular value the
			 * highest to the smallest so we just remove the last rows and columns.
			 */
			s = s.getMatrix(0, lowerKApprox - 1, 0, lowerKApprox - 1);
			u = u.getMatrix(0, u.getRowDimension() - 1, 0, lowerKApprox - 1);
			v = v.getMatrix(0, v.getRowDimension() - 1, 0, lowerKApprox - 1);
			Matrix vkT = v.transpose();

			// Step 4
			// Get coor of each document
			// Equal to vk

			// Step 5
			// Find new query vector q=(q^T)*uk*(sk^-1)
			Matrix q = new Matrix(query, query.length);
			q = q.transpose();
			q = q.times(u);
			q = q.times(s.inverse());

			double q_array[] = q.getColumnPackedCopy();

			// Step 6 Rank in decresing order of query-document cosine similarities.
			answer = new double[vkT.getColumnDimension()];

			for (int i = 0; i < vkT.getColumnDimension(); i++) {
				// Get each d matrix from vk
				Matrix di = vkT.getMatrix(0, vkT.getRowDimension() - 1, i, i);
				double di_array[] = di.getColumnPackedCopy();

				// cosine similarity between di and q
				answer[i] = cosine(di_array, q_array, true, 4);
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply the LSI technique rounding all the values inside the matrix
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query                       - weights
	 * @param documents                   - weights
	 * @param lowerRankApproximationValue
	 * @param lowerRankApproximationType
	 * @return List of similarities of the query for each document
	 */

	public double[] applyLSI(double[] query, int lowerRankApproximationType, double lowerRankApproximationValue,
			boolean roundMatrix, int scale) {

		double answer[];

		try {
			// Step 1
			// Get the input
			// ...

			// Step 2
			// Decompose matrix a from the set of documents
			// Use Jama Matrix Library
			// https://math.nist.gov/javanumerics/jama/doc/

			// Another Java Libraries
			// https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/

			// A = U*S*(V^T)
			// Done in the constructor
			if (svd == null)
				return null;

			Matrix u, s, v;

			u = svd.getU();
			s = svd.getS();
			v = svd.getV();

			// Step 3
			calculateLowKFinalValue(s, lowerRankApproximationValue, lowerRankApproximationType);

			// if lowerk is greater than max possible option. Select s row dimension by
			// default
			int lowerKApprox = (int) Math.min(lowerRankApproximationFinal, s.getRowDimension());

			// Get the value of the filter
			// double dim =
			// Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
			// int nbDim =
			// Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED)
			// ? (int) dim
			// : ((int) dim * s.getRowDimension());

			/*
			 * Here, rand-reduce The the U and S matrix are sorted by singular value the
			 * highest to the smallest so we just remove the last rows and columns.
			 */
			s = s.getMatrix(0, lowerKApprox - 1, 0, lowerKApprox - 1);
			u = u.getMatrix(0, u.getRowDimension() - 1, 0, lowerKApprox - 1);
			v = v.getMatrix(0, v.getRowDimension() - 1, 0, lowerKApprox - 1);
			Matrix vkT = v.transpose();

			// Step 4
			// Get coor of each document
			// Equal to vk

			// Step 5
			// Find new query vector q=(q^T)*uk*(sk^-1)
			Matrix q = new Matrix(query, query.length);
			q = q.transpose();
			q = q.times(u);
			q = q.times(s.inverse());

			double q_array[] = q.getColumnPackedCopy();

			// Step 6 Rank in decresing order of query-document cosine similarities.
			answer = new double[vkT.getColumnDimension()];

			for (int i = 0; i < vkT.getColumnDimension(); i++) {
				// Get each d matrix from vk
				Matrix di = vkT.getMatrix(0, vkT.getRowDimension() - 1, i, i);
				double di_array[] = di.getColumnPackedCopy();

				// cosine similarity between di and q
				answer[i] = cosine(di_array, q_array, roundMatrix, scale);
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply the LSI technique based on inputs
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query             - weights
	 * @param documents         - weights
	 * @param k
	 * @param rankApproximation
	 * @return List of similarities of the query for each document
	 */
	public double[] applyLSI(double[] query) {

		double answer[];

		try {
			// Step 1
			// Get the input
			// ...

			// Step 2
			// Decompose matrix a from the set of documents
			// Use Jama Matrix Library
			// https://math.nist.gov/javanumerics/jama/doc/

			// Another Java Libraries
			// https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/

			if (svd == null) {
				return null;
			}
			// A = U*S*(V^T)
			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();

			// Step 3
			// Implement low rank (k) approximation where K

			// if lowerk is greater than max possible option. Select s row dimension by
			// default
			int lowerKApprox = (int) Math.min(lowerRankApproximationFinal, s.getRowDimension());

			// Get the value of the filter
			// double dim =
			// Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
			// int nbDim =
			// Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED)
			// ? (int) dim
			// : ((int) dim * s.getRowDimension());

			/*
			 * Here, rand-reduce The the U and S matrix are sorted by singular value the
			 * highest to the smallest so we just remove the last rows and columns.
			 */
			s = s.getMatrix(0, lowerKApprox - 1, 0, lowerKApprox - 1);
			u = u.getMatrix(0, u.getRowDimension() - 1, 0, lowerKApprox - 1);
			v = v.getMatrix(0, v.getRowDimension() - 1, 0, lowerKApprox - 1);
			Matrix vkT = v.transpose();

			// Step 4
			// Get coor of each document
			// Equal to vk

			// Step 5
			// Find new query vector q=(q^T)*uk*(sk^-1)
			Matrix q = new Matrix(query, query.length);
			q = q.transpose();
			q = q.times(u);
			q = q.times(s.inverse());

			double q_array[] = q.getColumnPackedCopy();

			// Step 6 Rank in decresing order of query-document cosine similarities.
			answer = new double[vkT.getColumnDimension()];

			for (int i = 0; i < vkT.getColumnDimension(); i++) {
				// Get each d matrix from vk
				Matrix di = vkT.getMatrix(0, vkT.getRowDimension() - 1, i, i);
				double di_array[] = di.getColumnPackedCopy();

				// cosine similarity between di and q
				answer[i] = cosine(di_array, q_array, true, 4);
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply the LSI technique based on inputs
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query     - weights
	 * @param documents - weights
	 * @return List of similarities of the query for each document
	 */
	public double[] applyLSI(double[] query, double[][] documents) {

		double answer[];

		try {
			// Step 1
			// Get the input
			// ...

			// Step 2
			// Decompose matrix a from the set of documents
			// Use Jama Matrix Library
			// https://math.nist.gov/javanumerics/jama/doc/

			// Another Java Libraries
			// https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/

			if (svd == null) {
				Matrix a = new Matrix(documents);
				svd = a.svd();
			}
			// A = U*S*(V^T)
			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();

			// Step 3
			// Implement low rank (k) approximation where K

			// if lowerk is greater than max possible option. Select s row dimension by
			// default
			if (lowerRankApproximationFinal <= 0)
				lowerRankApproximationFinal = s.getRowDimension();
			int lowerKApprox = (int) Math.min(lowerRankApproximationFinal, s.getRowDimension() - 1);

			// Get the value of the filter
			// double dim =
			// Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
			// int nbDim =
			// Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED)
			// ? (int) dim
			// : ((int) dim * s.getRowDimension());

			/*
			 * Here, rand-reduce The the U and S matrix are sorted by singular value the
			 * highest to the smallest so we just remove the last rows and columns.
			 */
			s = s.getMatrix(0, lowerKApprox - 1, 0, lowerKApprox - 1);
			u = u.getMatrix(0, u.getRowDimension() - 1, 0, lowerKApprox - 1);
			v = v.getMatrix(0, v.getRowDimension() - 1, 0, lowerKApprox - 1);
			Matrix vkT = v.transpose();

			// Step 4
			// Get coor of each document
			// Equal to vk

			// Step 5
			// Find new query vector q=(q^T)*uk*(sk^-1)
			Matrix q = new Matrix(query, query.length);
			q = q.transpose();
			q = q.times(u);
			q = q.times(s.inverse());

			double q_array[] = q.getColumnPackedCopy();

			// Step 6 Rank in decresing order of query-document cosine similarities.
			answer = new double[vkT.getColumnDimension()];

			for (int i = 0; i < vkT.getColumnDimension(); i++) {
				// Get each d matrix from vk
				Matrix di = vkT.getMatrix(0, vkT.getRowDimension() - 1, i, i);
				double di_array[] = di.getColumnPackedCopy();

				// cosine similarity between di and q
				answer[i] = cosine(di_array, q_array, true, 4);
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply the LSI technique based on inputs
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query                       - weights
	 * @param documents                   - weights
	 * @param lowerRankApproximationValue
	 * @param lowerRankApproximationType
	 * @return List of similarities of the query for each document
	 */
	public double[] applyLSI(double[] query, double[][] documents, double lowerRankApproximationValue,
			int lowerRankApproximationType) {

		double answer[];

		try {
			// Step 1
			// Get the input
			// ...

			// Step 2
			// Decompose matrix a from the set of documents
			// Use Jama Matrix Library
			// https://math.nist.gov/javanumerics/jama/doc/

			// Another Java Libraries
			// https://statr.me/2015/09/an-overview-of-linear-algebra-libraries-in-scala-java/

			if (svd == null) {
				Matrix a = new Matrix(documents);
				svd = a.svd();
			}

			// A = U*S*(V^T)
			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();

			// Step 3
			// Implement low rank (k) approximation where K
			calculateLowKFinalValue(s, lowerRankApproximationValue, lowerRankApproximationType);
			int lowerKApprox = (int) Math.min(lowerRankApproximationFinal, s.getRowDimension());

			// Get the value of the filter
			// double dim =
			// Activator.getDefault().getPreferenceStore().getDouble(LSIPreferencePage.DIM);
			// int nbDim =
			// Activator.getDefault().getPreferenceStore().getBoolean(LSIPreferencePage.FIXED)
			// ? (int) dim
			// : ((int) dim * s.getRowDimension());

			/*
			 * Here, rand-reduce The the U and S matrix are sorted by singular value the
			 * highest to the smallest so we just remove the last rows and columns.
			 */
			s = s.getMatrix(0, lowerKApprox - 1, 0, lowerKApprox - 1);
			u = u.getMatrix(0, u.getRowDimension() - 1, 0, lowerKApprox - 1);
			v = v.getMatrix(0, v.getRowDimension() - 1, 0, lowerKApprox - 1);
			Matrix vkT = v.transpose();

			// Step 4
			// Get coor of each document
			// Equal to vk

			// Step 5
			// Find new query vector q=(q^T)*uk*(sk^-1)
			Matrix q = new Matrix(query, query.length);
			q = q.transpose();
			q = q.times(u);
			q = q.times(s.inverse());

			double q_array[] = q.getColumnPackedCopy();

			// Step 6 Rank in decresing order of query-document cosine similarities.
			answer = new double[vkT.getColumnDimension()];

			for (int i = 0; i < vkT.getColumnDimension(); i++) {
				// Get each d matrix from vk
				Matrix di = vkT.getMatrix(0, vkT.getRowDimension() - 1, i, i);
				double di_array[] = di.getColumnPackedCopy();

				// cosine similarity between di and q
				answer[i] = cosine(di_array, q_array, true, 4);
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	// Based on query as List
	/**
	 * Apply LSI just by query based on documents in the constructor
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @param k
	 * @param rankApproximation
	 * @return
	 */
	public double[] applyLSI(List<String> query) {

		double answer[];

		try {
			// Step 1 Get the different terms
			if (terms == null)
				return null;

			// Step 2 Create the query
			queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Step 3 Create the documents
			if (documentsMatrix == null)
				return null;

			// Create SVD Matrix
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Setp 4 Get the array of similarity
			answer = applyLSI(queryMatrix);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply LSI just by query based on documents in the constructor
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @param k
	 * @param rankApproximation
	 * @return
	 */
	public double[] applyLSI(List<String> query, int lowerKType, double lowerKValue) {

		double answer[];

		try {
			// Step 1 Get the different terms
			if (terms == null)
				return null;

			// Step 2 Create the query
			queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Step 3 Create the documents
			if (documentsMatrix == null)
				return null;

			// Create SVD Matrix
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Setp 4 Get the array of similarity
			answer = applyLSI(queryMatrix, lowerKType, lowerKValue);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply LSI giving options to set the lower rank approximation parameters
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @param lowerRankApproximationValue - From 1
	 * @param lowerRankApproximationType  - FIXED or PERCENTAGE
	 * @return
	 */
	public double[] applyLSI(List<String> query, List<List<String>> documents, double lowerRankApproximationValue,
			int lowerRankApproximationType) {

		double answer[];

		try {
			// Get the different terms
			List<String> terms = getTermsFromList(documents);

			// Create the query
			if (queryMatrix == null)
				queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Create the matrix of documents
			if (documentsMatrix == null)
				documentsMatrix = createDoubleMatrixFromList(terms, documents, true);

			// Create SVD Matrix
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Get the array of similarity
			answer = applyLSI(queryMatrix, documentsMatrix, lowerRankApproximationValue, lowerRankApproximationType);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply LSI giving options to set the lower rank approximation parameters and
	 * also rounding all the values in the matrix if round matrix its true
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @param lowerRankApproximationType
	 * @param lowerRankApproximationValue
	 * @param roundMatrix
	 * @param scale
	 * @param typeRound
	 * @return
	 */
	public double[] applyLSI(List<String> query, List<List<String>> documents, int lowerRankApproximationType,
			double lowerRankApproximationValue, boolean roundMatrix, int scale) {

		double answer[];

		try {
			// Get the different terms
			List<String> terms = getTermsFromList(documents);

			// Create the query
			if (queryMatrix == null)
				queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Create the matrix of documents
			if (documentsMatrix == null)
				documentsMatrix = createDoubleMatrixFromList(terms, documents, true);

			// Create SVD Matrix
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Get the array of similarity
			answer = applyLSI(queryMatrix, lowerRankApproximationType, lowerRankApproximationValue, roundMatrix, scale);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply LSI doing the entire process. Calculate the SVG and K values in default
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @return
	 */
	public double[] applyLSI(List<String> query, List<List<String>> documents) {

		double answer[];

		try {
			// Step 1 Get the different terms
			if (terms == null) {
				terms = getTermsFromList(documents);
				sortTerms();
			}

			// Create the query
			if (queryMatrix == null)
				queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Create the matrix of documents
			if (documentsMatrix == null)
				documentsMatrix = createDoubleMatrixFromList(terms, documents, true);

			// Step 4 Calculate SVG
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Get the array of similarity
			answer = applyLSI(queryMatrix, documentsMatrix);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	/**
	 * Apply LSI giving options to set the lower rank approximation parameters and
	 * select if it is wanted to be sorted the list of terms (It does not affect the
	 * result of the LSI algorithm just affect the order of the rows).
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param query
	 * @param documents
	 * @param lowerRankApproximationValue - From 1
	 * @param lowerRankApproximationType  - FIXED or PERCENTAGE
	 * @param sortTerms                   - ALPHABETICAL or PREDIFINED. For better
	 *                                    performance select PREDEFINED
	 * @return
	 */
	public double[] applyLSI(List<String> query, List<List<String>> documents, double lowerRankApproximationValue,
			int lowerRankApproximationType, int sortTerms) {

		double answer[];

		try {
			// Step 1 Get the different terms
			List<String> terms = getTermsFromList(documents);
			sortTypeTerms = sortTerms;
			sortTerms();

			// Create the query
			if (queryMatrix == null)
				queryMatrix = createDoubleQueryFromList(terms, query, true);

			// Create the matrix of documents
			if (documentsMatrix == null)
				documentsMatrix = createDoubleMatrixFromList(terms, documents, true);

			// Create SVD Matrix
			if (svd == null) {
				Matrix a = new Matrix(documentsMatrix);
				svd = a.svd();
			}

			// Step 4 Get the array of similarity
			answer = applyLSI(queryMatrix, documentsMatrix, lowerRankApproximationValue, lowerRankApproximationType);

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;

	}

	// List<String> uniqueWords (not repeated terms) inside documents
	/**
	 * 
	 * /** We count all different words In the matrix we must have for each words
	 * how many times it was found in the document even if it's 0
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param documents
	 * @return
	 */
	protected static List<String> getTermsFromMap(List<Map<String, Integer>> documents) {
		List<String> words = new ArrayList<String>();

		try {
			// Search inside each document
			for (Map<String, Integer> t : documents) {
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
			if (sortTypeTerms == 0) {
				Collections.sort(words);
			}
			// As was encountered in documents
			else if (sortTypeTerms == 1) {

			}
			// by default order
			else {
				Collections.sort(words);
			}

		} catch (Exception e) {
			words = null;
			e.printStackTrace();
		}

		return words;
	}

	/**
	 *
	 * We count all different words In the matrix we must have for each words how*
	 * many times it was found in the document even if it's0**
	 * 
	 * @author Nicolas Ordonez Chala**
	 * 
	 * @param documents
	 * 
	 * @return
	 */
	protected static List<String> getTermsFromList(List<List<String>> documents) {
		List<String> words = new ArrayList<String>();

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
		} catch (Exception e) {
			words = null;
			e.printStackTrace();
		}

		return words;
	}

	// Double[][] from documents
	/**
	 * We assign to each cell in the matrix the weight assigned in the document for
	 * each word
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param terms     - List of possible words that are inside the document
	 * @param documents - List of documents
	 * @return Matrix where each column is each document and the rows have the
	 *         weight inside the document
	 */
	protected static double[][] getDoubleMatrixFromMap(List<String> terms, List<Map<String, Integer>> documents) {

		// Matrix where
		// words.size() is the quantity of words that are available as rows
		// list.size() is the number of documents as columns
		double weight[][] = new double[terms.size()][documents.size()];

		try {
			// Count the number of the column
			int i = 0;
			// Search inside each document
			for (Map<String, Integer> d : documents) {
				// Search inside each word available
				for (String w : terms) {
					// If the word is present in the list of words
					if (d.containsKey(w))
						// Get the ocurrency from the map
						weight[terms.indexOf(w)][i] = d.get(w);
					else
						// If a words isn't in the HashMap it means that the word
						// did appear in the document so we see it time
						weight[terms.indexOf(w)][i] = 0;
				}
				i++;
			}
		} catch (Exception e) {
			weight = null;
			e.printStackTrace();
		}

		return weight;
	}

	/**
	 * We assign to each cell in the matrix the number of occurrences of each word
	 * in the document for each word
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param terms     - List of possible words that are inside the document
	 * @param documents - List of documents
	 * @return Matrix where each column is each document and the rows have the
	 *         weight inside the document
	 */
	protected static double[][] createDoubleMatrixFromList(List<String> terms, List<List<String>> documents,
			boolean caseSensible) {
		// terms.size() is the quantity of words that are available as rows
		// documents.size() is the number of documents as columns
		double weight[][] = new double[terms.size()][documents.size()];

		try {

			// Search inside each document
			for (int di = 0; di < documents.size(); di++) {
				// Search inside each term

				for (int ti = 0; ti < terms.size(); ti++) {
					// If the word is present in the list of words
					List<String> d = documents.get(di);
					weight[ti][di] = 0;
					if (d.size() > 0) {
						for (String w : d) {
							// verify if documents is case sensible
							if (caseSensible) {
								w = w.toLowerCase();
								terms.set(ti, terms.get(ti).toLowerCase());
							}

							if (terms.get(ti).equals(w))
								// Add the ocurrency
								weight[ti][di]++;
						}
					}
				}
			}
		} catch (Exception e) {
			weight = null;
			e.printStackTrace();
		}

		return weight;
	}

	// Double[] from query
	/**
	 * Get the weight of the query as a Map of documents
	 * 
	 * @param list
	 * @param query
	 * @return An array
	 */
	protected static double[] getDoubleQueryFromMap(List<String> terms, Map<String, Integer> query) {
		double answer[];
		try {

			answer = new double[terms.size()];
			int i = 0;
			// Search inside each word available
			for (String w : terms) {
				// If the word is present in the list of words
				if (query.containsKey(w))
					// Get the ocurrency from the map
					answer[i] = query.get(w);
				else
					answer[i] = 0;
				i++;
			}
		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;
	}

	/**
	 * Get the weight of the query
	 * 
	 * @param list
	 * @param query
	 * @return An array
	 */
	protected static double[] createDoubleQueryFromList(List<String> terms, List<String> query, boolean caseSensible) {
		double answer[];
		try {
			answer = new double[terms.size()];
			int i = 0;
			for (String t : terms) {
				answer[i] = 0;
				for (String q : query) {
					// Verify if is case sensible
					if (caseSensible) {
						t = t.toLowerCase();
						q.toLowerCase();
					}
					if (t.equals(q))
						answer[i]++;
				}
				i++;
			}

		} catch (Exception e) {
			answer = null;
			e.printStackTrace();
		}

		return answer;
	}

	// Matrix Operation
	/**
	 * Calculate the cosine between two vector
	 * https://en.wikipedia.org/wiki/Cosine_similarity
	 * 
	 * @param u The vector U
	 * @param v The vector V
	 * @return The cosine
	 */
	protected static double cosine(double u[], double v[], boolean applyScale, int scale) {
		/*
		 * the formula for cosine between vector U and V is ( U * V ) / ( ||U|| * ||V||
		 * )
		 */

		double scalaire = 0.0;
		double normeU = 0.0;
		double normeV = 0.0;

		// The smallest number allowed must be at least
		// -0.1
		if (applyScale) {
			if (scale <= 0)
				scale = 1;
			double smallestNumberAllowed = -1 / Math.pow(10, scale);

			for (int i = 0; i < u.length; i++) {
				// The value of the matrix must be higher than 0 and smaller than the smallest
				// number allowed. Otherwise, it will be rounded to zero
				if (u[i] < 0 && u[i] >= smallestNumberAllowed)
					u[i] = 0;
				if (v[i] < 0 && v[i] >= smallestNumberAllowed)
					v[i] = 0;

				scalaire += (u[i] * v[i]);
				normeU += Math.pow(u[i], 2);
				normeV += Math.pow(v[i], 2);
			}

		} else {
			for (int i = 0; i < u.length; i++) {
				scalaire += (u[i] * v[i]);
				normeU += Math.pow(u[i], 2);
				normeV += Math.pow(v[i], 2);
			}
		}

		normeU = Math.sqrt(normeU);
		normeV = Math.sqrt(normeV);
		double val = scalaire / (normeU * normeV);

		if (Double.isNaN(val))
			val = -1;
		return val;
	}

	// Some methods that help to understand the constructors
	/**
	 * Sort the terms
	 * 
	 * @author Nicolas Ordonez Chala
	 */
	protected static void sortTerms() {
		/*
		 * We sort the words list ( From LSI not necessary but made generally)
		 */
		if (sortTypeTerms == ALPHABETICAL_SORT) {
			Collections.sort(terms);
		}
		// As was encountered in documents
		else if (sortTypeTerms == PREDIFINED_SORT) {

		}
		// by default order
		else {
			Collections.sort(terms);
		}
	}

	/**
	 * Get the k to the low rank approximation
	 * 
	 * @author Nicolas Ordonez Chala
	 */
	protected static void calculateLowKFinalValue(Matrix s, double lraValue, int lraType) {
		// Step 3
		// Implement low rank (k) approximation where K
		double lowerK = 0;

		// if fixed -> number of columns to reduce
		if (lraType == FIXED)
			lowerK = (int) lraValue;

		// if fixed -> percentage of columns to reduce
		else if (lraType == PERCENTAGE)
			lowerK = s.getRowDimension() * lraValue;

		// if other -> not reduce
		else
			lowerK = s.getRowDimension();

		// if lowerk is greater than max possible option. Select s row dimension by
		// default
		lowerRankApproximationFinal = (int) Math.min(lowerK, s.getRowDimension());
	}

	// Round to Zero values that are too small

	/**
	 * Delete extra decimals e.g.1 input = 0.10000000 - number of decimals = 3 -
	 * output: 0.1 e.g.2 input = 0.10100001 - number of decimals = 3 - output: 0.101
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param input           - Double[][] number
	 * @param scale           - number of decimal positions. Recommend 4
	 * @param bigDecimalRound - e.g. BigDecimal.ROUND_HALF_UP
	 */
	protected Matrix roundMatrixValuesLowerThan(Matrix inputMatrix, int scale) {
		double[][] input = inputMatrix.getArray();
		double[][] output = new double[input.length][input[0].length];

		// The smallest number allowed must be at least
		// -0.1
		if (scale <= 0)
			scale = 1;
		double smallestNumberAllowed = -1 / Math.pow(10, scale);

		for (int f = 0; f < input.length; f++) {
			for (int c = 0; c < input[f].length; c++) {
				try {
					// The value of the matrix must be higher than 0 and smaller than the smallest
					// number allowed. Otherwise, it will be rounded to zero
					if (input[f][c] < 0 && input[f][c] >= smallestNumberAllowed)
						output[f][c] = (input[f][c]);
				} catch (NullPointerException e) {
					output[f][c] = 0;
				}
			}
		}
		Matrix outputMatrix = new Matrix(output);
		return outputMatrix;

	}

	// Round matrix
	/**
	 * Delete extra decimals e.g.1 input = 0.10000000 - number of decimals = 3 -
	 * output: 0.1 e.g.2 input = 0.10100001 - number of decimals = 3 - output: 0.101
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param input           - Double[][] number
	 * @param scale           - number of decimal positions. Recommend 4
	 * @param bigDecimalRound - e.g. BigDecimal.ROUND_HALF_UP
	 */
	@Deprecated
	protected double[][] roundDoubleMatrix(double[][] input, int scale, int bigDecimalRound) {
		double[][] output = new double[input.length][input[0].length];
		for (int f = 0; f < input.length; f++) {
			for (int c = 0; c < input[f].length; c++) {
				try {
					output[f][c] = Double
							.parseDouble("" + BigDecimal.valueOf(input[f][c]).setScale(scale, bigDecimalRound));
				} catch (NullPointerException e) {
					output[f][c] = 0;
				}
			}
		}

		return output;

	}

	/**
	 * Delete extra decimals e.g.1 input = 0.10000000 - number of decimals = 3 -
	 * output: 0.1 e.g.2 input = 0.10100001 - number of decimals = 3 - output: 0.101
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param input           - Double[][] number
	 * @param scale           - number of decimal positions. Recommend 4
	 * @param bigDecimalRound - e.g. BigDecimal.ROUND_HALF_UP
	 */
	@Deprecated
	protected Matrix roundMatrix(Matrix inputMatrix, int scale, int bigDecimalRound) {
		double[][] input = inputMatrix.getArray();
		double[][] output = new double[input.length][input[0].length];
		for (int f = 0; f < input.length; f++) {
			for (int c = 0; c < input[f].length; c++) {
				try {
					output[f][c] = Double
							.parseDouble("" + BigDecimal.valueOf(input[f][c]).setScale(scale, bigDecimalRound));
				} catch (NullPointerException e) {
					output[f][c] = 0;
				}
			}
		}
		Matrix outputMatrix = new Matrix(output);
		return outputMatrix;

	}

}
