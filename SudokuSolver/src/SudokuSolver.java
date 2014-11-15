import java.io.IOException;
import java.util.Scanner;

public class SudokuSolver {
	
	/*
	 * Reads space delimited Sudoku puzzle from standard in
	 */
	public static Sudoku readSudoku() {
		Scanner in = new Scanner(System.in);
		int[][] puzzle = new int[9][9];
		for(int i=0; i < 9; i++)
			for(int j=0; j < 9; j++)
				puzzle[i][j] = in.nextInt();
		in.close();
		return new Sudoku(puzzle);
	}
	
	public static void main(String[] args) throws IOException {
		long t0 = System.currentTimeMillis();
		
		Sudoku sudoku = readSudoku();
		sudoku.solve();
		
		System.out.println(sudoku);
		long tf = System.currentTimeMillis();
		System.out.println((tf - t0) + " ms");

	}

}
