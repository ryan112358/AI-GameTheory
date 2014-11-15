package minesweeper.ai.players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import minesweeper.ai.games.BoardInfoHelper;
import minesweeper.ai.games.GameState;
import minesweeper.ai.games.BoardConfiguration.Cell;
import minesweeper.ai.games.BoardConfiguration.Position;
import minesweeper.ai.games.GameState.State;
import minesweeper.ai.games.MutableBoard;
import minesweeper.ai.utils.Combinations;
import minesweeper.ai.utils.Node;

public class ProbablisticSearchTreeAI implements AIPlayer {
	
	private static final int SEARCH_SPACE_LIMIT = 25000;
	
	private DeductionType dt;
	private DebugMode debug;
	
	public ProbablisticSearchTreeAI() {
		this(DebugMode.OFF);
	}
	
	public ProbablisticSearchTreeAI(DebugMode debug) {
		this.debug = debug;
		toPickMap = new HashMap<>();
		toFlagMap = new HashMap<>();
		toPick = new HashSet<>();
		toFlag = new HashSet<>();
	}
	
	private Set<Position> toPick, toFlag;

	@Override
	public void solve(GameState game) {
		BoardInfoHelper helper = new BoardInfoHelper(game);
		game.pick(game.getRows()/2, game.getCols()/2);
		
		while(game.getState() == State.IN_PROGRESS) {
	        if(! deduce(helper.getBoard()))
	        	if(! backtrackSolve(helper.getBoard()))
	        		if(! bruteForceSearch(helper.getBoard()))
	        			if(! probablisticBacktrackSolve(helper.getBoard()))
			        		if(! pickRandom(helper.getBoard()))
			        			break;
	        pickAndFlag(game);
		}
		
		if(debug == DebugMode.ON) { 
			if(game.getState() == State.LOSE)
				System.out.println("Lost due to " + dt);
			System.out.println(game);
		}
	}
	
	private void pickAndFlag(GameState game) {
    	for(Position p : toPick) 
    		game.pick(p);
    	for(Position p : toFlag)
    		game.flag(p);
	}
	
	Position bestGuess = null; boolean shouldPick = false; float bestProb = 0; boolean probBacktrackValid = false;
	private void reset() {  bestGuess=null; shouldPick=false; bestProb=0; probBacktrackValid=false; };
	//work is done by backtrack
	private boolean probablisticBacktrackSolve(MutableBoard board) {
		toPick.clear(); toFlag.clear();
    	dt = DeductionType.PROBABLISTIC_BACKTRACK;
		if(probBacktrackValid) {
	    	if(bestGuess != null) {
	    		if(shouldPick) toPick.add(bestGuess);
	    		else toFlag.add(bestGuess);
	    		if(debug == DebugMode.ON)
	    			System.out.println("Making guess: "+(shouldPick?"picking ":"flagging ")+bestGuess+" - Probability: "+bestProb);
	    		reset();
	    		return true;
	    	} else if(debug == DebugMode.ON) System.out.println("Backtracking Failed!");
		}
		reset();
		return false;
	}
	
	private int solutionsFound;
	private Map<Position,Integer> toPickMap, toFlagMap;
	private boolean backtrackSolve(MutableBoard grid) {
		BoardInfoHelper helper = new BoardInfoHelper(grid);
		
    	for(List<Position> positions : helper.getClosedUnknownBorderCells()) {
    		probBacktrackValid = false;
	    	toPickMap.clear(); toFlagMap.clear(); solutionsFound = 0;
	    	toPick.clear(); toPick.addAll(positions); 
	    	toFlag.clear(); toFlag.addAll(positions);
	    	backtrackSolve(grid, helper, positions, new Node(null,null), new Node(null,null));
	    	if(toPick.size() == 0 && toFlag.size() == 0) {
	    		toPick.clear(); toFlag.clear();
	    		Entry<Position, Integer> pickMax = null, flagMax = null;
	    		if(toPickMap.size() >= 1) pickMax = Collections.max(toPickMap.entrySet(), (a,b) -> a.getValue()-b.getValue());
	    		if(toFlagMap.size() >= 1) flagMax = Collections.max(toFlagMap.entrySet(), (a,b) -> a.getValue()-b.getValue());
	    		if(pickMax != null)
	    			if((float) pickMax.getValue() / solutionsFound > bestProb) {
	    				bestGuess = pickMax.getKey();
	    				shouldPick = true;
	    				bestProb = (float) pickMax.getValue() / solutionsFound;
	    			}
	    		if(flagMax != null)
	    			if((float) flagMax.getValue() / solutionsFound > bestProb) {
	    				bestGuess = flagMax.getKey();
	    				shouldPick = false;
	    				bestProb = (float) flagMax.getValue() / solutionsFound;
	    			}
	    	} else {
		    	dt = DeductionType.BACKTRACK;
	    		return true;
	    	}
    	}
    	probBacktrackValid = true;
    	return false;
	}
	
	private void incrementAll(Map<Position,Integer> map, Iterable<Position> list) {
		for(Position p : list)
			map.put(p,map.getOrDefault(p,0)+1);
	}
	private boolean backtrackSolve(MutableBoard board, BoardInfoHelper helper, List<Position> positions, Node outPick, Node outFlag) {
		if(positions.isEmpty()) {
			solutionsFound++;
			toPick.retainAll(outPick.asSet());
			toFlag.retainAll(outFlag.asSet());
			incrementAll(toPickMap,outPick);
			incrementAll(toFlagMap,outFlag);
			return true;
		}
		Position p = positions.remove(0);
		board.setCell(p,Cell.NO_BOMB);
		boolean result = false;
		if(helper.validate(p))
			if(backtrackSolve(board,helper,positions,new Node(p,outPick),outFlag))
				result = true;
		board.setCell(p, Cell.FLAG);
		if(helper.validate(p)) 
			if(backtrackSolve(board,helper,positions,outPick,new Node(p,outFlag)))
				result = true;
		board.setCell(p, Cell.UNKNOWN);
		positions.add(0,p);
		return result;
	}
	
	private boolean pickRandom(MutableBoard board) {
		BoardInfoHelper helper = new BoardInfoHelper(board);
    	List<Position> unknown = helper.getUnknownBorderCells();
    	toPick.clear(); toFlag.clear();
    	if(unknown.size() == 0)
    		unknown = helper.getCellsByValue(Cell.UNKNOWN);
    	if(unknown.size() >= 1)
    		toPick.add(unknown.get((int) (unknown.size() * Math.random())));
    	dt = DeductionType.RANDOM;
    	return toPick.size() == 1;
	}
	
	private boolean deduce(MutableBoard board) {
		BoardInfoHelper helper = new BoardInfoHelper(board);
		toPick.clear(); toFlag.clear();
		for(Position cell : helper.getCellsWithAdjacentBombs()) {
            List<Position> unknown = helper.getAdjacentCellsByValue(cell, Cell.UNKNOWN);
            List<Position> flagged = helper.getAdjacentCellsByValue(cell, Cell.FLAG);
            if(board.getCell(cell) == unknown.size() + flagged.size())
                for(Position p : unknown)
                    toFlag.add(p);
            if(board.getCell(cell) == flagged.size())
                for(Position p : unknown)
                	toPick.add(p);
        }
		dt = DeductionType.NORMAL;
		return toPick.size() > 0 || toFlag.size() > 0;
	}
	
    private boolean bruteForceSearch(MutableBoard board) {
    	BoardInfoHelper helper = new BoardInfoHelper(board);
        List<Position> unknown = helper.getCellsByValue(Cell.UNKNOWN);
        int bombsToPlace = board.getRemainingBombs();
        toFlag.clear(); toPick.clear();
        if(Combinations.nCr(unknown.size(), bombsToPlace) < SEARCH_SPACE_LIMIT) {
        	toFlag.addAll(unknown); toPick.addAll(unknown);
        	enumerateBombLocations(helper, unknown, new Node(null,null), new Node(null,null), bombsToPlace);
        	dt = DeductionType.BRUTE_FORCE;
        	if(debug == DebugMode.ON) System.out.println("BRUTE FORCE results: " + toPick + toFlag); 
        	return toPick.size() > 0 || toFlag.size() > 0;
        } else return false;
    }
   
    private void enumerateBombLocations(BoardInfoHelper helper, List<Position> unknown, Node flagLocs, Node pickLocs, int bombsToPlace) {
        if(unknown.size() == 0) {
        	if(bombsToPlace==0) {
	        	if(helper.validateAll()) {
		            toPick.retainAll(pickLocs.asSet());
		            toFlag.retainAll(flagLocs.asSet());
	        	}
        	}
            return;
        } else if(bombsToPlace < 0 || bombsToPlace > unknown.size()) return;
        Position p = unknown.remove(0);
        enumerateBombLocations(helper,new ArrayList<>(unknown),new Node(p,flagLocs), pickLocs, bombsToPlace-1);
        enumerateBombLocations(helper,new ArrayList<>(unknown),flagLocs, new Node(p,pickLocs), bombsToPlace);
    }
	
	public String toString() {
		return "Probablistic Backtracking Search Tree AI";
	}

}
