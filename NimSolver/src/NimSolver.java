/*
 * This program solves the mathematical strategy game of Nim.  Here are the rules
 * 
 * You can have up to 3 heaps of stones
 * At each turn, a player may remove 1,2, or 3 stones from any single pile
 * The player to take the last stone wins the game
 * 
 * While my solution isn't as optimal as it could be, it runs instantly for fairly big test cases and I
 * left out optimizations for the sake of elegance and simplicity
 */
public class NimSolver {
	
	/* Cache to store partial results
	 * 0 -> Not calculated yet
	 * -1 -> Losing Configuration
	 * 1 -> Winning Configuration
	 */
	static byte[][][] cache;
	
	/*
	 * Returns true if the current player can win with optimal strategy, and false otherwise
	 */
	static boolean winConfig(int a, int b, int c) {
		if(cache[a][b][c] == 0) {
			boolean ans = false;
			for(int k=1; k <= Math.min(3, a); k++)
				if(!winConfig(a-k, b, c))
					ans = true;
			for(int k=1; k <= Math.min(3, b); k++)
				if(!winConfig(a, b-k, c))
					ans = true;
			for(int k=1; k <= Math.min(3, c); k++)
				if(!winConfig(a, b, c-k))
					ans = true;
			cache[a][b][c] = (byte) (ans ? 1 : -1);
		}
		return cache[a][b][c] == 1;
		
	}
	/*
	 * This program will print out the losing configurations (a,b,c)
	 * 
	 * If you can reduce a game to a losing configuration in a single turn, then it's a winning configuration so 
	 * just memorize a few losing configurations and you will be able to win this game consistently
	 */
	public static void main(String[] args) {
		int N = 8;
		cache = new byte[N][N][N];
		cache[0][0][0] = -1;
		
		for(int a=0; a < N; a++)
			for(int b=a; b < N; b++)
				for(int c=b; c < N; c++)
					if(! winConfig(a,b,c))
						System.out.println("("+a+", "+b+", "+c+")");

	}

}
