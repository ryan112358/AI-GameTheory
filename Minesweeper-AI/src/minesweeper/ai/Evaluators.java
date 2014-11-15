package minesweeper.ai;

import java.util.Arrays;
import java.util.Iterator;

import minesweeper.ai.games.GameState;
import minesweeper.ai.games.NativeGameState;
import minesweeper.ai.games.GameState.State;
import minesweeper.ai.players.AIPlayer;

public class Evaluators {
	
	public interface GameGenerator {
		public GameState getGame();
	}
	static class GameIterator implements Iterator<GameState>, Iterable<GameState> {
		int trials;
		GameGenerator gen;
		public GameIterator(GameGenerator gen, int trials) {
			this.trials = trials;
			this.gen = gen;
		}
		public boolean hasNext() {
			return trials-- > 0;
		}
		public GameState next() {
			return gen.getGame();
		}
		public Iterator<GameState> iterator() {
			return this;
		}
	}
	
	public static void parallelSimulate() {
		//ExecutorService exec = Executors.newFixedThreadPool(100);
		
	}

	public static double[] realTimeComparison(GameIterator games, AIPlayer... players) {
		int[] wins = new int[players.length];
		for(AIPlayer player : players)
			System.out.print("\t"+player.getClass().getSimpleName());
		System.out.println(); int i=0;
		for(GameState game : games) {
			System.out.print(i+++". ");
			for(int p=0; p<players.length; p++) {
				players[p].solve(game);
				if(game.getState()==State.WIN) wins[p]++;
				game.restart();
				System.out.print("\t\t"+(int)(wins[p]*1000f/i)/1000f);
			}
			System.out.println();
		}
		final int trials = i;
		return Arrays.stream(wins).mapToDouble(k->(double)k/trials).toArray();
	}
	
	public static void evaluateAIs(AIPlayer... players) {
		long t0 = System.currentTimeMillis();
		System.out.print("Difficulty\t");
		for(AIPlayer player : players) {
			System.out.print("\t"+player.getClass().getSimpleName());
		}
		
		System.out.print("\nEasy\t");
		int[] wins = new int[players.length];
		for(int i=0; i<1000; i++) {
			GameState game = NativeGameState.createEasyGame();
			for(int p=0; p<players.length; p++) {
				players[p].solve(game);
				if(game.getState()==State.WIN) wins[p]++;
				game.restart();
			}
		}
		for(int w : wins) System.out.print("\t\t"+w);
		
		System.out.print("\nIntermediate");
		wins = new int[players.length];
		for(int i=0; i<1000; i++) {
			GameState game = NativeGameState.createIntermediateGame();
			for(int p=0; p<players.length; p++) {
				players[p].solve(game);
				if(game.getState()==State.WIN) wins[p]++;
				game.restart();
			}
		}
		for(int w : wins) System.out.print("\t\t"+w);
		
		System.out.print("\nAdvanced");
		wins = new int[players.length];
		for(int i=0; i<1000; i++) {
			GameState game = NativeGameState.createAdvancedGame();
			for(int p=0; p<players.length; p++) {
				players[p].solve(game);
				if(game.getState()==State.WIN) wins[p]++;
				game.restart();
			}
		}
		for(int w : wins) System.out.print("\t\t"+w);
		
		System.out.println("\n"+(System.currentTimeMillis()-t0));
		
	}
	
	public static void evaluateAI(AIPlayer player) {
		evaluateAI(player, 1000);
	}
	
	public static void evaluateAI(AIPlayer player, int trials) {
		System.out.println("Evaluation of " + player);
		int[][] results = new int[3][2]; //[easy/med/adv][win/lose]
		for(int i=0; i < trials; i++) {
			GameState game = NativeGameState.createEasyGame();
			player.solve(game);
			results[0][game.getState()==State.WIN?0:1]++;
		}
		System.out.println("Easy: ");
		System.out.println("\tWins: " + results[0][0] + "\n\tLosses: " + results[0][1]);
		for(int i=0; i < trials; i++) {
			GameState game = NativeGameState.createIntermediateGame();
			player.solve(game);
			results[1][game.getState()==State.WIN?0:1]++;
		}
		System.out.println("Intermediate: ");
		System.out.println("\tWins: " + results[1][0] + "\n\tLosses: " + results[1][1]);
		for(int i=0; i < trials; i++) {
			GameState game = NativeGameState.createAdvancedGame();
			player.solve(game);
			results[2][game.getState()==State.WIN?0:1]++;
		}
		System.out.println("Advanced: ");
		System.out.println("\tWins: " + results[2][0] + "\n\tLosses: " + results[2][1]);
	}

}
