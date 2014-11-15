

public class Expectations {

	//inital options
	/* Surrender
	 * Hit
	 * Stand
	 * Double Down
	 * Split (Pair)
	 */
	//other options
	/*
	 * Hit
	 * Stand
	 */
	//Generated from DealerProb file
	//across: 17,18,19,20,21,BJ,Bust
	//down: 2,3,4,...,A
	public static double[][] dealerOddsChart =
		{ {}, {},
		{ 0.131,0.137,0.131,0.125,0.119,0,	  0.357 },
		{ 0.127,0.132,0.127,0.121,0.116,0,	  0.377 },
		{ 0.123,0.128,0.122,0.117,0.112,0,	  0.398 },
		{ 0.119,0.123,0.118,0.114,0.109,0,	  0.418 },
		{ 0.117,0.117,0.112,0.108,0.103,0,	  0.443 },
		{ 0.369,0.138,0.079,0.079,0.074,0,	  0.262 },
		{ 0.129,0.359,0.129,0.069,0.069,0,	  0.245 },
		{ 0.120,0.120,0.351,0.120,0.061,0,	  0.228 },
		{ 0.111,0.111,0.111,0.342,0.034,0.077,0.212 },
		{ 0.060,0.146,0.140,0.140,0.062,0.308,0.145 } };
	
	public static double SURRENDER = -0.5;
	
	/**
	 * Probability of beating the dealer given that the dealer doesn't have a blackjack
	 * @param handTotal
	 * @param dealerUpCard
	 * @return
	 */
	public static double pBeatDealer(int handTotal, int dealerUpCard) {
		if(handTotal > 21)
			return 0;
		double result = 0;
		switch(handTotal) {
		case 21:
			result+=dealerOddsChart[dealerUpCard][3]; //20
		case 20:
			result+=dealerOddsChart[dealerUpCard][2]; //19
		case 19:
			result+=dealerOddsChart[dealerUpCard][1]; //18
		case 18:
			result+=dealerOddsChart[dealerUpCard][0]; //17
		default:
			result+=dealerOddsChart[dealerUpCard][6]; //Bust
		} 
		return result / (1.0 - dealerOddsChart[dealerUpCard][5]);
	}
	/**
	 * Probability of losing to the dealer given that he doesn't have a blackjack
	 * @param handTotal
	 * @param dealerUpCard
	 * @return
	 */
	public static double pLoseDealer(int handTotal, int dealerUpCard) {
		if(handTotal > 21)
			return 1;
		else if(handTotal < 17)
			return (1.0 - dealerOddsChart[dealerUpCard][6] - dealerOddsChart[dealerUpCard][5]) / (1.0 - dealerOddsChart[dealerUpCard][5]);
		
		double result = 0;
		switch(handTotal) {
		case 16:
			result+=dealerOddsChart[dealerUpCard][0];
		case 17:
			result+=dealerOddsChart[dealerUpCard][1];
		case 18:
			result+=dealerOddsChart[dealerUpCard][2];
		case 19:
			result+=dealerOddsChart[dealerUpCard][3];
		case 20:
			result+=dealerOddsChart[dealerUpCard][4];
		}
		return result / (1.0 - dealerOddsChart[dealerUpCard][5]);
	}
	
	public static double hit(int handTotal, boolean activeAce, int dealerUpCard) {
		double expectation = 0.0;
		for(int t=2; t<10; t++)
			expectation += restOptimalPlay(handTotal + t, activeAce, dealerUpCard);
		expectation += 4.0*restOptimalPlay(handTotal + 10, activeAce, dealerUpCard);
		if(handTotal < 11)
			expectation += restOptimalPlay(handTotal + 11, true, dealerUpCard);
		else
			expectation += restOptimalPlay(handTotal + 1, activeAce, dealerUpCard);
		return expectation / 13.0;
	}
	
	public static double stand(int handTotal, boolean activeAce, int dealerUpCard) {
		//p(Player > Dealer) - p(Player < Dealer)
		return pBeatDealer(handTotal, dealerUpCard) - pLoseDealer(handTotal, dealerUpCard);
	}
	
	public static double doubleDown(int handTotal, boolean activeAce, int dealerUpCard) {
		double expectation = 0.0;
		for(int t=2; t<10; t++) {
			int newTotal = handTotal + t;
			if(activeAce == true && newTotal > 21)
				newTotal -= 10;
			expectation += pBeatDealer(newTotal, dealerUpCard) - pLoseDealer(newTotal, dealerUpCard);
		}
		int newTotal = handTotal + 10;
		if(activeAce == true && newTotal > 21)
			newTotal -= 10;
		expectation += 4*(pBeatDealer(newTotal, dealerUpCard) - pLoseDealer(newTotal, dealerUpCard));
		if(handTotal < 11)
			expectation += pBeatDealer(handTotal + 11, dealerUpCard) - pLoseDealer(handTotal + 11, dealerUpCard);
		else {
			expectation += pBeatDealer(handTotal + 1, dealerUpCard) - pLoseDealer(handTotal + 1, dealerUpCard);
		}
		return (2.0 * expectation) / 13.0;
	}
	
	public static double split(int card, int dealerUpCard, int numSplits) {	
		if(numSplits > 3)
			return -1; //Can't split more than 3 times
		double result = 0.0;
		for(int k = 2; k<=9; k++)
			result += splitOptimalPlay(card, k, card == 11, dealerUpCard, numSplits);
		result += 4.0*splitOptimalPlay(card, 10, card == 11, dealerUpCard, numSplits);
		if(card == 11)
			result += splitOptimalPlay(card, 1, true, dealerUpCard, numSplits);
		else
			result += splitOptimalPlay(card, 11, true, dealerUpCard, numSplits);
		return (2.0 * result) / 13.0;
	}
	
	public static double firstOptimalPlay(int card1, int card2, boolean activeAce, int dealerUpCard) {
//		if(card1 + card2 == 21) {
//			if(dealerUpCard == 11) 
//				return (9.0 * 1.5) / 13.0;
//			else if(dealerUpCard == 10)
//				return (12.0 * 1.5) / 13.0;
//			else
//				return 1.5;
//		}
		
		//Options: Hit, Stand, Surrender, Split, Double Down
		double hit = hit(card1 + card2, activeAce, dealerUpCard);
		double stand = stand(card1 + card2, activeAce, dealerUpCard);
		double surrender = SURRENDER;
		double split = -10000; //arbitrary small number
		if(card1 == card2 || (card1 == 11 && card2 == 1))
			split = split(card1, dealerUpCard, 1);
		double doubledown = doubleDown(card1 + card2, activeAce, dealerUpCard);
		double result = mymax(hit, stand,surrender,split,doubledown);
		//System.out.println(result);
		return result;
	}
	
	public static double splitOptimalPlay(int card1, int card2, boolean activeAce, int dealerUpCard, int numSplits) {
		//Options: Hit, Stand, Surrender, Split, Double Down
		double hit = hit(card1 + card2, activeAce, dealerUpCard);
		double stand = stand(card1 + card2, activeAce, dealerUpCard);
		double surrender = SURRENDER;
		double split = -10000; //arbitrary small number
		if(card1 == card2 || (card1 == 11 && card2 == 1))
			split = split(card1, dealerUpCard, numSplits + 1);
		double doubledown = doubleDown(card1 + card2, activeAce, dealerUpCard);
		//double result = mymax(hit, stand,surrender,split,doubledown);
		//System.out.println(result);
		return Math.max(Math.max(Math.max(hit,stand), Math.max(surrender, split)), doubledown);
	}
	
	public static double mymax(double hit, double stand, double surrender, double split, double dbldwn) {
		double max = Math.max(Math.max(Math.max(hit,stand), Math.max(surrender, split)), dbldwn);
		System.out.print("\t"+round3(max));
		if(hit == max) {
			//System.out.print("\tH");
			return hit;
		}
		else if(stand == max) {
			//System.out.print("\tS");
			return stand;
		}
		else if(surrender == max) {
			//System.out.print("\tL");
			return surrender;
		}
		else if(split == max) {
			//System.out.print("\tP");
			return split;
		}
		else if(dbldwn == max) {
			//System.out.print("\tDD");
			return dbldwn;
		}
		else
			System.out.println("ERROR");
		return -1;
	}
	
	public static double restOptimalPlay(int handTotal, boolean activeAce, int dealerUpCard) {
		//Options: Hit, Stand
		if(handTotal > 21) {
			if(activeAce) {
				handTotal -= 10;
				activeAce = false;
			}
			else 
				return -1;
		}
		double hit = hit(handTotal, activeAce, dealerUpCard);
		double stand = stand(handTotal, activeAce, dealerUpCard);
		return Math.max(hit, stand);
		//return mymax2(hit,stand);
	}
	
	public static double mymax2(double hit, double stand) {
		if(hit > stand) {
			System.out.print("\tH");
			return hit;
		}
		else {
			System.out.print("\tS");
			return stand;
		}
	}
	
	static String[] values = { "", "", "2", "3", "4", "5", "6", "7", "8", "9", "10", "A" };
	
	public static void solve() {
		for(int dealer = 2; dealer <=10; dealer++)
			System.out.print("\t" + dealer);
		System.out.println("\tA");
		solveSplit();
		solveAces();
		solveTotals();
	}
	
	public static void solveSplit() {
		for(int split = 2; split <= 11; split++) {
			System.out.print(values[split] + "-" + values[split]);
			for(int dealer = 2; dealer <= 11; dealer++) {
				if(split == 11)
					firstOptimalPlay(11,1,true,dealer);
				else
					firstOptimalPlay(split,split,split==11, dealer);
			}
			System.out.println();
		}
	}
	
	public static void solveAces() {
		for(int card = 2; card <= 10; card++) {
			System.out.print("A-" + values[card]);
			for(int dealer = 2; dealer <= 11; dealer++) {
				firstOptimalPlay(11,card,true,dealer);
			}
			System.out.println();
		}
	}
	
	public static void solveTotals() {
		for(int total = 5; total <= 21; total++) {
			System.out.print(total);
			for(int dealer = 2; dealer <= 11; dealer++) {
				firstOptimalPlay(total,0,false,dealer);
			}
			System.out.println();
		}
	}
	
	public static void solveRest() {
		for(int dealer = 2; dealer <=10; dealer++)
			System.out.print("\t" + dealer);
		System.out.println("\tA");
		//solve aces
		for(int card = 2; card <= 10; card++) {
			System.out.print("'Soft"+(11+card)+"':[");
			for(int dealer = 2; dealer <= 11; dealer++) {
				tempOptimal(11+card,true,dealer);
			}
			System.out.println("],");
		}
		//solve totals
		for(int total = 5; total <= 21; total++) {
			System.out.print("'Hard"+total+"':[");
			for(int dealer = 2; dealer <= 11; dealer++) {
				tempOptimal(total,false,dealer);
			}
			System.out.println("],");
		}
	}
	
	public static void tempOptimal(int total, boolean activeAce, int dealer) {
		double stand = stand(total,activeAce,dealer);
		double optimal = restOptimalPlay(total,activeAce,dealer);
//		System.out.print("\t"+round3(optimal));
		if(stand == optimal)
			System.out.print(",S");
		else
			System.out.print(",H");
	}
	
	public static void main(String[] args) {
		solve();
		//solveRest();
	}
	
	public static double round3(double n) {
		return Math.round(n*1000.0) / 1000.0;
	}

}
