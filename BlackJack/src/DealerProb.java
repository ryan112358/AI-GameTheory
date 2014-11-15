public class DealerProb {
	
	public static int BUST = 22;
	
	public static double round3(double n) {
		return Math.round(n*1000.0) / 1000.0;
	}
	
	public static double p(int start, int end, boolean activeAce) {
		if(start > 21) {
			if(activeAce)
				return p(start - 10, end, false);
			else if(end == BUST)
				return 1;
			else
				return 0;
		}
		else if(start == 17 && activeAce)
			return p(start - 10, end, false);
		else if(start == end)
			return 1;
		else if(start >= 17)
			return 0;
		else {
			double result = 0;
			for(int i=2; i<10; i++) 
				result += p(start + i, end, activeAce);
			result += 4*p(start + 10, end, activeAce);
			if(start < 11)
				result += p(start + 11, end, true);
			else 
				result += p(start + 1, end, activeAce);
			result /= 13.0;
			return result;
		}
	}
	
	static double[] probabilities = new double[27];
	
	public static void p(int value, boolean activeAce) {
		if((value == 17 || value > 21) && activeAce == true)
			p(value - 10, false);
		else if(value >= 17)
			probabilities[value]++;
		else {
			for(int i=2; i<=9; i++)
				p(value+i, activeAce);
			for(int i=0; i<4; i++) 
				p(value+10, activeAce);
			if(value > 11)
				p(value+1, activeAce);
			else 
				p(value+11, true);
		}
	}
	
	public static void dealerOddsChart() {
		System.out.println("\t17\t18\t19\t20\t21\tBust\tTotal");
		for(int i = 2; i<=11; i++) {
			System.out.print(i+"\t");
			double total = 0;
			for(int k=17; k<=22; k++) {
				total += p(i,k,i==11);
				System.out.print(round3(p(i, k,i==11)) + "\t");
			}
			System.out.println(round3(total));
		}
	}
	
	
	public static void main(String[] args) {
		dealerOddsChart();
	}
}
