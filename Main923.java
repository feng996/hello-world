package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main923 {
	public static int threeSumMulti(int[] A, int target) {
		final int mod = (int) Math.pow(10, 9) + 7;
		Arrays.sort(A);

		long count = 0;
		int n = A.length;

		for (int i = 0; i < n; i++) {
			int mid = i + 1;
			int j = n - 1;

			while (mid < j) {
				int sum = A[i] + A[mid] + A[j];
				int mm = A[mid];
				int jj = A[j];

				if (sum == target) {
					int countM = 0;
					int countJ = 0;

					while (mid < j && A[mid] == mm) {
						mid++;
						countM++;
					}

					while (mid <= j && A[j] == jj) {
						j--;
						countJ++;
					}
					if (mm == jj) {
						count += (countM + countJ) * (countM + countJ - 1) / 2;
					} else {
						count += countM * countJ;
					}

				} else if (sum < target) {
					mid++;
				} else {
					j--;
				}
			}
		}

		return (int) (count % mod);
	}

	public int lengthOfLIS(int[] nums) {
		int n = nums.length;
		
		int[][] memo = new int[n+1][n];
		
		for (int i = 0; i < n+1; i++) {
			for (int j = 0; j < n; j++) {
				memo[i][j] = -1;
			}
		}
		
		return maxLength(nums, -1, 0, memo);
	}
	
	public int maxLength(int[] nums, int pre, int cur, int[][] memo) {
		if (cur == nums.length) {
			return 0;
		}
		
		if (memo[pre+1][cur] >= 0) {
			return memo[pre+1][cur];
		}
		
		int l = 0;
		if (pre < 0 || nums[cur] > nums[pre]) {
			l = maxLength(nums, cur, cur+1, memo) + 1;
		}
		
		int r = maxLength(nums, pre, cur+1, memo);
		memo[pre+1][cur] = Math.min(l, r);
		
		return memo[pre+1][cur];
		
	}
	
	public int dp_lengthOfLIS(int[] nums) {
		int n = nums.length;
		if (n == 0) {
			return 0;
		}
		
		int[] dp = new int[n];
		dp[0] = 1;
		int maxLength = 1;
		for (int i = 1; i < n; i++) {
			int maxx = 0;
			for (int j = 0; j < i; j++) {
				if (nums[i] > nums[j]) {
					maxx = Math.max(maxx, dp[j]);
				}
			}
			dp[i] = maxx + 1;
			maxLength = Math.max(dp[i], maxLength);
		}
		
		return maxLength;
	}
	
	public static int dpandtwo_lengthOfLIS(int[] nums) {
		int n = nums.length;
		if (n == 0) {
			return 0;
		}
		
		List<Integer> ls = new ArrayList<>();
		ls.add(nums[0]);
		
		for (int i = 1; i < n; i++) {
			if (nums[i] >= ls.get(ls.size()-1)) {
				ls.add(nums[i]);
				continue;
			}
			int idx = Collections.binarySearch(ls, nums[i]);
			System.out.println(idx);
			
			idx = idx >= 0 ? idx : -(idx + 1);
			ls.set(idx, nums[i]);
		}
		
		return ls.size();
	}
	
	//最长公共子序列
	public String longestPalindrome(String s) {
		if (s.length() <= 1) {
			return s;
		}
		int left = 0;
		int right = 0;
		for (int i = 0; i < s.length(); i++) {
			int l = maxLength(s, i, i);
			int r = maxLength(s, i, i+1);
			
			int len = Math.max(l, r);
			if (len > right - left + 1) {
				left = i - (len - 1) / 2;
				right = i + len / 2;
			}
		}
		
		return s.substring(left, right + 1);
	}
	
	public int maxLength(String s, int i, int j) {
		while (i >= 0 && j < s.length() && s.charAt(i) == s.charAt(j)) {
			i--;
			j++;
		}
		
		return j - i - 1;
	}

	public static void main(String... strings) {
		System.out.println(dpandtwo_lengthOfLIS(new int[] {4,10,4,3,8,9}));
	}
}
