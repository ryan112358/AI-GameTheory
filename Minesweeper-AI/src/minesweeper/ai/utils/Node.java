package minesweeper.ai.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import minesweeper.ai.games.BoardConfiguration.Position;

public class Node implements Iterable<Position> {
	private Position data;
	private Node next;
	public Node(Position data, Node next) {
		this.data = data;
		this.next = next;
	}
	@Override
	public Iterator<Position> iterator() {
		return new Iterator<Position>() {
			Node curr=Node.this;
			public boolean hasNext() {
				return curr.data != null;
			}
			public Position next() {
				Position ans = curr.data;
				curr = curr.next;
				return ans;
			}
			
		};
	}
	public Set<Position> asSet() {
		Set<Position> ans = new HashSet<>();
		for(Position p : this)
			ans.add(p);
		return ans;
	}
	public String toString() {
		String ans = "";
		for(Position p : this)
			ans += p + " ";
		return ans;
	}
}
