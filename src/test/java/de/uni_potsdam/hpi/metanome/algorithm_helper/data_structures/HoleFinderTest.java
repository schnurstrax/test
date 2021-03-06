package de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HoleFinderTest {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpdateFirst() {
		HoleFinder finder = new HoleFinder(5);
		
		ColumnCombinationBitset mNUCC = new ColumnCombinationBitset();
		mNUCC.setColumns(1,3,4);
		finder.update(mNUCC);

		ColumnCombinationBitset oneColumn0 = new ColumnCombinationBitset();
		oneColumn0.setColumns(0);
		ColumnCombinationBitset oneColumn2 = new ColumnCombinationBitset();
		oneColumn2.setColumns(2);
		
		assertEquals(2, finder.complementarySet.size());
		assertTrue(finder.complementarySet.contains(oneColumn0));
		assertTrue(finder.complementarySet.contains(oneColumn2));
	}

	@Test
	public void testUpdateAllOther() {
		HoleFinder finder = new HoleFinder(5);
		
		ColumnCombinationBitset firstCombi = new ColumnCombinationBitset();
		firstCombi.setColumns(1,3,4);
		finder.update(firstCombi);
		
		ColumnCombinationBitset nextCombi = new ColumnCombinationBitset();
		nextCombi.setColumns(2,3,4);
		finder.update(nextCombi);

		ColumnCombinationBitset column10000 = new ColumnCombinationBitset();
		column10000.setColumns(0);
		ColumnCombinationBitset column01100 = new ColumnCombinationBitset();
		column01100.setColumns(1,2);
		ColumnCombinationBitset column10100 = new ColumnCombinationBitset();
		column10100.setColumns(0,2);
		
		assertEquals(2, finder.complementarySet.size());
		assertTrue(finder.complementarySet.contains(column10000));
		assertTrue(finder.complementarySet.contains(column01100));
		assertFalse(finder.complementarySet.contains(column10100));
	}
	
	@Test
	public void testRemoveSets() {
		HoleFinder finder = new HoleFinder(5);
		
		ColumnCombinationBitset firstCombi = new ColumnCombinationBitset();
		firstCombi.setColumns(1,3,4);
		finder.update(firstCombi);
		
		ColumnCombinationBitset nextCombi = new ColumnCombinationBitset();
		nextCombi.setColumns(2,3,4);
		finder.update(nextCombi);
		
		ColumnCombinationBitset column10000 = new ColumnCombinationBitset();
		column10000.setColumns(0);
		ColumnCombinationBitset column01100 = new ColumnCombinationBitset();
		column01100.setColumns(1,2);

		assertEquals(2, finder.complementarySet.size());
		assertTrue(finder.complementarySet.contains(column10000));
		assertTrue(finder.complementarySet.contains(column01100));
		
		ArrayList<ColumnCombinationBitset> combinations = new ArrayList<ColumnCombinationBitset>();
		combinations.add(column01100);
		
		finder.removeMinimalPositivesFromComplementarySet(combinations);

		assertEquals(1, finder.complementarySet.size());
		assertTrue(finder.complementarySet.contains(column10000));
		assertFalse(finder.complementarySet.contains(column01100));
	}
	
	@Test
	public void testGet() {
		HoleFinder finder = new HoleFinder(5);
		
		ColumnCombinationBitset firstCombi = new ColumnCombinationBitset();
		firstCombi.setColumns(1,3,4);
		finder.update(firstCombi);
		
		List<ColumnCombinationBitset> result = finder.getHoles();

		ColumnCombinationBitset oneColumn0 = new ColumnCombinationBitset();
		oneColumn0.setColumns(0);
		ColumnCombinationBitset oneColumn2 = new ColumnCombinationBitset();
		oneColumn2.setColumns(2);
		
		assertEquals(2, result.size());
		assertTrue(result.contains(oneColumn0));
		assertTrue(result.contains(oneColumn2));
	}
}