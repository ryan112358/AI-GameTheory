package minesweeper.ai.games;

public interface BoardConfiguration {
	
	/**
	 * A static class that contains constants for Minesweeper cells. <br/>
	 * Note: Cells with values 0-8 do not have special constants assigned to them.
	 */
	static class Cell {
		//Cells with values don't need constants (0-8)
		public static final int UNKNOWN = -1;
		public static final int FLAG = -2;
		public static final int BOMB = -3;
		public static final int OUT_OF_BOUNDS = -4;
		public static final int NO_BOMB = -5;
		public static String toString(int value) {
			switch(value) {
			case UNKNOWN: return "?";
			case FLAG: return "F";
			case BOMB: return "B";
			case NO_BOMB: return "S";
			default: return String.valueOf(value);
			}
		}
	}
	/**
	 * A simple class for a (row,col) pair
	 */
	static class Position {
		public int row;
		public int col;
		private static Position[][] cache = new Position[50][50];
		static {
			for(int i=0; i < 50; i++)
				for(int j=0; j < 50; j++)
					cache[i][j] = new Position(i,j);
		}
		private Position(int r, int c) {
			row = r;
			col = c;
		}
		public static Position valueOf(int r, int c) {
			if(r < 50 && c < 50)
				return cache[r][c];
			return new Position(r,c);
		}
		public int hashCode() { return row*100+col; }
		public boolean equals(Object other) { return row == ((Position) other).row && col == ((Position) other).col; }
		public String toString() { return "("+row+", "+col+")"; }
	}
	
	/**
	 * Returns the value of the cell at the given position
	 * @param row The row number of the cell
	 * @param col The column number of the cell
	 * @return The cell value at the given position
	 */
	public int getCell(int row, int col);
	public int getCell(Position p);
	/**
	 * @return The number of rows in the board
	 */
	public int getRows();
	/**
	 * @return The number of cols in the board
	 */
	public int getCols();
	
	/**
	 * Returns the total number of bombs set for this game.
	 * @return The number of bombs
	 */
	public int getBombs();
	/**
	 * Returns the number of bombs left assuming all flags are on bombs
	 * @return The number of remaining bombs
	 */
	public int getRemainingBombs();

}
