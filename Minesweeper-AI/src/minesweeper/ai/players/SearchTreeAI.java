package minesweeper.ai.players;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import minesweeper.ai.games.BoardInfoHelper;
import minesweeper.ai.games.GameState;
import minesweeper.ai.games.BoardConfiguration.Cell;
import minesweeper.ai.games.BoardConfiguration.Position;
import minesweeper.ai.games.GameState.State;
import minesweeper.ai.games.MutableBoard;
import minesweeper.ai.utils.Combinations;
import minesweeper.ai.utils.Node;

public class SearchTreeAI implements AIPlayer {
	
	private static final int SEARCH_SPACE_LIMIT = 25000;
	
	private DeductionType dt;
	private DebugMode debug;
	
	public SearchTreeAI() {
		this(DebugMode.OFF);
	}
	
	public SearchTreeAI(DebugMode debug) {
		this.debug = debug;
		toPick = new HashSet<>();
		toFlag = new HashSet<>();
	}
	
	private Set<Position> toPick;
	private Set<Position> toFlag;

	@Override
	public void solve(GameState game) {
		BoardInfoHelper helper = new BoardInfoHelper(game);
		game.pick(game.getRows()/2, game.getCols()/2);
		while(game.getState() == State.IN_PROGRESS) {
	        if(! deduce(helper.getBoard()))
	        	if(! backtrackSolve(helper.getBoard()))
	        		if(! bruteForceSearch(helper.getBoard()))
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
	
	public boolean backtrackSolve(MutableBoard grid) {
		BoardInfoHelper helper = new BoardInfoHelper(grid);
    	for(List<Position> positions : helper.getClosedUnknownBorderCells()) {
//    		if(positions.size() > 15)
//    			positions = positions.subList(0, 15);
	    	toPick.clear(); toPick.addAll(positions);
	    	toFlag.clear(); toPick.addAll(positions);
	    	backtrackSolve(grid, helper, positions, new Node(null,null), new Node(null,null));
	    	dt = DeductionType.BACKTRACK;
	    	if(toPick.size() > 0 || toFlag.size() > 0)
	    		return true;
    	}
    	return false;
	}
	
	public boolean backtrackSolve(MutableBoard board, BoardInfoHelper helper, List<Position> positions, Node outPick, Node outFlag) {
		if(positions.isEmpty()) {
			toPick.retainAll(outPick.asSet());
			toFlag.retainAll(outFlag.asSet());
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
		toPick.clear(); toFlag.clear();
		BoardInfoHelper helper = new BoardInfoHelper(board);
    	List<Position> unknown = helper.getUnknownBorderCells();
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
		return "Backtracking Search Tree AI";
	}

}
