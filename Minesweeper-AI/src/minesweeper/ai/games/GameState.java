package minesweeper.ai.games;

public interface GameState extends BoardConfiguration {

	public static enum State { WIN, LOSE, IN_PROGRESS };
	/**
	 * Picks the cell on the board (must be an UNKNOWN cell)
	 * @param row The row number of the cell
	 * @param col The column number of the cell
	 */
	public void pick(int row, int col);
	public void pick(Position p);
	/**
	 * Flags the cell on the board if its unknown, or unflags it if it's already flagged.
	 * @param row The row number of the cell
	 * @param col The column number of the cell
	 */
	public void flag(int row, int col);
	public void flag(Position p);

	/**
	 * Returns the state of the game
	 * @return WIN, LOSE, or IN_PROGRESS
	 */
	public State getState();
	/**
	 * Restarts this GameState.  Note that the locations of the bombs are exactly the same on restart.
	 */
	public void restart();

}
