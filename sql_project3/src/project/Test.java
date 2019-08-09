package project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Test {
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int n = in.nextInt();
		Map<Integer, Integer> map = new HashMap<>();
		ArrayList<Point> arr = new ArrayList<>(100001);
		
		
		for (int i = 0; i < n-1; i++) {
			int l = in.nextInt();
			int r = in.nextInt();
			map.put(l, r);
			arr.set(i, new Point(l, r));
		}
		
		int count = 0;
		Set<Integer> set = new HashSet<>();
		while (true) {
			count++;
			set.clear();
			set.add(1);
			arr.removeIf(i -> i.y == 1);
			for (int i = 0; i < arr.size(); i++) {
				Point p = arr.get(i);
				if (set.contains(p.y)) {
					continue;
				}
				set.add(p.y);
				p.x = p.y;
				p.y = map.get(p.y);
			}
			if (arr.isEmpty()) {
				break;
			}
		}
		System.out.println(count);
	}
}
class Point {
	int x;
	int y;
	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}
}
