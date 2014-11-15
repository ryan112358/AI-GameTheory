package minesweeper.ai.players;

import minesweeper.ai.games.GameState;

public interface AIPlayer {
	//Will hopefully extend these options to allow for more complex debugging features
	public static enum DebugMode { ON, OFF };
	static enum DeductionType { RANDOM, NORMAL, BACKTRACK, PROBABLISTIC_BACKTRACK, BRUTE_FORCE };
	
	public void solve(GameState game);

}
