package minesweeper.ai.utils;

public class Combinations {
	
	private static long[][] nCr = new long[50][];
	static {
		for(int i=0; i < nCr.length; i++) {
			nCr[i] = new long[i+1]; 
			nCr[i][0] = nCr[i][i] = 1;
			for(int j = 1; j < i; j++)
				nCr[i][j] = nCr[i-1][j-1] + nCr[i-1][j];
		}
	}
	
	public static long factorial(int n) {
		long ans = 1;
		while(n > 1) ans *= n--;
		return ans;
	}
	
	public static long nCr(int n, int r) {
		if(n >= nCr.length || r > n) return Long.MAX_VALUE; 
		return nCr[n][r];
	}

}
