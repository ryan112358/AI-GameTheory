

public class FarkleSoln {
	
	static double[][] cache = new double[10000/50][6];
	
	/**
	 * Computes the Expected Value (AKA the amount of points you can expect to score on average)
	 * given the current state (pts, dice) assuming optimal play
	 * 
	 * Due to the nature of the game, there are a lot of possible ways to score points that is difficult to model
	 * efficiently with a computer algorithm.  As a result, I worked out the probabilities by hand and hard coded them
	 * into this function.
	 */
	static double solve(int pts, int dice) {
		if(pts >= 10000)
			return pts;
		else if(cache[pts/50][dice-1] != 0)
			return cache[pts/50][dice-1];
		double ans, result;
		switch(dice) {
		case 1:
			ans = solve(pts+50,6) + solve(pts+100,6);
			result = Math.max(pts, ans / 6.0);
			break;
		case 2:
			ans = 
			solve(pts+200,6) + 2 * solve(pts+150,6) +
			solve(pts+100,6) + 8 * solve(pts+100,1) +
			8 * solve(pts+50,1);
			result = Math.max(ans / 36.0, pts);
			break;
		case 3:
			ans = 
			solve(pts+1000,6) + solve(pts+200,6) +
			solve(pts+300,6) + solve(pts+400,6) +
			solve(pts+500,6) + solve(pts+600,6) +
			3 * solve(pts+250,6) + 3 * solve(pts+200,6) + 
			12 * Math.max(solve(pts+100,2),solve(pts+200,1)) + 
			24 * Math.max(solve(pts+100,2),solve(pts+150,1)) +
			12 * Math.max(solve(pts+50,2),solve(pts+100,1)) + 
			48 * solve(pts+100,2) + 
			48 * solve(pts+50,2);
			result = Math.max(ans / 216.0, pts);
			break; 
		case 4:
			ans =
			//4 of a kind
			solve(pts+2000,6) + solve(pts+400,6) + 
			solve(pts+600,6) + solve(pts+800,6) + 
			solve(pts+1000,6) + solve(pts+1200,6) + 
			//3 of a kind
			4 * solve(pts+1050,6) + 16 * solve(pts+1000,1) +
			4 * solve(pts+300,6) + 4 * solve(pts+250,6) +
			12 * solve(pts+200,1) + 4 * solve(pts+400,6) + 
			4 * solve(pts+350,6) + 12 * solve(pts+300,1) + 
			4 * solve(pts+500,6) + 4 * solve(pts+450,6) + 
			12 * solve(pts+400,1) + 4 * solve(pts+600,6) + 
			16 * solve(pts+500,1) + 4 * solve(pts+700,6) + 
			4 * solve(pts+650,6) + 12 * solve(pts+600,1) +
			//misc
			6 * solve(pts+300,6) + 
			48 * Math.max(solve(pts+100,3),Math.max(solve(pts+200,2),solve(pts+250,1))) + 
			48 * Math.max(solve(pts+100,3),Math.max(solve(pts+150,2),solve(pts+200,1))) + 
			96 * Math.max(solve(pts+100,3),solve(pts+200,2)) + 
			192 * Math.max(solve(pts+100,3),solve(pts+150,2)) + 
			96 * Math.max(solve(pts+50,3),solve(pts+100,2)) + 
			240 * solve(pts+100,3) + 240 * solve(pts+50,3);
			result = Math.max(ans / 1296.0, pts);
			break; 
		case 5:
			ans = 
			//5 of a kind
			solve(pts+3000,6) + solve(pts+600,6) + 
			solve(pts+900,6) + solve(pts+1200,6) + 
			solve(pts+1500,6) + solve(pts+1800,6) +
			//4 of a kind
			5 * solve(pts+2050,6) + 20 * solve(pts+2000,1) + 
			5 * solve(pts+500,6) + 5 * solve(pts+450,6) +
			15 * solve(pts+400,1) + 
			5 * solve(pts+700,6) + 5 * solve(pts+650,6) +
			15 * solve(pts+600,1) + 
			5 * solve(pts+900,6) + 5 * solve(pts+850,6) +
			15 * solve(pts+800,1) + 
			5 * solve(pts+1100,6) + 20 * solve(pts+1000,1) +
			5 * solve(pts+1300,6) + 5 * solve(pts+1250,6) +
			15 * solve(pts+1200,1) + 
			//3 of a kind
			10 * solve(pts+1100,6) + 
			80 * Math.max(solve(pts+1000,2),solve(pts+1050,1)) +
			160 * solve(pts+1000,2) +
			
			10 * solve(pts+400,6) + 
			20 * solve(pts+350,6) + 
			10 * solve(pts+300,6) +
			60 * Math.max(solve(pts+100,4),Math.max(solve(pts+200,2),solve(pts+300,1))) + 
			60 *  Math.max(solve(pts+50,4),Math.max(solve(pts+200,2),solve(pts+250,1))) + 
			90 * solve(pts+200,2) +
			
			10 * solve(pts+500,6) + 
			20 * solve(pts+450,6) + 
			10 * solve(pts+400,6) +
			60 *  Math.max(solve(pts+100,4),Math.max(solve(pts+300,2),solve(pts+400,1))) + 
			60 *  Math.max(solve(pts+50,4),Math.max(solve(pts+300,2),solve(pts+350,1))) + 
			90 * solve(pts+300,2) +
			
			10 * solve(pts+600,6) + 
			20 * solve(pts+550,6) + 
			10 * solve(pts+500,6) +
			60 *  Math.max(solve(pts+100,4),Math.max(solve(pts+400,2),solve(pts+500,1))) + 
			60 *  Math.max(solve(pts+50,4),Math.max(solve(pts+400,2),solve(pts+450,1))) + 
			90 * solve(pts+400,2) +
			
			10 * solve(pts+700,6) + 
			80 * Math.max(solve(pts+100,4),Math.max(solve(pts+500,2),solve(pts+600,1))) +
			160 * solve(pts+500,2) +
			
			10 * solve(pts+800,6) + 
			20 * solve(pts+750,6) + 
			10 * solve(pts+700,6) +
			60 *  Math.max(solve(pts+100,4),Math.max(solve(pts+600,2),solve(pts+700,1))) + 
			60 *  Math.max(solve(pts+50,4),Math.max(solve(pts+600,2),solve(pts+650,1))) + 
			90 * solve(pts+600,2) +
			//misc
			120 * Math.max(Math.max(solve(pts+100,4),solve(pts+200,3)),
					Math.max(solve(pts+250,2),solve(pts+300,1))) +
			480 * Math.max(solve(pts+100,4),Math.max(solve(pts+200,3),solve(pts+250,2))) + 
			480 * Math.max(solve(pts+100,4),Math.max(solve(pts+150,3),solve(pts+200,2))) + 
			600 * Math.max(solve(pts+100,4),solve(pts+200,3)) + 
			1200 * Math.max(solve(pts+100,4),solve(pts+150,3)) + 
			600 * Math.max(solve(pts+50,4),solve(pts+100,3)) + 
			1020 * solve(pts+100,4) +
			1020 * solve(pts+50,4);
			result = Math.max(pts, ans / 7776.0);
			break;
		default:
			ans = 
			//6 of a kind
			solve(pts+4000,6) + solve(pts+800,6) +
			solve(pts+1200,6) + solve(pts+1600,6) +
			solve(pts+2000,6) + solve(pts+2400,6) +
			//5 of a kind
			6 * solve(pts+3050,6) + 24 * solve(pts+3000,1) +
			6 * solve(pts+700,6) + 6 * solve(pts+650,6) + 18 * solve(pts+600,1) +
			6 * solve(pts+1000,6) + 6 * solve(pts+950,6) + 18 * solve(pts+900,1) +
			6 * solve(pts+1300,6) + 6 * solve(pts+1250,6) + 18 * solve(pts+1200,1) +
			6 * solve(pts+1600,6) + 24 * solve(pts+1500,1) +
			6 * solve(pts+1900,6) + 6 * solve(pts+1850,6) + 18 * solve(pts+1800,1) +
			//4 of a kind
			15 * solve(pts+2100,6) + 120 * solve(pts+2050,1) + 240 * solve(pts+2000,2) +
			
			15 * solve(pts+600,6) + 30 * solve(pts+550,6) + 
			15 * solve(pts+500,6) + 90 * solve(pts+500,1) +
			90 * solve(pts+450,1) + 135 * solve(pts+400,2) +
			
			15 * solve(pts+800,6) + 30 * solve(pts+750,6) + 
			15 * solve(pts+700,6) + 90 * solve(pts+700,1) +
			90 * solve(pts+650,1) + 135 * solve(pts+600,2) +
			
			15 * solve(pts+1000,6) + 30 * solve(pts+950,6) + 
			15 * solve(pts+900,6) + 90 * solve(pts+900,1) +
			90 * solve(pts+850,1) + 135 * solve(pts+800,2) +
			
			15 * solve(pts+1200,6) + 120 * solve(pts+1100,1) + 240 * solve(pts+1000,2) +
			
			15 * solve(pts+1400,6) + 30 * solve(pts+1350,6) + 
			15 * solve(pts+1300,6) + 90 * solve(pts+1300,1) +
			90 * solve(pts+1250,1) + 135 * solve(pts+1200,2) +
			//3 of a kind x 2
			20 * 
			(solve(pts+1200,6) + solve(pts+1300,6) + solve(pts+1400,6) +
					solve(pts+1500,6) + solve(pts+1600,6) +
					solve(pts+500,6) + solve(pts+600,6) +
					solve(pts+700,6) + solve(pts+800,6) +
					solve(pts+700,6) + solve(pts+800,6) +
					solve(pts+900,6) + solve(pts+900,6) + 
					solve(pts+1000,6) + solve(pts+1100,6)) +
			//3 of a kind
			240 * Math.max(solve(pts+1000,3),Math.max(solve(pts+1050,2),solve(pts+1100,1))) + 
			960 * Math.max(solve(pts+1000,3),solve(pts+1050,2)) + 
			1200 * solve(pts+1000,3) +
			
			60 * solve(pts+450,6) + 60 * solve(pts+400,6) +
			180 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),solve(pts+400,1))) + 
			360 * Math.max(solve(pts+100,5), Math.max(solve(pts+150,4),Math.max(solve(pts+200,3),Math.max(solve(pts+300,2),solve(pts+350,1))))) +
			180 * Math.max(solve(pts+50,5),Math.max(solve(pts+100,4),Math.max(solve(pts+200,3), Math.max(solve(pts+250,2),solve(pts+300,1))))) + 
			540 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,3),solve(pts+300,2))) + 
			540 * Math.max(solve(pts+50,5),Math.max(solve(pts+200,3),solve(pts+250,2))) + 
			480 * solve(pts+200,3) +
			
			60 * solve(pts+550,6) + 60 * solve(pts+500,6) +
			180 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),Math.max(solve(pts+300,3), Math.max(solve(pts+400,2),solve(pts+500,1))))) + 
			360 * Math.max(solve(pts+100,5), Math.max(solve(pts+150,4),Math.max(solve(pts+300,3),Math.max(solve(pts+400,2),solve(pts+450,1))))) +
			180 * Math.max(solve(pts+50,5),Math.max(solve(pts+100,4),Math.max(solve(pts+300,3), Math.max(solve(pts+350,2),solve(pts+400,1))))) + 
			540 * Math.max(solve(pts+100,5),Math.max(solve(pts+300,3),solve(pts+400,2))) + 
			540 * Math.max(solve(pts+50,5),Math.max(solve(pts+300,3),solve(pts+350,2))) + 
			480 * solve(pts+300,3) +
			
			60 * solve(pts+650,6) + 60 * solve(pts+600,6) +
			180 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),Math.max(solve(pts+400,3), Math.max(solve(pts+500,2),solve(pts+600,1))))) + 
			360 * Math.max(solve(pts+100,5), Math.max(solve(pts+150,4),Math.max(solve(pts+400,3),Math.max(solve(pts+500,2),solve(pts+550,1))))) +
			180 * Math.max(solve(pts+50,5),Math.max(solve(pts+100,4),Math.max(solve(pts+400,3), Math.max(solve(pts+450,2),solve(pts+500,1))))) + 
			540 * Math.max(solve(pts+100,5),Math.max(solve(pts+400,3),solve(pts+500,2))) + 
			540 * Math.max(solve(pts+50,5),Math.max(solve(pts+400,3),solve(pts+450,2))) + 
			480 * solve(pts+400,3) +
			
			240 * Math.max(solve(pts+500,3),Math.max(solve(pts+600,2),solve(pts+700,1))) + 
			960 * Math.max(solve(pts+500,3),solve(pts+600,2)) + 
			1200 * solve(pts+500,3) +
			
			60 * solve(pts+850,6) + 60 * solve(pts+800,6) +
			180 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),Math.max(solve(pts+600,3), Math.max(solve(pts+700,2),solve(pts+800,1))))) + 
			360 * Math.max(solve(pts+100,5), Math.max(solve(pts+150,4),Math.max(solve(pts+600,3),Math.max(solve(pts+700,2),solve(pts+750,1))))) +
			180 * Math.max(solve(pts+50,5),Math.max(solve(pts+100,4),Math.max(solve(pts+600,3), Math.max(solve(pts+650,2),solve(pts+700,1))))) + 
			540 * Math.max(solve(pts+100,5),Math.max(solve(pts+600,3),solve(pts+700,2))) + 
			540 * Math.max(solve(pts+50,5),Math.max(solve(pts+600,3),solve(pts+650,2))) + 
			480 * solve(pts+600,3) +
			//misc
			1800 * solve(pts+750,6) + 720 * solve(pts+1500,6) +
			1080 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),Math.max(solve(pts+250,3),solve(pts+300,2)))) + 
			3600 * Math.max(solve(pts+100,5),Math.max(solve(pts+200,4),solve(pts+250,3))) +
			3600 * Math.max(solve(pts+100,5),Math.max(solve(pts+150,4),solve(pts+200,3))) + 
			2520 * Math.max(solve(pts+100,5),solve(pts+200,4)) +
			5400 * Math.max(solve(pts+100,5),solve(pts+150,4)) + 
			2520 * Math.max(solve(pts+50,5),solve(pts+100,4)) +
			3600 * solve(pts+100,5) + 3600 * solve(pts+50,5);
			result = Math.max(pts, ans / 46656.0);
		}
		cache[pts/50][dice-1] = result;
		return result;
	}
	
	/*
	 * This program outputs an Expected Value table where each row is the number of points you currently have
	 * (incremented by 50 each time) and the columns represent the number of dice you have left to throw.
	 * 
	 * There are 3 possible cases for the different cell values
	 *  - 0 --> State is unreachable
	 *  - CellValue = 50*row --> Stop Rolling
	 *  - CellValue > 50*row --> Roll Again
	 *  
	 *  Note that the table is most interesting near the top
	 */
	public static void main(String[] args) {
		solve(0,6);
		for(int i=0; i<cache.length; i++) {
			for(int j=0; j<cache[0].length; j++)
				System.out.print(round(cache[i][j]) + "\t");
			System.out.println();
		}

	}
	
	static double round(double d) {
		return Math.round(d*1000)/1000.0;
	}

}
