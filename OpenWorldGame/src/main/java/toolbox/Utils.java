package toolbox;

import java.util.List;

public class Utils {
	
	//TODO Make a generic method.
	public static float[] floatListToArray(List<Float> list) {
		int size = list.size();
		float[] array = new float[size];
		
		for (int i=0; i < size; i++) {
			array[i] = list.get(i);
		}
		
		return array;
	}
	
	public static int[] integerListToArray(List<Integer> list) {
		int size = list.size();
		int[] array = new int[size];
		
		for (int i=0; i < size; i++) {
			array[i] = list.get(i);
		}
		
		return array;
	}

}
