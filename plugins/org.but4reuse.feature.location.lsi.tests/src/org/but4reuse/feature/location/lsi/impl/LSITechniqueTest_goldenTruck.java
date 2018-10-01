package org.but4reuse.feature.location.lsi.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Nicolas Ordonez Chala
 *
 */
class LSITechniqueTest_goldenTruck {

	/**
	 * Delete extra decimals e.g.1 input = 0.10000000 - number of decimals = 3 -
	 * output: 0.1 e.g.2 input = 0.10100001 - number of decimals = 3 - output: 0.101
	 * 
	 * @author Nicolas Ordonez Chala
	 * @param input - Double number
	 * @param scale - Number of decimals
	 * @return double with scale decimal positions
	 */
	public static double setNumberOfDecimals(double input, int scale) {
		double output = Double.parseDouble("" + BigDecimal.valueOf(input).setScale(scale, BigDecimal.ROUND_HALF_UP));
		return output;
	}

	private final static int SCALE = 5;

	// Set query
	private static List<String> query;

	// Set documents
	private static List<List<String>> documents;

	/**
	 * http://www1.se.cuhk.edu.hk/~seem5680/lecture/LSI-Eg.pdf
	 */
	@Before
	public void setUp() {
		query = new ArrayList<String>();
		documents = new ArrayList<List<String>>();
		query.add("gold");
		query.add("silver");
		query.add("truck");

		// Set document number 1
		List<String> d1 = new ArrayList<String>();
		d1.add("a");
		d1.add("damaged");
		d1.add("fire");
		d1.add("gold");
		d1.add("in");
		d1.add("of");
		d1.add("shipment");

		// Set document number 2
		List<String> d2 = new ArrayList<String>();
		d2.add("a");
		d2.add("arrived");
		d2.add("delivery");
		d2.add("in");
		d2.add("of");
		d2.add("silver");
		d2.add("silver");
		d2.add("truck");

		// Set document number 3
		List<String> d3 = new ArrayList<String>();
		d3.add("a");
		d3.add("arrived");
		d3.add("gold");
		d3.add("in");
		d3.add("of");
		d3.add("shipment");
		d3.add("truck");

		// Add documents to the set of documents
		documents.add(d1);
		documents.add(d2);
		documents.add(d3);
	}


	@Test
	void goldenTruckApplyLSIwithoutLowerRankAproximation() {
		System.out.println("U " + -1/Math.pow(10, 4) );
		// Step 0 Set Up the variables
		setUp();
		
		// Step 1 Get similarity between the query and the documents
		LSITechnique lsiTechnique = new LSITechnique();
		double answer[] = lsiTechnique.applyLSI(query, documents);

		// Step 2 Show answer and their content round to 4 decimals
		System.out.println("GoldenTruck Example: Withouot Lower Rank K Value");
		for (double d : answer) {
			System.out.print(d + " = ");
			System.out.println(setNumberOfDecimals(d, SCALE));
		}

		// Step 3 The order supposed to be the same whatever was the k
		assertTrue(setNumberOfDecimals(answer[1], SCALE)>(setNumberOfDecimals(answer[2], SCALE)));
		assertTrue(setNumberOfDecimals(answer[2], SCALE)>(setNumberOfDecimals(answer[0], SCALE)));
	}
	
	@Test
	void goldenTruckApplyLSIwithFixedAndPredifinedSort() {
		// Step 0 Set Up the variables
		setUp();
		
		// Step 1 Get similarity between the query and the documents
		LSITechnique lsiTechnique = new LSITechnique(documents, LSITechnique.FIXED, 2, LSITechnique.PREDIFINED_SORT );
		double answer[] = lsiTechnique.applyLSI(query, documents);

		// Step 2 Show answer and their content round to 4 decimals
		System.out.println("GoldenTruck Example: FIXED AND PREDIFINED SORT");
		for (double d : answer) {
			System.out.print(d + " = ");
			System.out.println(setNumberOfDecimals(d, SCALE));
		}

		// Step 3 Compare answer with their expect result with an accepted error of +- 0.001
		assertEquals(-0.0541, setNumberOfDecimals(answer[0], SCALE), 0.001);
		assertEquals(0.9910, setNumberOfDecimals(answer[1], SCALE), 0.001);
		assertEquals(0.4478, setNumberOfDecimals(answer[2], SCALE), 0.001);
	}

	@Test
	void cosine() {
		// Step 0 Set Up the variables
		// 0.1 Query
		double q[] = { -0.2140, -0.1821 };

		// 0.2 Documents
		double d1[] = { -0.4945, 0.6492 };
		double d2[] = { -0.6458, -0.7194 };
		double d3[] = { -0.5817, 0.2469 };

		// Step 1 Get cosine distance
		double answer[] = { LSITechnique.cosine(q, d1, true, 4), LSITechnique.cosine(q, d2, true, 4), LSITechnique.cosine(q, d3, true, 4) };

		// Step 2 Show answer and their content round to 4 decimals
		System.out.println("Test: cosine");
		for (double d : answer) {
			System.out.print(d + " = ");
			System.out.println(setNumberOfDecimals(d, SCALE));
		}

		// Step 3 Compare answer with their expect result with an accepted error of +- 0.001
		assertEquals(-0.0541, setNumberOfDecimals(answer[0], SCALE), 0.001);
		assertEquals(0.9910, setNumberOfDecimals(answer[1], SCALE), 0.001);
		assertEquals(0.4478, setNumberOfDecimals(answer[2], SCALE), 0.001);
	}
	
	@Test
	void cosineOLD() {
		// Step 0 Set Up the variables
		// 0.1 Query
		double q[] = { -4.722033425638928E-19, -2.468242195039802E-17 };

		// 0.2 Documents
		double d1[] = { 0.1375769027896202, 0.3678132327620547 };

		// Step 1 Get cosine distance
		double answer[] = { FeatureLocationLSI.cosine(q, d1), LSITechnique.cosine(q, d1, false, 0) };

		// Step 2 Show answer and their content round to 4 decimals
		System.out.println("Test: cosine");
		for (double d : answer) {
			System.out.print(d + " = ");
			System.out.println(setNumberOfDecimals(d, SCALE));
		}

		// Step 3 Compare answer with their expect result with an accepted error of +- 0.001
		assertEquals(-0.94, setNumberOfDecimals(answer[0], SCALE), 0.01);
		assertEquals(-0.94, setNumberOfDecimals(answer[1], SCALE), 0.01);
	}

}
