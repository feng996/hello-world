package temp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Pdd {
	public void s1() {

		Scanner in = new Scanner(System.in);

		String[] ar = in.nextLine().split(" ");
		String[] br = in.nextLine().split(" ");

		int[] a = new int[ar.length + 2];
		a[0] = Integer.MIN_VALUE;
		a[a.length - 1] = Integer.MAX_VALUE;

		int ned = -1;
		for (int i = 1; i < a.length - 1; i++) {
			a[i] = Integer.parseInt(ar[i - 1]);
			if (a[i] <= a[i - 1]) {
				ned = i;
			}
		}

		int left = a[ned - 1];
		int right = a[ned + 1];

		int need = Integer.MIN_VALUE;

		for (int i = 0; i < br.length; i++) {
			int cur = Integer.parseInt(br[i]);
			if (cur > left && cur < right && cur > need) {
				need = cur;
			}
		}

		if (need == Integer.MIN_VALUE) {
			System.out.println("NO");
		} else {
			a[ned] = need;
			for (int i = 1; i < a.length - 1; i++) {
				System.out.print(a[i] + " ");
			}
		}
	}

	public void s2() {
		Scanner sc = new Scanner(System.in);

		String[] ar = sc.nextLine().split(" ");
		Map<Character, Character> map = new HashMap<>();
		for (String s : ar) {
			char l = s.charAt(0);
			char r = s.charAt(s.length() - 1);

			if (map.containsKey(l)) {
				char temp = l;
				l = map.get(l);
				map.remove(temp);
			}
			map.put(r, l);
		}

		if (map.size() == 1) {
			for (Map.Entry<Character, Character> l : map.entrySet()) {
				if (l.getKey() == l.getValue()) {
					System.out.println("true");
					return;
				}
			}
		}
		System.out.println("false");
	}

	public static void main(String... strings) {
		Scanner sc = new Scanner(System.in);

		int n = sc.nextInt();
		int m = sc.nextInt();

		int[] times = new int[n];
		Map<Integer, List<Integer>> yl = new HashMap<>();

		List<Pair> list = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			times[i] = sc.nextInt();
			list.add(new Pair(i + 1, times[i]));
		}

		for (int i = 0; i < m; i++) {
			int l = sc.nextInt();
			int r = sc.nextInt();

			if (yl.containsKey(r)) {
				yl.get(r).add(l);
			} else {
				yl.put(r, new ArrayList<Integer>() {
					{
						add(l);
					}
				});
			}
		}

		list.sort(new Comparator<Pair>() {

			@Override
			public int compare(Pair o1, Pair o2) {
				return o1.time - o2.time;
			}

		});
		int[] res = new int[n];
		int k = 0;
		Set<Integer> check = new HashSet<>();
		while (list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				boolean flag = true;
				Pair temp = list.get(i);
				List<Integer> l = yl.get(temp.id);
				if (l == null) {
					res[k++] = temp.id;
					check.add(temp.id);
					list.remove(i);
					break;
				} else {
					for (int ll : l) {
						if (!check.contains(ll)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						res[k++] = temp.id;
						check.add(temp.id);
						list.remove(i);
						break;
					}
				}
			}
		}
		for (int i = 0; i < n; i++) {
			System.out.print(res[i] + " ");
		}
	}

	static class Pair {
		int id;
		int time;

		public Pair(int id, int time) {
			this.id = id;
			this.time = time;
		}
	}

}
