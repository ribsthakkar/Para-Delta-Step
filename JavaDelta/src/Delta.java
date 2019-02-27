import com.sun.javaws.exceptions.InvalidArgumentException;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class Delta {
	private int delta, source;
	private ConcurrentHashMap<Integer, HashSet<Node>> bucket;
	private ConcurrentHashMap<Node, HashSet<Node>> light;
	private ConcurrentHashMap<Node,HashSet<Node>> heavy;
	public ArrayList<Node> property_map;
	private Set<Thread> pool;

	public Delta(int d, int source, ArrayList<Node> vertices) {
		this.source = source;
		delta = d;
		bucket = new ConcurrentHashMap<>();
		property_map = vertices;
		pool = new HashSet<>();
		light = new ConcurrentHashMap<>();
		heavy = new ConcurrentHashMap<>();

	}

	private boolean GreaterThanCAS(int w, int newValue) {
		while(true) {
			int local = property_map.get(w).getWeight().get();
			if(newValue >= local) {
				return false; // swap failed
			}
			if (property_map.get(w).getWeight().compareAndSet(local, newValue)) {
				int i = local / delta;
				if(bucket.containsKey(i)) {
					bucket.get(i).remove(property_map.get(w));
				}
				int n = newValue / delta;
				if(bucket.containsKey(n)) {
					bucket.get(n).add(property_map.get(w));
				} else {
					HashSet<Node> a = new HashSet<>();
					a.add(property_map.get(w));
					bucket.put(n, a);
				}
				return true;  // swap successful }
			}
			// keep trying
		}
	}

	private boolean LessThanCAS(int w, int newValue) {
		while(true) {
			int local = property_map.get(w).getWeight().get();
			if(newValue != local) {
				return false; // swap failed
			}
			return true;
			// keep trying
		}
	}
	/*
	 * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
	 */
	public synchronized void relax(int w, int x) {
//		 System.out.println("Called relax with vertex " + w + " and weight " + x);
//		if (GreaterThanCAS(w, x)) {
//			if (LessThanCAS(w, Integer.MAX_VALUE)) {
//				int index = property_map.get(w).getWeight().get() / delta;
//				if (bucket.get(index).contains(w)) {
//					int new_val = x / delta;
//					if (new_val != index) {
//						bucket.remove(index);
//					}
//				}
//				if (!bucket.containsKey(x / delta)) {
//					Set<Integer> w_list = new ConcurrentSkipListSet<>();
//					w_list.add(w);
//					bucket.put(x / delta, w_list);
//				} else {
//					if (bucket.get(x / delta).contains(w)) {
//						bucket.get(x / delta).add(w);
//					}
//				}
//			} else {
//				if (!bucket.containsKey(x / delta)) {
//					Set<Integer> w_list = new ConcurrentSkipListSet<>();
//					w_list.add(w);
//					bucket.put(x / delta, w_list);
//				} else {
//                    bucket.get(x / delta).add(w);
//				}
//				delta *= 1.5;
//			}
////			property_map.get(w).setWeight(x);
//		}
		GreaterThanCAS(w, x);

	}

	public HashMap<Integer, Integer> find_requests(Set<Integer> vertices, boolean kind, Graph g) {
		HashMap<Integer, Integer> output = new HashMap<>();
		for(int u: vertices) {
			for(Node n: g.getAdjacentVertices(u).keySet()) {
				int v = n.getID();
				int edgeWeight = property_map.get(u).getWeight().get() + g.getEdgeWeight(u, v);
				if (kind) {
					if(g.getEdgeWeight(u,v) <= delta) {
						if(output.containsKey(v)) {
							if(edgeWeight < output.get(v)) {
								output.put(v, edgeWeight);
							}
						} else {
							output.put(v, edgeWeight);
						}
					}
				} else {
					if(g.getEdgeWeight(u,v) > delta) {
						if(output.containsKey(v)) {
							if(edgeWeight < output.get(v)) {
								output.put(v, edgeWeight);
							}
						} else {
							output.put(v, edgeWeight);
						}
					}
				}
			}
		}
		return output;
	}

//	public void relax_requests(HashMap<Integer,Integer> requests) {
//		pool = new HashSet<>();
//        for (Integer key : requests.keySet()) {
//			Thread t = new Thread(() -> {
//				int value = requests.get(key);
//				relax(key, value);
//			});
//			pool.add(t);
////			t.start();
//
//		}
//		for(Thread t:pool) {
//			t.start();
//		}
//		try {
//			for(Thread t: pool) {
//				t.join();
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public void relax_requests(HashMap<Node, HashSet<Integer>> req) {
		pool = new HashSet<>();
		for(Node n: req.keySet()) {
			HashSet<Integer> weights = req.get(n);
			for (int w: weights) {
				relax(n.getID(), w);
				Thread t = new Thread(() -> relax(n.getID(),w));
				pool.add(t);
				t.start();
			}
		}
		try {
			for(Thread t:pool) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public HashMap<Integer, Integer> delta_stepping(Graph g) {
		for(Node node: g.getVertexList()) {
			int n = node.getID();
			property_map.get(n).setWeight(Integer.MAX_VALUE);
			for(Node dest: node.getAdjacent().keySet()) {
				if(node.getAdjacent().get(dest) > delta) {
					if(heavy.containsKey(node)) {
						heavy.get(node).add(dest);
					} else {
						HashSet<Node> nodes = new HashSet<>();
						nodes.add(dest);
						heavy.put(node, nodes);
					}
				} else {
					if(light.containsKey(node)) {
						light.get(node).add(dest);
					} else {
						HashSet<Node> nodes = new HashSet<>();
						nodes.add(dest);
						light.put(node, nodes);
					}
				}
			}
		}
//		System.out.println(light.size());
//		System.out.println(heavy.size());
		relax(source, 0);
		int ctr = 0;
		while(bucket.size() > 0) {
			Set<Node> s = new ConcurrentSkipListSet<>();
			try {
//				int i = Collections.min(bucket.keySet());
//				int sub_ctr = 0;
//				Set<Integer> r = new ConcurrentSkipListSet<>();
//				while(bucket.containsKey(i)) {
//					req = find_requests(bucket.get(i), true, g);
//					r.addAll(bucket.get(i));
//					bucket.remove(i);
//					relax_requests(req);
//					sub_ctr +=1;
//				}
//				req = find_requests(r, false, g);
//				relax_requests(req);
//				ctr += 1;
			int in = Collections.min(bucket.keySet());
			HashMap<Node, HashSet<Integer>> requests = new HashMap<>();
			while(!bucket.get(in).isEmpty()) {
				for (Node v: bucket.get(in)) {
					HashSet<Node> ladj = light.remove(v);
					if(ladj != null)
						setupRequests(g, requests, v, ladj);
				}
				s.addAll(bucket.get(in));
				bucket.get(in).clear();
				relax_requests(requests);
			}
			requests = new HashMap<>();
			for (Node v: s) {
				HashSet<Node> wadj = heavy.remove(v);
				if(wadj != null)
					setupRequests(g, requests, v, wadj);
			}
			relax_requests(requests);
			if(bucket.get(in).isEmpty())
				bucket.remove(in);
			} catch (NoSuchElementException e) {
				System.out.println(e);
				return null;
			}
		}
		HashMap<Integer, Integer> out = new HashMap<>();
		for(int i = 0; i < g.nodes().size(); i ++) {
			out.put(i, g.getVertexList().get(i).getWeight().get());
			g.getVertexList().get(i).setWeight(Integer.MAX_VALUE);
		}
		return out;
	}

	private void setupRequests(Graph g, HashMap<Node, HashSet<Integer>> requests, Node v, HashSet<Node> adj) {
		for(Node w: adj) {
			if (requests.containsKey(w)) {
				requests.get(w).add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
			} else {
				HashSet<Integer> a = new HashSet<>();
				a.add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
				requests.put(w, a);
			}
		}
	}

}
