package minesweeper.ai.games;

public class NativeGameState implements GameState {
	
	private boolean initialized; //flags don't get placed until first pick has been made
	private int[][] board;
	private int[][] key;
	private int bombs;
	
	private int flags;
	private int found;
	private State state;
	/**
	 * Returns a new instance of an Easy Game (as defined by Microsoft) <br/>
	 * @return A 9x9 grid with 10 bombs
	 */
	public static GameState createEasyGame() {
		return new NativeGameState(9,9,10);
	}
	/**
	 * Returns a new instance of an Intermediate Game (as defined by Microsoft) <br/>
	 * @return A 16x16 grid with 40 bombs
	 */
	public static GameState createIntermediateGame() {
		return new NativeGameState(16,16,40);
	}
	/**
	 * Returns a new instance of an Advanced Game (as defined by Microsoft) <br/>
	 * @return A 16x30 grid with 99 bombs
	 */
	public static GameState createAdvancedGame() {
		return new NativeGameState(16,30,99);
	}
	/**
	 * Creates a new instance of a Game with the given specifications
	 * @param rows The number of rows in the board
	 * @param cols The number of cols in the board
	 * @param bombs The number of hidden bombs on the board
	 */
	public NativeGameState(int rows, int cols, int bombs) {
		board = new int[rows][cols];
		key = new int[rows][cols];
		initBoard();
		this.bombs = bombs;
		this.flags = 0;
		this.found = 0;
		initialized = false;
		state = State.IN_PROGRESS;
	}
	
	@Override
	public void restart() {
		initBoard();
		this.flags = 0;
		this.found = 0;
		state = State.IN_PROGRESS;
	}

	@Override
	public void pick(int r, int c) {
        if(! inBounds(r,c) || getCell(r,c) != Cell.UNKNOWN || state != State.IN_PROGRESS) return;
		if(!initialized) initKey(r,c);
		
        if (key[r][c] == Cell.BOMB) {
            board[r][c] = Cell.BOMB;
            state = State.LOSE;
            //board = key; //show answer
        } else if (key[r][c] == 0) {
            board[r][c] = 0;
            pick(r + 1, c + 1);
            pick(r + 1, c);
            pick(r + 1, c - 1);
            pick(r, c + 1);
            pick(r, c - 1);
            pick(r - 1, c + 1);
            pick(r - 1, c);
            pick(r - 1, c - 1);
        } else {
            board[r][c] = key[r][c];
        }
        found++;
        if(found == key.length*key[0].length-bombs)
        	state = State.WIN;
	}

	@Override
	public void flag(int r, int c) {
		//assert getCell(r,c) < 0 : "Cannot flag this cell";
		if(state != State.IN_PROGRESS) return;
        if(board[r][c] == Cell.FLAG) {
            board[r][c] = Cell.UNKNOWN;
            flags -= 1;
        }
        else if(board[r][c] == Cell.UNKNOWN) {
            board[r][c] = Cell.FLAG;
            flags += 1;
        }
	}

	@Override
	public int getBombs() {
		return bombs;
	}

	@Override
	public int getRemainingBombs() {
		return bombs - flags;
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public int getCell(int r, int c) {
		if(inBounds(r,c))
			return board[r][c];
		return Cell.OUT_OF_BOUNDS;
	}
	
	@Override
	public int getRows() {
		return key.length;
	}
	
	@Override
	public int getCols() {
		return key[0].length;
	}
	
	@Override
	public void pick(Position p) {
		pick(p.row,p.col);
		
	}
	@Override
	public void flag(Position p) {
		flag(p.row,p.col);
		
	}
	@Override
	public int getCell(Position p) {
		return getCell(p.row,p.col);
	}
	
	private void initBoard() {
		for(int r=0; r < getRows(); r++)
			for(int c=0; c < getCols(); c++)
				board[r][c] = Cell.UNKNOWN;
	}
	
	private void initKey(int row, int col) {
		for (int k = 0; k < bombs; k++) {
            int r = (int) (Math.random() * key.length), c = (int) (Math.random() * key[0].length);
            while (key[r][c] == Cell.BOMB || isAdjacent(r,c,row,col)) {
                r = (int) (Math.random() * key.length);
                c = (int) (Math.random() * key[0].length);  
            }
            key[r][c] = Cell.BOMB;
        }
		for(int r=0; r < getRows(); r++)
			for(int c=0; c < getCols(); c++)
				if(key[r][c] != Cell.BOMB) 
					key[r][c] = getAdjacentBombs(r, c);
        initialized = true;
	}
	
    private boolean isAdjacent(int a, int b, int x, int y) {
        return Math.abs(a-x) <= 1 && Math.abs(b-y) <= 1;
    }
	
	private int getAdjacentBombs(int r, int c) {
        int result = 0;
        if(getKeyCell(r-1,c-1) == Cell.BOMB) result++;
        if(getKeyCell(r-1,c) == Cell.BOMB) result++;
        if(getKeyCell(r-1,c+1) == Cell.BOMB) result++;
        if(getKeyCell(r,c-1) == Cell.BOMB) result++;
        if(getKeyCell(r,c+1) == Cell.BOMB) result++;
        if(getKeyCell(r+1,c-1) == Cell.BOMB) result++;
        if(getKeyCell(r+1,c) == Cell.BOMB) result++;
        if(getKeyCell(r+1,c+1) == Cell.BOMB) result++;
        return result;
	}
	private int getKeyCell(int r, int c) {
		if(inBounds(r,c))
			return key[r][c];
		return Cell.OUT_OF_BOUNDS;
	}
    
    private boolean inBounds(int r, int c) {
    	return r < key.length && c < key[0].length && r >= 0 && c >= 0;
    }
    
    @Override
    public String toString() {
        String result = "Bombs Remaining: " + getRemainingBombs() + "\tCells Remaining: " + (key.length*key[0].length-found-bombs);
        for (int i = 0; i < board.length; i++) {
            result += "\n";
            for (int j = 0; j < board[0].length; j++)
                result += Cell.toString(board[i][j]) + "  ";
        }
        return result;
    }

}
