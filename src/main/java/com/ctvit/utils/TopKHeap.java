package com.ctvit.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


/*
 * 小堆，堆顶最小；保存了最大的size个值；
 */
public class TopKHeap<T/* extends Comparable<? super T> */> {

	private int size;
	private int maxSize;
	// DefaultConfig.getInstance().getTopK()
	private Object[] data = new Object[500];

	public TopKHeap(T[] data) {
		super();
		try {
			this.initHeap(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TopKHeap(int fixedSize) {
		super();
		assert fixedSize >= 1;
		this.size=0;
		this.maxSize = fixedSize;
	}

	@SuppressWarnings("unchecked")
	public void initHeap(T[] data) throws Exception {
		assert data.length >= 1;
		if (this.data.length <= this.size) {
			this.data = new Object[(int) (data.length * 1.5)];
		}
		this.maxSize = this.size = data.length;
		System.arraycopy(data, 0, this.data, 0, this.size);
		int startPos = this.getParentIndex(this.size - 1);
		for (int i = startPos; i >= 0; i--) {
			this.shiftdown(i);
		}
	}

	@SuppressWarnings("unchecked")
	public T getHeapTop() {
		return (T) this.data[0];
	}

	/**
	 * 加元素到堆中，但是保持堆 的大小
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void addToHeap(T value) throws Exception {
		if (this.maxSize > this.size) {
			this.data[this.size] = value;
			this.shiftup(this.size++);
		} else {
			if (!less(value, this.getHeapTop())) {
				this.data[0] = value;
				this.shiftdown(0);
			}
		}
	}

	// is x < y ?
	private boolean less(T x, T y) throws Exception {
		if (x instanceof Entry && y instanceof Entry) {
			Object o = ((Entry) x).getValue();
			Object oy = ((Entry) y).getValue();
			if (o instanceof Double) {
				double xv = 0, yv = 0;
				xv = (Double) (o);
				yv = (Double) (((Entry) y).getValue());
				return (xv < yv);
			} else if (o instanceof Integer) {
				int xv = 0, yv = 0;
				xv = (Integer) (o);
				yv = (Integer) (((Entry) y).getValue());
				return (xv < yv);
			} else if (o instanceof Float) {
				Float xv = 0.0f, yv = 0.0f;
				xv = (Float) (o);
				yv = (Float) (((Entry) y).getValue());
				return (xv < yv);
			} 
		} else {
			System.out.println("x:"+x.getClass());
			throw new Exception("unsupport types now");
		}
		return false;
	}

	private void shiftup(int pos) throws Exception {
		int parentIdx = this.getParentIndex(pos);
		while (pos != 0 && less(this.getValue(pos), this.getValue(parentIdx))) {
			this.swap(pos, parentIdx);
			pos = parentIdx;
			parentIdx = this.getParentIndex(pos);
		}
	}

	public T removeTop() throws Exception {
		T rst = this.getHeapTop();
		this.data[0] = this.data[--this.size];
		this.shiftdown(0);
		return rst;
	}

	public boolean hasNext() {
		return this.size > 0;
	}

	@SuppressWarnings("unchecked")
	public T[] getData() {
		return (T[]) this.data;
	}

	@SuppressWarnings("unchecked")
	public T getValue(int index) {
		return (T) this.data[index];
	}

	private int getParentIndex(int pos) {
		return (pos - 1) / 2;
	}

	private int getLeftChildIdx(int pos) {
		return pos * 2 + 1;
	}

	private int getRightChildIdx(int pos) {
		return pos * 2 + 2;
	}

	private void swap(int idx1, int idx2) {
		T tmp = this.getValue(idx1);
		this.data[idx1] = this.getValue(idx2);
		this.data[idx2] = tmp;
	}

	/**
	 * 节点值向下级交换，构造堆
	 * 
	 * @param pos
	 * @throws Exception
	 */
	private void shiftdown(int pos) throws Exception {
		int leftChildIdx = this.getLeftChildIdx(pos);
		// 没有子节点了
		if (leftChildIdx >= this.size) {
			return;
		}
		int rightChildIdx = getRightChildIdx(pos);
		int toBeSwapIdx = leftChildIdx;
		if (rightChildIdx < this.size
				&& !less(this.getValue(leftChildIdx),
						this.getValue(rightChildIdx))) {
			toBeSwapIdx = rightChildIdx;
		}
		// swap
		if (!less(this.getValue(pos), this.getValue(toBeSwapIdx))) {
			this.swap(pos, toBeSwapIdx);
			this.shiftdown(toBeSwapIdx);
		}
	}

	public boolean isFull() {
		return this.maxSize == this.size;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getSize() {
		return size;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * Integer[] data = { 7, 12, 13, 24, 8, 6, 4, 27, 14, 8, 12, 56, 22 };
		 * TopKHeap<Integer> heap = new TopKHeap<Integer>(data); while
		 * (heap.hasNext()) { System.out.print(heap.removeTop());
		 * System.out.print("  "); }
		 * 
		 * System.out.println("\n-------------------------");
		 * heap.initHeap(data); for (int i = 0; i < 10; i++) {
		 * heap.addToHeap(i); } while (heap.hasNext()) {
		 * System.out.print(heap.removeTop()); System.out.print("  "); }
		 * 
		 * System.out.println("\n**************************");
		 */
		/*
		 * TopKHeap heap = new TopKHeap<Integer>(10); Random rd = new Random();
		 * for (int i = 0; i < 20; i++) { int value = rd.nextInt(100);
		 * System.out.print(value); System.out.print("  ");
		 * heap.addToHeap(value); }
		 * System.out.println("\n#############################"); while
		 * (heap.hasNext()) { System.out.print(heap.removeTop());
		 * System.out.print("  "); }
		 */
		TopKHeap heap = new TopKHeap<Map.Entry>(10);
		Random rd = new Random();
		Map<String, Double> map = new HashMap<String, Double>();
		for (int i = 0; i < 20; i++) {
			double value = rd.nextDouble();
			System.out.print(value);
			System.out.print("  ");
			map.put(String.valueOf(i), value);
		}
		map.put(String.valueOf(20), 10.0);
		for (Map.Entry e : map.entrySet()) {
			heap.addToHeap(e);
		}
		System.out.println("\n#############################");
		System.out.println(heap.getValue(heap.size - 1));
		while (heap.hasNext()) {
			System.out.print(heap.removeTop());
			System.out.print("  ");
		}

	}

}