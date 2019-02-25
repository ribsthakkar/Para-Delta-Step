import com.sun.javaws.exceptions.InvalidArgumentException;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.*;

public class Delta {
	private int delta, source;
	private HashMap<Integer, ArrayList<Node>> bucket;
	public ArrayList<Node> property_map;
	private Stack<Thread> pool;

	public Delta(int d, int source, ArrayList<Node> vertices) {
		this.source = source;
		delta = d;
		bucket = new HashMap<>();
		property_map = vertices;
		pool = new Stack<>();

	}
	
	/*
	 * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
	 */
	public void relax(Node w, int x) {
//		System.out.println("Called relax with vertex " + w + " and weight " + x);
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
					ArrayList<Node> w_list = new ArrayList<>();
					w_list.add(w);
					bucket.put(x / delta, w_list);
				} else {
					if (bucket.get(x / delta).contains(w)) {
						bucket.get(x / delta).add(w);
					}
				}
			} else {
				if (!bucket.containsKey(x / delta)) {
					ArrayList<Node> w_list = new ArrayList<>();
					w_list.add(w);
					bucket.put(x / delta, w_list);
				} else {
					if (!bucket.get(x / delta).contains(w)) {
						bucket.get(x / delta).add(w);
					}
				}
			}
			property_map.put(w, x);
		}
	}

	public HashMap<Integer, Integer> find_requests(ArrayList<Integer> vertices, String kind, Graph g) {
		HashMap<Integer, Integer> output = new HashMap<>();
		for(int u: vertices) {
			for(Node n: g.getAdjacentVertices(u).keySet()) {
				int v = n.getID();
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
			Thread t = new Thread() {
				public void run() {
					int value = requests.get(key);
					relax(key, value);
				}
			};
			pool.add(t);
			t.start();

		}
		try {
			while(!pool.isEmpty()) {
				pool.pop().join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void delta_stepping(Graph g) {
		for(Node node: g.getVertexList()) {
			int n = node.getID();
			property_map.put(n, Integer.MAX_VALUE);
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
