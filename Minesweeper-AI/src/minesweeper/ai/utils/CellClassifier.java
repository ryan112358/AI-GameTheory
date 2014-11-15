package minesweeper.ai.utils;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import minesweeper.ai.games.BoardConfiguration.Cell;

public class CellClassifier {
	
	private static Color[] COLORS = new Color[9];
	private static Color UNKNOWN = new Color(75,175,79), UNKNOWN2 = new Color(102,202,107);
	static {
		COLORS[0] = new Color(217,247,219);
		COLORS[1] = new Color(63,78,199);
		COLORS[2] = new Color(27,108,1);
		COLORS[3] = new Color(174,5,10);
		COLORS[4] = new Color(6,1,121);
		COLORS[5] = new Color(129,0,0);
	}
	
	public static int classify(BufferedImage img) {
		for(int i=1; i < 6; i++)
			if(anyMatch(img, COLORS[i]))
				return i;
		if(anyMatch(img, UNKNOWN) || anyMatch(img,UNKNOWN2))
			return Cell.UNKNOWN;
		return 0;
	}
	
	private static boolean anyMatch(BufferedImage img, Color color) {
		for(int i=0; i < img.getWidth(); i++)
			for(int j=0; j < img.getHeight(); j++)
				if(closeEnough(img.getRGB(i, j), color.getRGB()))
					return true;
		return false;
	}
	
	private static boolean closeEnough(int rgb1, int rgb2) {
		return closeEnough(new Color(rgb1), new Color(rgb2));
	}
	private static boolean closeEnough(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr*dr + dg*dg + db*db) < 35;
	}
	
	public static Rectangle reduceBoard(BufferedImage img) {
		//Top Corner: 121, 131, 147
		//Bottom Corner: 101, 111, 137
		int topLeft = new Color(42,145,45).getRGB();
		int bottomRight = new Color(42,145,45).getRGB();
		Point tl = null, br = null;
		int w = img.getWidth(), h = img.getHeight();
		outerloop:
		for(int x=0; x < w/5; x++)
			for(int y=0; y < h/5; y++)
				if(closeEnough(img.getRGB(x, y), topLeft)) {
					tl = new Point(x,y);
					break outerloop;
				}
		outerloop:
		for(int x=w-1; x > 4*w/5; x--)
			for(int y=h-1; y > 4*h/5; y--)
				if(closeEnough(img.getRGB(x, y), bottomRight)) {
					br = new Point(x,y);
					break outerloop;
				}
		return new Rectangle(tl.x,tl.y,br.x-tl.x,br.y-tl.y);
	}

}
