import java.util.*;

public class Delta {
	private int delta, source, totalNodes, totalEdges;
	private HashMap<Integer, Integer> distances;
	private HashMap<Integer, ArrayList<Integer>> bucket;
	private HashMap<Integer, Integer> property_map;

	public Delta(int d, int source, int tn, int te) {
		this.source = source;
		totalNodes = tn;
		totalEdges = te;
		delta = d;
		bucket = new HashMap<Integer, ArrayList<Integer>>();
		distances = new HashMap<Integer, Integer>();
		property_map = new HashMap<Integer, Integer>();
	}
	
	/*
	 * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
	 */
	public void relax(int w, int x) {
		if (x < property_map.get(w)) {
			if (property_map.get(w) != Integer.MAX_VALUE) {
				int index = property_map.get(w) / delta;
				if (bucket.get(index).contains(w)) {
					int new_val = x / delta;
					if (new_val != index) {
						bucket.remove(index);
					}
				} 
				if (!bucket.containsKey(x / delta)) {
					ArrayList<Integer> w_list = new ArrayList<Integer>();
					w_list.add(w);
					bucket.put(x / delta, w_list);
				} else if (bucket.get(x / delta).contains(w)) {
					bucket.get(x / delta).add(w);
				}
			} else {
				if (!bucket.containsKey(x / delta)) {
					ArrayList<Integer> w_list = new ArrayList<Integer>();
					w_list.add(w);
					bucket.put(x / delta, w_list);
				} else if (!bucket.get(x / delta).contains(w)) {
					bucket.get(x / delta).add(w);
				}
			}
		}
	}
	
	
}
