package minesweeper.ai.games;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import minesweeper.ai.utils.User32;

import com.sun.jna.platform.win32.WinDef.HWND;

import minesweeper.ai.utils.CellClassifier;

public class Windows7GameState implements GameState {
	
	private static final String PROGRAM_PATH = "C:/Program Files/Microsoft Games/Minesweeper/Minesweeper.exe";
	
	private Rectangle windowRect;
	private int rows, cols;
	private int bombs;
	private State state;
	private Set<Position> flagged = new HashSet<>();
	
	private Robot robot;
	private BufferedImage snapshot;
	private boolean upToDate;
	{ 
		try { robot = new Robot(); } 
		catch(Exception e) { System.exit(0);}
	}
	
	public static GameState createEasyGame() {
		return new Windows7GameState(9, 9, 10);
	}
	public static GameState createIntermediateGame() {
		return new Windows7GameState(16,16,40);
	}
	public static GameState createAdvancedGame() {
		return new Windows7GameState(16,30,99);
	}
	
	public Windows7GameState(int rows, int cols, int bombs) {
		this.rows = rows;
		this.cols = cols;
		this.bombs = bombs;
		state = State.IN_PROGRESS;
		startGame();
	}
	
	private void startGame() {
        HWND hwnd = User32.INSTANCE.FindWindow(null, "Minesweeper");
        if(hwnd == null) {
        	try {
				new ProcessBuilder(PROGRAM_PATH).start();
	        	sleep(2222);
	        	hwnd = User32.INSTANCE.FindWindow(null, "Minesweeper");
			} catch (IOException e) { e.printStackTrace(); }
        }
        User32.INSTANCE.SwitchToThisWindow(hwnd, false);
        takeNap();
        config();
        takeNap();
        int[] rect = { 0,0,0,0 };
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        windowRect = new Rectangle(rect[0], rect[1], rect[2]-rect[0], rect[3]-rect[1]);
        Rectangle r = CellClassifier.reduceBoard(robot.createScreenCapture(windowRect));
        windowRect.width = r.width;
        windowRect.height = r.height;
        windowRect.x += r.x;
        windowRect.y += r.y;
	}
	
	private void config() {
        robot.keyPress(KeyEvent.VK_F5);
        takeNap();
        HWND options = User32.INSTANCE.FindWindow(null, "Options");
        int[] r = {0,0,0,0};
        User32.INSTANCE.GetWindowRect(options, r);
        mouseClick(r[0]+200,r[1]+75);
        robot.keyPress(KeyEvent.VK_TAB);
        type(rows);
        robot.keyPress(KeyEvent.VK_TAB);
        type(cols);    
        robot.keyPress(KeyEvent.VK_TAB);
        type(bombs);   
        robot.keyPress(KeyEvent.VK_ENTER);     
        takeNap(); takeNap();
	}

	@Override
	public int getCell(int row, int col) {
		if(! upToDate) update();
		if(flagged.contains(Position.valueOf(row,col)))
			return Cell.FLAG;
		return CellClassifier.classify(cellImg(row,col));
	}
	
	private void update() {
		snapshot = robot.createScreenCapture(windowRect); 
		upToDate = true;
	}
	
	private BufferedImage cellImg(int row, int col) {
		Rectangle r = rectFromPosition(row,col);
		r.x -= windowRect.x; r.y -= windowRect.y;
		return snapshot.getSubimage(r.x, r.y, r.width, r.height);
	}

	@Override
	public int getCell(Position p) {
		return getCell(p.row, p.col);
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public int getCols() {
		return cols;
	}

	@Override
	public int getBombs() {
		return bombs;
	}

	@Override
	public int getRemainingBombs() {
		return bombs - flagged.size();
	}

	@Override
	public void pick(int row, int col) {
		upToDate = false;
		if(checkStatus() != State.IN_PROGRESS) return;
		Point pt = pointFromPosition(row, col);
		mouseClick(pt.x,pt.y);
	}

	@Override
	public void pick(Position p) {
		pick(p.row, p.col);
	}

	@Override
	public void flag(int row, int col) {
		upToDate = false;
		if(checkStatus() != State.IN_PROGRESS) return;
		flagged.add(Position.valueOf(row,col));
		Point pt = pointFromPosition(row, col);
		mouseRightClick(pt.x,pt.y);
	}

	@Override
	public void flag(Position p) {
		flag(p.row, p.col);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void restart() {
		// TODO Auto-generated method stub
		
	}
	
	public BufferedImage screenshot() {
		return robot.createScreenCapture(windowRect);
	}
	
	private Point pointFromPosition(int row, int col) {
		return new Point(windowRect.x+(col*windowRect.width+windowRect.width/2) / cols, windowRect.y+(row*windowRect.height+windowRect.height/2) / rows);
	}
	
	private Rectangle rectFromPosition(int row, int col) {
		Rectangle ans = new Rectangle(pointFromPosition(row,col));
		ans.x -= 10; ans.y -= 10; ans.width = 20; ans.height = 20;
		return ans;
	}
	
	private void takeNap() {
		sleep(44);
	}
	private void sleep(long millis) {
		try { Thread.sleep(millis); }
		catch(InterruptedException e) {}
	}
	
	private State checkStatus() {
		if(User32.INSTANCE.FindWindow("", "Game Lost") != null) 
			state = State.LOSE;
		if(User32.INSTANCE.FindWindow("", "Game Won") != null) 
			state = State.LOSE;
		return state;
	}
	
	private void mouseClick(int x, int y) {
		robot.mouseMove(x,y);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		takeNap();
	}
	
	private void mouseRightClick(int x, int y) {
		robot.mouseMove(x,y);
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		takeNap();
	}
	
    private static final int[] KEYEVENTS = {
            KeyEvent.VK_0,KeyEvent.VK_1,KeyEvent.VK_2,
            KeyEvent.VK_3,KeyEvent.VK_4,KeyEvent.VK_5,
            KeyEvent.VK_6,KeyEvent.VK_7,KeyEvent.VK_8,
            KeyEvent.VK_9
        };
	private void type(int n) {
		if(n >= 100) robot.keyPress(KEYEVENTS[n/100 % 10]);
		if(n >= 10) robot.keyPress(KEYEVENTS[n/10 % 10]);
		robot.keyPress(KEYEVENTS[n%10]);
		takeNap();
	}

}

