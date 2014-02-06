package de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;

public abstract class GraphTraverser {
	protected int OVERFLOW_THRESHOLD = 10000;
	protected Map<ColumnCombinationBitset, PositionListIndex> calculatedPlis;
	
	protected int numberOfNegativeColumns;
	protected PruningGraph negativeGraph;
	protected PruningGraph positiveGraph;
	protected List<ColumnCombinationBitset> minimalPositives;
	protected List<ColumnCombinationBitset> maximalNegatives;
	
	protected Deque<ColumnCombinationBitset> randomWalkTrace;
	protected List<ColumnCombinationBitset> seedCandidates;
	protected HoleFinder holeFinder;

	protected String relationName;
	protected ImmutableList<String> namesOfNegativeColumns;
	
	protected Random random = new Random();
	
	public GraphTraverser() {	
		this.calculatedPlis = new HashMap<ColumnCombinationBitset, PositionListIndex>();
		
		this.randomWalkTrace = new LinkedList<ColumnCombinationBitset>();
		
		this.minimalPositives = new LinkedList<ColumnCombinationBitset>();
		this.maximalNegatives = new LinkedList<ColumnCombinationBitset>();
	}

	// TODO: find overlappings in init() and buildInitialPlis() in FD and UCC GraphTraverser
	
	public void traverseGraph() throws CouldNotReceiveResultException {
		//initial PLI
		ColumnCombinationBitset currentColumn = this.getSeed();
		
		while (null != currentColumn) {
			this.randomWalk(currentColumn);
			currentColumn = this.getSeed();
		}
	}

	protected void randomWalk(ColumnCombinationBitset currentColumnCombination) throws CouldNotReceiveResultException {
		ColumnCombinationBitset newColumn;
		while (null != currentColumnCombination) {
			// Check currentColumn

			if(this.isSubsetOfMaximalNegativeColumnCombination(currentColumnCombination)) {
				newColumn = null;
			}
			else if(this.isSupersetOfPositiveColumnCombination(currentColumnCombination)) {
				newColumn = null;
			}
			// UCC? --> get a child
			else if (this.isPositiveColumnCombination(currentColumnCombination)) {
				newColumn = getNextChildColumnCombination(currentColumnCombination);
				if (null == newColumn) {
					// No child available --> found minimal positive
					this.addMinimalPositive(currentColumnCombination);
					this.receiveResult(currentColumnCombination.createColumnCombination(this.relationName, this.namesOfNegativeColumns));
				}
				this.positiveGraph.add(currentColumnCombination);
			}
			// Get next parent and check for maximal negative
			else {
				newColumn = getNextParentColumnCombination(currentColumnCombination);
				// No parent available --> found maximal negative
				if (null == newColumn) {
					this.maximalNegatives.add(currentColumnCombination);
					this.holeFinder.update(currentColumnCombination);
				}
				this.negativeGraph.add(currentColumnCombination);
			}
			
			// Go to next column
			if (null != newColumn) {
				this.randomWalkTrace.push(currentColumnCombination);
				currentColumnCombination = newColumn;
			}
			else {
				if (randomWalkTrace.isEmpty()) {
					return;
					
				}
				currentColumnCombination = this.randomWalkTrace.poll();
			}
		}
	}

	protected abstract boolean isPositiveColumnCombination(ColumnCombinationBitset currentColumnCombination);

	protected ColumnCombinationBitset getSeed() {
		ColumnCombinationBitset seedCandidate = this.findUnprunedSetAndUpdateGivenList(this.seedCandidates, true);
		if (seedCandidate == null) {
			this.holeFinder.removeMinimalPositivesFromComplementarySet(this.minimalPositives);
			this.seedCandidates = this.holeFinder.getHoles();
			seedCandidate = this.findUnprunedSetAndUpdateGivenList(this.seedCandidates, true);
		}
		
		return seedCandidate;
	}
	
	protected PositionListIndex getPLIFor(ColumnCombinationBitset columnCombination) {
		PositionListIndex pli = this.calculatedPlis.get(columnCombination);
		if (pli != null) {
			return this.calculatedPlis.get(columnCombination);
		}
		
		pli = createPliFromExistingPli(columnCombination);
		return pli;
	}

	// FIXME add abort threshold 
	protected PositionListIndex createPliFromExistingPli(ColumnCombinationBitset columnCombination) {
		int removeColumns = 1;
		int numberOfColumns = columnCombination.size();
		// check all elements till the ceiling half of length 
		// 4 / 2 = 2 ==> (4+1) / 2 = 2
		// 5 / 2 = 2 ==> (5+1) / 2 = 3
		int minSetLength = (int)((numberOfColumns + 1) / 2);
		
		List<ColumnCombinationBitset> subsets;
		PositionListIndex foundPli;
		
		PositionListIndex currentMinusPli;
		ColumnCombinationBitset currentMinusSet;
		
		// initialize with worst case
		ColumnCombinationBitset largestExistingPliSet = columnCombination.getContainedOneColumnCombinations().get(0);
		ColumnCombinationBitset largestExistingPliMinusSet = columnCombination.minus(largestExistingPliSet);
		PositionListIndex largestExistingPli = null;
		
		while(numberOfColumns - removeColumns >= minSetLength) {
			subsets = columnCombination.getNSubsetColumnCombinations(numberOfColumns-removeColumns);
			for (ColumnCombinationBitset subset : subsets) {
				currentMinusSet = columnCombination.minus(subset);
				currentMinusPli = this.calculatedPlis.get(currentMinusSet);
				foundPli = this.calculatedPlis.get(subset);
				if(foundPli != null && largestExistingPli != null) {
					largestExistingPli = foundPli;
					largestExistingPliSet = subset;
					largestExistingPliMinusSet = currentMinusSet;
				}
				
				if (foundPli != null && currentMinusPli != null) {
					// calculate new PLI and return it
					PositionListIndex intersectedPli = foundPli.intersect(currentMinusPli);
					this.calculatedPlis.put(columnCombination, intersectedPli);
					return intersectedPli;
				}
			}
			
			removeColumns += 1;
		}
		
		return extendPli(largestExistingPliSet, largestExistingPliMinusSet);
	}

	/**
	 * columnCombination may not be null.
	 * 
	 * @param columnCombination
	 * @param extendingColumns
	 * @return
	 */
	protected PositionListIndex extendPli(ColumnCombinationBitset columnCombination, ColumnCombinationBitset extendingColumns) {
		PositionListIndex currentPli = this.calculatedPlis.get(columnCombination);
		
		for (ColumnCombinationBitset currentOneColumnCombination : extendingColumns.getContainedOneColumnCombinations()) {		
			currentPli = currentPli.intersect(this.calculatedPlis.get(currentOneColumnCombination));
			columnCombination = columnCombination.union(currentOneColumnCombination);
			this.calculatedPlis.put(columnCombination, currentPli);
		}
		
		return currentPli;
	}
	
	public Map<ColumnCombinationBitset, PositionListIndex> getCalculatedPlis() {
		return this.calculatedPlis;
	}
	
	protected ColumnCombinationBitset getNextParentColumnCombination(ColumnCombinationBitset column) {
		//TODO: check if only ducc specific
		if (this.minimalPositives.contains(column)) {
			return null;
		}
		List<ColumnCombinationBitset> supersets = column.getDirectSupersets(this.numberOfNegativeColumns);
		return findUnprunedSet(supersets);
	}
	
	protected ColumnCombinationBitset getNextChildColumnCombination(ColumnCombinationBitset column) {
		//TODO: check if only ducc specific
		if (this.maximalNegatives.contains(column)) {
			return null;
		}
		List<ColumnCombinationBitset> subsets = column.getDirectSubsets();
		return findUnprunedSet(subsets);
	}
	
	protected ColumnCombinationBitset findUnprunedSet(List<ColumnCombinationBitset> sets) {
		return this.findUnprunedSetAndUpdateGivenList(sets, false);
	}
	
	protected ColumnCombinationBitset findUnprunedSetAndUpdateGivenList(List<ColumnCombinationBitset> sets, boolean setPrunedEntriesToNull) {
		// Randomize order for random walk
		//Collections.shuffle(sets);
		
		if (sets.isEmpty()) {
			return null;
		}
		
		int random = this.random.nextInt(sets.size());		
		int i;
		int no;
		
		// TODO: use an iterator to be faster on the list
		for(i = 0; i < sets.size(); i++) {
			no = (i + random) % sets.size();
			ColumnCombinationBitset singleSet = sets.get(no);
			
			if (singleSet == null)
				continue;
			
			if (this.positiveGraph.find(singleSet)) {
				if (setPrunedEntriesToNull) {
					sets.set(no, null);
				}
				continue;
			}
			
			if (this.negativeGraph.find(singleSet)) {
				if (setPrunedEntriesToNull) {
					sets.set(no, null);
				}
				continue;
			}	
			return singleSet;
		}
		return null;
	}

	protected boolean isSupersetOfPositiveColumnCombination(ColumnCombinationBitset currentColumnCombination) {
		for(ColumnCombinationBitset ccb: this.minimalPositives) {
			if(ccb.isSubsetOf(currentColumnCombination)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isSubsetOfMaximalNegativeColumnCombination(ColumnCombinationBitset currentColumnCombination) {
		for(ColumnCombinationBitset ccb: this.maximalNegatives) {
			if(ccb.containsSubset(currentColumnCombination)) {
				return true;
			}
		}
		
		return false;
	}

	protected void addMinimalPositive(ColumnCombinationBitset positiveColumnCombination) {
		this.minimalPositives.add(positiveColumnCombination);
	}
	
	public Collection<ColumnCombinationBitset> getMinimalPositiveColumnCombinations() {
		return minimalPositives;
	}
	
	protected abstract void receiveResult(ColumnCombination result) throws CouldNotReceiveResultException;
}
