package minesweeper.ai.games;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class MutableBoard implements BoardConfiguration {
	
	private int[][] board;
	private int bombs;
	private int flags;
	
	public MutableBoard(int[][] board, int bombs) {
		this.board = board;
		this.bombs = bombs;
		this.flags = (int) Arrays.stream(board).
				flatMapToInt(x -> Arrays.stream(x)).
				filter(c->c==Cell.FLAG).count();
	}
	
	public void setCell(int row, int col, int cellValue) {
		if(board[row][col] == Cell.FLAG) flags--;
		if(cellValue == Cell.FLAG) flags++;
		board[row][col] = cellValue;
	}
	
	public void setCell(Position p, int cellValue) {
		setCell(p.row, p.col, cellValue);
	}

	@Override
	public int getCell(int row, int col) {
		if(inBounds(row,col))
			return board[row][col];
		return Cell.OUT_OF_BOUNDS;
	}

	@Override
	public int getCell(Position p) {
		return getCell(p.row, p.col);
	}

	@Override
	public int getRows() {
		return board.length;
	}

	@Override
	public int getCols() {
		return board[0].length;
	}

	@Override
	public int getBombs() {
		return bombs;
	}

	@Override
	public int getRemainingBombs() {
		return bombs - flags;
	}
	
	private boolean inBounds(int r, int c) {
		return r < board.length && c < board[0].length && r >= 0 && c >= 0;
	}
	
    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++)
                result += Cell.toString(board[i][j]) + "  ";
            result += "\n";
        }
        return result;
    }
    
    public static MutableBoard readFromFile(int rows, int cols, String file) {
    	Scanner is = null;
		try {
			is = new Scanner(new File(file));
		} catch (FileNotFoundException e) {}
    	int[][] ans = new int[rows][cols];
    	for(int r=0; r<rows; r++)
    		for(int c=0; c<cols; c++)
    			ans[r][c] = parseCell(is.next());
    	is.close();
    	return new MutableBoard(ans,0);
    }
    
    private static int parseCell(String cell) {
    	switch(cell) {
    	case "?": return Cell.UNKNOWN;
    	case "F": return Cell.FLAG;
    	case "B": return Cell.BOMB;
    	case "S": return Cell.NO_BOMB;
    	default: return Integer.parseInt(cell);
    	}
    }

}
