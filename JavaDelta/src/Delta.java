import com.sun.javaws.exceptions.InvalidArgumentException;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.*;

public class Delta {
	private int delta, source, totalNodes, totalEdges;
	private HashMap<Integer, Integer> distances;
	private HashMap<Integer, ArrayList<Integer>> bucket;
	public HashMap<Integer, Integer> property_map;

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

	public HashMap<Integer, Integer> find_requests(ArrayList<Integer> vertices, String kind, Graph<Integer> g) {
		HashMap<Integer, Integer> output = new HashMap<>();
		for(int u: vertices) {
			for(int v: g.getAdjacentVertices(u)) {
				int edgeWeight = property_map.get(u) + g.getEdgeWeight(u, v);
				if (kind.equals("light")) {
					if(g.getEdgeWeight(u,v) <= delta) {
						if(output.containsKey(v)) {
							if(edgeWeight < output.get(v)) {
								output.put(v, edgeWeight);
							}
						} else {
							output.put(v, edgeWeight);
						}
					}
				} else if (kind.equals("heavy")) {
					if(g.getEdgeWeight(u,v) > delta) {
						if(output.containsKey(v)) {
							if(edgeWeight < output.get(v)) {
								output.put(v, edgeWeight);
							}
						} else {
							output.put(v, edgeWeight);
						}
					}
				} else {
					System.err.println("Invalid edge type");
					System.exit(1);
				}
			}
		}
		return output;
	}

	public void relax_requests(HashMap<Integer,Integer> requests) {
		for (Integer key : requests.keySet()) {
			int value = requests.get(key);
			relax(key, value);
		}
	}

	public void delta_stepping(Graph<Integer> g) {
		for(int node: g.getVertexList()) {
			property_map.put(node, Integer.MAX_VALUE);
		}

		relax(source, 0);
		HashMap<Integer, Integer> req;
		int ctr = 0;
		while(bucket.size() > 0) {
			int i = Collections.min(bucket.keySet());
			int sub_ctr = 0;
			ArrayList<Integer> r = new ArrayList<>();
			while(bucket.containsKey(i)) {
				req = find_requests(bucket.get(i), "light", g);
				r.addAll(bucket.get(i));
				bucket.remove(i);
				relax_requests(req);
				sub_ctr +=1;
			}
			req = find_requests(r, "heavy", g);
			relax_requests(req);
			ctr += 1;
		}
	}
	
	
}
