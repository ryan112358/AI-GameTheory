package minesweeper.ai.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import minesweeper.ai.games.BoardConfiguration.Cell;
import minesweeper.ai.games.BoardConfiguration.Position;

/**
 * This is a class that contains utility methods that help an AI easily get
 * important information from a GameState
 *
 */
public class BoardInfoHelper {
	
	private BoardConfiguration board;
	
	public BoardInfoHelper(BoardConfiguration board) {
		this.board = board;
	}
	
	/**
	 * Returns a list of cells adjacent to the given position that has the given value
	 * @param p The position of the cell
	 * @param cellValue The cell value (ex. Cell.UNKNOWN or Cell.FLAG)
	 * @return a list of Positions adjacent to the given position of cells that have the given value
	 * @see GameState.Position
	 */
	public List<Position> getAdjacentCellsByValue(Position p, int cellValue) {
        return getAdjacentCellsByValue(p.row, p.col, cellValue);
	}
	
	public List<Position> getAdjacentCells(Position p) {
        return getAdjacentCells(p.row,p.col);
	}
	
	public List<Position> getAdjacentCellsWithAdjacentBombs(Position p) {
        return getAdjacentCellsWithAdjacentBombs(p.row,p.col);
	}
	
	/**
	 * Returns a list of cells adjacent to the given position that has the given value
	 * @param row The row number of the cell
	 * @param col The column number of the cell
	 * @param cellValue The cell value (ex. Cell.UNKNOWN or Cell.FLAG)
	 * @return a list of Positions adjacent to the given (row,col) of cells that have the given value
	 * @see GameState.Position
	 */
	public List<Position> getAdjacentCellsByValue(int row, int col, int cellValue) {
        return getAdjacentCells(row, col).stream().
        									filter(p->board.getCell(p)==cellValue).
        									collect(Collectors.toList());
	}
	
	public List<Position> getAdjacentCells(int row, int col) {
        List<Position> ans = new ArrayList<Position>();
        if(inBounds(row-1,col-1)) ans.add(Position.valueOf(row-1,col-1));
        if(inBounds(row-1,col)) ans.add(Position.valueOf(row-1,col));
        if(inBounds(row-1,col+1)) ans.add(Position.valueOf(row-1,col+1));
        if(inBounds(row,col-1)) ans.add(Position.valueOf(row,col-1));
        if(inBounds(row,col+1)) ans.add(Position.valueOf(row,col+1));
        if(inBounds(row+1,col-1)) ans.add(Position.valueOf(row+1,col-1));
        if(inBounds(row+1,col)) ans.add(Position.valueOf(row+1,col));
        if(inBounds(row+1,col+1)) ans.add(Position.valueOf(row+1,col+1));
        return ans;
	}
	
	/**
	 * Returns a list of cell positions from the board that have the given board
	 * @param cellValue The cell value (ex. Cell.UNKNOWN or Cell.FLAG)
	 * @return a list of Positions of cells that have the given value
	 */
	public List<Position> getCellsByValue(int cellValue) {
		return enumeratePositions().stream().
				filter(p->board.getCell(p) == cellValue).
				collect(Collectors.toList());
	}
	
	/**
	 * Returns a list of cell positions from the board that have a positive value
	 * @return a list of Positions of cells that have a value of 1,2,...,8
	 */
	public List<Position> getCellsWithAdjacentBombs() {
		return enumeratePositions().stream().
									filter(p->board.getCell(p) >= 1).
									collect(Collectors.toList());
	}
	
	public List<Position> getAdjacentCellsWithAdjacentBombs(int row, int col) {
        return getAdjacentCells(row, col).stream()
        								 .filter(p->board.getCell(p) > 0)
        								 .collect(Collectors.toList());
	}
	
	/**
	 * Returns cells that haven't been picked or flagged yet that are 
	 * bordering a cell that has been picked and has a positive value
	 * @return a list of Positions of cells that are unknown border cells
	 */
	public List<Position> getUnknownBorderCells() {
		Set<Position> ans = new HashSet<>();
		for(Position p : getCellsWithAdjacentBombs())
			ans.addAll(getAdjacentCellsByValue(p.row,p.col,Cell.UNKNOWN));
		List<Position> result = new ArrayList<>(ans);
		Collections.sort(result, (p1,p2) -> ((p1.row-p2.row)*board.getCols()+p1.col-p2.col));
		return result;
	}
	/**
	 * Returns a list of Positions of cells that are unknown border 
	 * cells and that are all part of the same closed off region
	 * @return
	 */
	public List<List<Position>> getClosedUnknownBorderCells() {
		return partition(getUnknownBorderCells());
	}
	
	/**
	 * @return A copy of the board as a 2 dimensional array of cells (ints)
	 */
	public MutableBoard getBoard() {
		int[][] ans = new int[board.getRows()][board.getCols()];
		enumeratePositions().stream().forEach(p -> ans[p.row][p.col]=board.getCell(p));
		return new MutableBoard(ans, board.getBombs());
	}
	
	public boolean validate(int row, int col) {
		for(Position p : getAdjacentCellsWithAdjacentBombs(row, col)) {
			List<Position> unknown = getAdjacentCellsByValue(p, Cell.UNKNOWN);
			List<Position> flags = getAdjacentCellsByValue(p, Cell.FLAG);
			int cellValue = board.getCell(p);
			if(flags.size() > cellValue || cellValue > flags.size()+unknown.size())
				return false;
		}
		return true;
	}
	
	public boolean validateAll() {
		for(Position p : getCellsWithAdjacentBombs()) {
			int u = getAdjacentCellsByValue(p, Cell.UNKNOWN).size();
			int f = getAdjacentCellsByValue(p, Cell.FLAG).size();
			int c = board.getCell(p);
			if(f > c || u + f < c) return false;
		}
		return true;
	}
	
	public boolean validate(Position p) {
		return validate(p.row, p.col);
	}
	
	private boolean inBounds(int r, int c) {
		return r < board.getRows() && c < board.getCols() && r >= 0 && c >= 0;
	}
	
	public List<Position> enumeratePositions() {
		List<Position> result = new ArrayList<>();
		for(int r=0; r<board.getRows(); r++)
			for(int c=0; c<board.getCols(); c++)
				result.add(Position.valueOf(r,c));
		return result;
	}
	
    private boolean isAdjacent(Position p1, Position p2) {
        return Math.abs(p1.row-p2.row) <= 1 && Math.abs(p1.col-p2.col) <= 1;
    }
    
    
    /** Jacek's Code */

	private boolean sharesElement(List<Position> a, List<Position> b) {
        for (Position c : a)
            for (Position d : b)
                if (isAdjacent(c,d)) return true;	
	        return false;
	}
	
	private List<List<Position>> partition(List<Position> input) {
		List<Position> data = new ArrayList<Position>(input);
		List<List<Position>> output = new ArrayList<List<Position>>();

		while (data.size() > 0) {
			Position first = data.get(0);
			List<Position> toadd = new ArrayList<Position>();
			output.add(toadd);

			Iterator<Position> it = data.iterator();

			while (it.hasNext()) {
				Position current = it.next();
				if (isAdjacent(first, current)) {
					it.remove();
					toadd.add(current);
				}
			}
		}

		boolean changeMade = true;
		while (changeMade) {

			changeMade = false;

			for (int i = 0; i < output.size(); i++) {

				List<Position> first = output.get(i);

				for (int j = 0; j < output.size(); j++) {
					if (i != j && sharesElement(first, output.get(j))) {
						changeMade = true;
						first.addAll(output.get(j));
						output.remove(j);
						if (i > j)
							i--;
						j--;
					}
				}

			}

		}

		for (List<Position> result : output) {
			Collections
					.sort(result,
							(p1, p2) -> ((p1.row - p2.row) * board.getCols()
									+ p1.col - p2.col));
		}

		Collections.sort(output, (a1, a2) -> a1.size() - a2.size());

		return output;

	}
}
