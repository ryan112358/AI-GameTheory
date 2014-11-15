package minesweeper.ai.players;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import minesweeper.ai.games.BoardInfoHelper;
import minesweeper.ai.games.GameState;
import minesweeper.ai.games.BoardConfiguration.Cell;
import minesweeper.ai.games.BoardConfiguration.Position;
import minesweeper.ai.games.GameState.State;
import minesweeper.ai.games.MutableBoard;

public class SimpleAI implements AIPlayer {
	
	private DebugMode debug;
	
	public SimpleAI() {
		this(DebugMode.OFF);
	}
	public SimpleAI(DebugMode debug) { 
		this.debug = debug;
		toPick = new HashSet<>();
		toFlag = new HashSet<>();
	}
	/* My algorithms can't return a Set, so we must have them mutate an existing Set */
	private Set<Position> toPick;
	private Set<Position> toFlag;

	@Override
	public void solve(GameState game) {
		BoardInfoHelper helper = new BoardInfoHelper(game);

		game.pick(game.getRows()/2, game.getCols()/2);
		while(game.getState() == State.IN_PROGRESS) {
	        if(! deduce(helper.getBoard()))
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
	
	@Override
	public String toString() {
		return "Basic Deductive AI";
	}

}
