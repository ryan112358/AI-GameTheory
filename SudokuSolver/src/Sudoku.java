import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sudoku {
	
	int[][] grid = new int[9][9];
	
	/*
	 * Sudoku Constructor
	 * Takes in a puzzle (unknown values must be given as 0)
	 */
	public Sudoku(int[][] puzzle) {
		this.grid = puzzle;
	}
	
	/*
	 * Attempts to solve the Sudoku puzzle
	 * returns true on success, false otherwise
	 * NOTE: This function mutates the original data structure
	 */
	public boolean solve() {
		return backtrackSolve(0,0);
	}
	
	private boolean backtrackSolve(int i, int j) {
		if(i == 9) 
			return true;
		if(grid[i][j] != 0)
			return backtrackSolve(i + (j+1)/9, (j+1)%9);
		
		for(int x: getMoves(i,j)) {
			grid[i][j] = x;
			if(backtrackSolve(i + (j+1)/9, (j+1)%9))
				return true;
		}
		grid[i][j] = 0;
		return false;
	}
	
	private List<Integer> getMoves(int i, int j) {
		List<Integer> result = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
		for(int k=0; k<9; k++) {
			result.remove(new Integer(grid[k][j]));
			result.remove(new Integer(grid[i][k]));
		}	
		for(int m = (i/3)*3; m<(i/3+1)*3; m++)
			for(int n = (j/3)*3; n<(j/3+1)*3; n++)
				result.remove(new Integer(grid[m][n]));
		return result;
	}
	
	/*
	 * Solves a Sudoku puzzle under the additional constraint that the 2 diagonals must 
	 * have all the numbers from 1 to 9.  Returns true on success, false otherwise
	 */
	public boolean solveDiag() {
		return solveDiag(0,0);
	}
	
	
	private boolean solveDiag(int i, int j) {
		if(i == 9)
			return true;
		if(grid[i][j] != 0)
			return solveDiag(i + (j+1)/9, (j+1)%9);
		
		for(int x: getMovesDiag(i,j)) {
			grid[i][j] = x;
			if(solveDiag(i + (j+1)/9, (j+1)%9))
				return true;
		}
		grid[i][j] = 0;
		return false;
	}
	
	private List<Integer> getMovesDiag(int i, int j) {
		List<Integer> result = getMoves(i,j);
		if(i == j)
			for(int m = 0; m < i; m++)
				result.remove(grid[m][m]);
		if(i == 8 - j)
			for(int m = 0; m < i; m++)
				result.remove(grid[m][8-m]);
		return result;
	}
	
	public String toString() {
		String s = "";
		for(int i=0; i<grid.length; i++) {
			for(int j=0; j<grid.length; j++)
				s += grid[i][j] + " ";
			s+="\n";
		}
		return s;
	}

}
