package de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;


public class PruningGraphFixture {
	
	protected ColumnCombinationBitset columnCombinationA = new ColumnCombinationBitset().setColumns(0);
	protected ColumnCombinationBitset columnCombinationB = new ColumnCombinationBitset().setColumns(1);
	protected ColumnCombinationBitset columnCombinationC = new ColumnCombinationBitset().setColumns(2);
	protected ColumnCombinationBitset columnCombinationD = new ColumnCombinationBitset().setColumns(3);
	
	
	protected ColumnCombinationBitset columnCombinationAB;
	protected ColumnCombinationBitset columnCombinationAC;
	protected ColumnCombinationBitset columnCombinationBC;
	protected ColumnCombinationBitset columnCombinationBD;
	protected ColumnCombinationBitset columnCombinationABC;
	
	public PruningGraphFixture() {
		// AB
		columnCombinationAB = new ColumnCombinationBitset().setColumns(0,1);
		
		// AC Column
		columnCombinationAC = new ColumnCombinationBitset().setColumns(0, 2);

		// BC Column
		columnCombinationBC = new ColumnCombinationBitset().setColumns(1, 2);

		//BD Column
		columnCombinationBD = new ColumnCombinationBitset().setColumns(1, 3);

		// ABC Column
		columnCombinationABC = new ColumnCombinationBitset().setColumns(0, 1, 2);	
	}
	
	protected List<ColumnCombinationBitset> getListOf(ColumnCombinationBitset... columnCombinations) {
		List<ColumnCombinationBitset> columnCombinationList = new LinkedList<ColumnCombinationBitset>();
		
		for (ColumnCombinationBitset columnCombination : columnCombinations) {
			columnCombinationList.add(columnCombination);
		}
		
		return columnCombinationList;		
	}
	
	public PruningGraph getGraphWith1Element() {
		PruningGraph actualGraph = new PruningGraph(5, 10, true);
		
		actualGraph.columnCombinationMap.put(columnCombinationB, getListOf(columnCombinationBC));
		actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));
		
		return actualGraph;
	}
	
	public PruningGraph getGraphWith2ElementAndOverflow() {
		PruningGraph actualGraph = new PruningGraph(5, 2, true);
		
		actualGraph.columnCombinationMap.put(columnCombinationB, actualGraph.OVERFLOW);
		actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));
		actualGraph.columnCombinationMap.put(columnCombinationBC, getListOf(columnCombinationBC));
		actualGraph.columnCombinationMap.put(columnCombinationBD, getListOf(columnCombinationBD));
		actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationBD));
		
		return actualGraph;
	}
	
	public PruningGraph getGraphWith2ElementAndOverflowNonUnique() {
		PruningGraph actualGraph = new PruningGraph(3, 2, false);
		
		actualGraph.columnCombinationMap.put(columnCombinationB, actualGraph.OVERFLOW);
		actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));
		actualGraph.columnCombinationMap.put(columnCombinationBC, getListOf(columnCombinationBC));
		actualGraph.columnCombinationMap.put(columnCombinationBD, getListOf(columnCombinationBD));
		actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationBD));
		
		return actualGraph;
	}
	
	public PruningGraph getGraphForMinimalUniques() {
		PruningGraph actualGraph = new PruningGraph(5, 10000, true);
		actualGraph.columnCombinationMap.put(columnCombinationA, getListOf(columnCombinationAB, columnCombinationAC));
		actualGraph.columnCombinationMap.put(columnCombinationB, getListOf(columnCombinationAB));
		actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationAC));
		actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationD));
		return actualGraph;
	}
	
	public Collection<ColumnCombinationBitset> getExpectedMinimalUniques() {
		Collection<ColumnCombinationBitset> expectedUniques = new HashSet<ColumnCombinationBitset>();
		expectedUniques.add(new ColumnCombinationBitset().setColumns(0,1));
		expectedUniques.add(new ColumnCombinationBitset().setColumns(0,2));
		expectedUniques.add(new ColumnCombinationBitset().setColumns(3));
		return expectedUniques;
	}
}
