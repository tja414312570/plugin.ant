package plugin.ant.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArraysVsLinked {
	public static void main(String[] args) {
		int len = 50*100000;
		List<Integer> arrays = new ArrayList(1024);
		List<Integer> link = new LinkedList<>();
		long now = System.nanoTime();
		for(int i = 0 ;i<len;i++) {
			arrays.add(i);
		}
		System.out.println("数据添加:"+(System.nanoTime() - now));
		now = System.nanoTime();
		for(int i = 0 ;i<len;i++) {
			link.add(i);
		}
		System.out.println("链表添加:"+(System.nanoTime() - now));
		
	}
}