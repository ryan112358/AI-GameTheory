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

public class BruteForceAI implements AIPlayer {
	
	private static final int SEARCH_SPACE_LIMIT = 50000;
	
	private DebugMode debug;
	
	public BruteForceAI() {
		this(DebugMode.OFF);
	}
	public BruteForceAI(DebugMode debug) { 
		this.debug = debug;
		toFlag = new HashSet<>();
		toPick = new HashSet<>();
	}
	
    private Set<Position> toFlag, toPick;

	@Override
	public void solve(GameState game) {
		BoardInfoHelper helper = new BoardInfoHelper(game);
		game.pick(game.getRows()/2, game.getCols()/2);
		while(game.getState() == State.IN_PROGRESS) {
	        if(! deduce(helper.getBoard()))
	        	if(! bruteForceSearch(helper.getBoard()))
	        		if(! pickRandom(helper.getBoard()))
	        			break;
	        pickAndFlag(game);
		}
		if(debug == DebugMode.ON) System.out.println(game); 
	}
	
	private void pickAndFlag(GameState game) {
    	for(Position p : toPick) 
    		game.pick(p);
    	for(Position p : toFlag)
    		game.flag(p);
	}
	
	private boolean pickRandom(MutableBoard board) {
		BoardInfoHelper helper = new BoardInfoHelper(board);
    	List<Position> unknown = helper.getUnknownBorderCells();
    	toPick.clear(); toFlag.clear();
    	if(unknown.size() == 0)
    		unknown = helper.getCellsByValue(Cell.UNKNOWN);
    	if(unknown.size() >= 1)
    		toPick.add(unknown.get((int) (unknown.size() * Math.random())));
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
		return toPick.size() > 0 || toFlag.size() > 0;
	}
	
    private boolean bruteForceSearch(MutableBoard board) {
    	BoardInfoHelper helper = new BoardInfoHelper(board);
        List<Position> unknown = helper.getCellsByValue(Cell.UNKNOWN);
        int bombsToPlace = board.getRemainingBombs();
        toFlag.clear(); toPick.clear();
        if(Combinations.nCr(unknown.size(), bombsToPlace) < SEARCH_SPACE_LIMIT) {
        	toFlag.addAll(unknown); toPick.addAll(unknown);
        	enumerateBombLocations(helper, board, unknown, new Node(null,null), new Node(null,null), bombsToPlace);
        	return toPick.size() > 0 || toFlag.size() > 0;
        } else return false;

    }
   
    private void enumerateBombLocations(BoardInfoHelper helper, MutableBoard board, List<Position> unknown, Node flagLocs, Node pickLocs, int bombsToPlace) {
        if(unknown.size() == 0) {
        	if(bombsToPlace==0) {
	        	for(Position pick : pickLocs)
	        		board.setCell(pick, Cell.NO_BOMB);
	        	for(Position flag : flagLocs)
	        		board.setCell(flag, Cell.FLAG);
	        	if(helper.validateAll()) {
		            toPick.retainAll(pickLocs.asSet());
		            toFlag.retainAll(flagLocs.asSet());
	        	}
        	}
            return;
        } else if(bombsToPlace < 0 || bombsToPlace > unknown.size()) return;
        Position p = unknown.remove(0);
        enumerateBombLocations(helper,board,new ArrayList<>(unknown),new Node(p,flagLocs), pickLocs, bombsToPlace-1);
        enumerateBombLocations(helper,board,new ArrayList<>(unknown),flagLocs, new Node(p,pickLocs), bombsToPlace);
    }
	
	@Override
	public String toString() {
		return "Basic Deductive AI with Brute Force End Game Strategy";
	}

}
