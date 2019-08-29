package demo1;

import java.util.Arrays;
import java.util.Stack;

public class SortMethods {
	///排序算法///
	
	// -------------------------冒泡排序------------------------------------//
	public static void bubbleSort(int[] array) {

		for (int i = 0; i < array.length - 1; i++) {
			for (int j = 0; j < array.length - 1 - i; j++) {
				if (array[j] > array[j + 1]) {
					int temp = array[j];
					array[j] = array[j + 1];
					array[j + 1] = temp;
				}
			}
		}
	}
	// -------------------------冒泡排序end---------------------------------//

	// -------------------------选择排序------------------------------------//
	public static void selectSort(int[] array) {
		for (int i = 0; i < array.length; i++) {
			int minn = i;
			for (int j = i + 1; j < array.length; j++) {
				if (array[j] < array[minn]) {
					minn = j;
				}
			}
			int temp = array[i];
			array[i] = array[minn];
			array[minn] = temp;
		}
	}
	// -------------------------选择排序end---------------------------------//

	// -------------------------插入排序------------------------------------//
	public static void insertSort(int[] array) {
		for (int i = 0; i < array.length; i++) {
			int temp = array[i];
			int j = i;
			for (j = i; j > 0 && array[j - 1] > temp; j--) {
				array[j] = array[j - 1];
			}
			array[j] = temp;
		}
	}
	// -------------------------插入排序end---------------------------------//

	// -------------------------快速排序------------------------------------//
	public static void quickSort_Recursive(int[] array, int i, int j) {

		if (i < j) {
			int pos = _quickSort(array, i, j);
			quickSort_Recursive(array, i, pos - 1);
			quickSort_Recursive(array, pos + 1, j);
		}
	}

	public static void quickSort_NonRecursive(int[] array, int i, int j) {
		Stack<Integer> stack = new Stack<>();
		stack.push(j);
		stack.push(i);

		while (!stack.isEmpty()) {
			int l = stack.pop();
			int r = stack.pop();

			if (l < r) {
				int pos = _quickSort(array, l, r);
				if (pos > l) {
					stack.push(pos - 1);
					stack.push(l);
				}
				if (pos < r) {
					stack.push(r);
					stack.push(pos + 1);
				}
			}
		}
	}

	public static int _quickSort(int[] array, int left, int right) {
		int i = left;
		int j = right;
		int cur = array[j];
		while (i < j) {
			while (i < j && array[i] <= cur) {
				i++;
			}
			array[j] = array[i];

			while (i < j && array[j] >= cur) {
				j--;
			}
			array[i] = array[j];

		}
		array[i] = cur;
		return i;
	}
	// -------------------------快速排序end---------------------------------//

	// -------------------------归并排序------------------------------------//
	public static void mergeSort(int[] array, int left, int right) {

		if (left < right) {
			int mid = ((left + right) >> 1);
			mergeSort(array, left, mid);
			mergeSort(array, mid + 1, right);
			_mergeSort(array, left, mid, right);
		}
		
	}

	public static void _mergeSort(int[] array, int left, int mid, int right) {
		int[] temp = new int[right - left + 1];

		int li = left;
		int lj = mid;
		int ri = mid + 1;
		int rj = right;
		int k = 0;
		while (li <= lj && ri <= rj) {
			int minn;
			if (array[li] <= array[ri]) {
				minn = array[li++];
			} else {
				minn = array[ri++];
			}
			temp[k++] = minn;
		}
		while (li <= lj) {
			temp[k++] = array[li++];
		}
		while (ri <= rj) {
			temp[k++] = array[ri++];
		}

		for (int i = 0; i < temp.length; i++) {
			array[i + left] = temp[i];
		}
	}
	// -------------------------归并排序end---------------------------------//
	
	// -------------------------堆排序--------------------------------------//
	
	public static void heapSort(int[] array) {
		for (int i = array.length / 2; i >= 0; i--) {
			heapAdjust(array, i, array.length);
		}
		
		for (int i = array.length-1; i > 0; i--) {
			swap(array, i, 0);
			heapAdjust(array, 0, i);
		}
	}
	
	public static void heapAdjust(int[] array, int i, int n) {
		int child;
		int father;
		for (father = array[i]; leftchild(i) < n; i = child) {
			child = leftchild(i);
			
			if (child != n - 1 && array[child] < array[child+1]) {
				child++;
			}
			
			if (father < array[child]) {
				array[i] = array[child];
			} else {
				break;
			}
		}
		
		array[i] = father;
	}
	
	public static int leftchild(int i) {
		return 2 * i + 1;
	}
	
	public static void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
	// -------------------------堆排序end-----------------------------------//
	
	///排序算法end///
	
	
	///二叉树遍历算法
	
	//前序遍历
	public static void preOrderUnRecur(Node head) {
		
		if (head != null) {
			Stack<Node> stack = new Stack<>();
			stack.push(head);
			while (!stack.empty()) {
				head = stack.pop();
				
				System.out.print(head.val + " ");
				if (head.right != null) {
					stack.push(head.right);
				}
				if (head.left != null) {
					stack.push(head.left);
				}
			}
		}
		System.out.println();
	}
	//前序遍历end
	
	//中序遍历
	public static void inOrderUnRecur(Node head) {
		if (head != null) {
			Stack<Node> stack = new Stack<>();
			
			while (!stack.empty() || head != null) {
				
				if (head != null) {
					stack.push(head);
					head = head.left;
				} else {
					head = stack.pop();
					System.out.print(head.val + " ");
					head = head.right;
				}
				
			}
		}
	}
	//中序遍历end
	
	//后序遍历
	public static void posOrderUnrecur(Node head) {
		if (head != null) {
			Stack<Node> s1 = new Stack<>();
			Stack<Node> s2 = new Stack<>();
			
			s1.push(head);
			while (!s1.empty()) {
				head = s1.pop();
				s2.push(head);
				
				if (head.left != null) {
					s1.push(head.left);
				}
				
				if (head.right != null) {
					s1.push(head.right);
				}
			}
			
			while (!s2.empty()) {
				System.out.print(s2.pop().val + " ");
			}
		}
	}
	//后序遍历end
	
	//链表反转
	//链表反转end
	
	///二叉树遍历算法end
	public static void main(String... strings) {
		Node head = new Node(1);
		head.left = new Node(2);
		head.right = new Node(3);
		head.left.left = new Node(4);
		head.left.right = new Node(5);
		head.left.right.left = new Node(6);
//		preOrderUnRecur(head);
		posOrderUnrecur(head);
		
	}
}

