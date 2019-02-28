import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class Delta {
	private int delta, source;
	private Map<Integer, Set<Node>> bucket;
	private Map<Node, HashSet<Node>> light;
	private Map<Node,HashSet<Node>> heavy;
	public ArrayList<Node> property_map;
	private Set<Thread> pool;

	public Delta(int d, int source, ArrayList<Node> vertices) {
		this.source = source;
		delta = d;
		bucket = new ConcurrentHashMap<>();
		property_map = vertices;
		pool = new HashSet<>();
		light = new HashMap<>();
		heavy = new HashMap<>();

	}

	private boolean GreaterThanCAS(int node, int newValue, Node prev) {
		while(true) {
			int local = property_map.get(node).getWeight().get();
			if(newValue >= local) {
				return false; // swap failed
			}
			if (property_map.get(node).getWeight().compareAndSet(local, newValue)) {
				property_map.get(node).setPrev(prev);
				int i = local / delta;
				if(bucket.containsKey(i)) {
					bucket.get(i).remove(property_map.get(node));
				}
				int n = newValue / delta;
				if(bucket.containsKey(n)) {
					bucket.get(n).add(property_map.get(node));
				} else {
					Set<Node> a = new ConcurrentSkipListSet<>();
					a.add(property_map.get(node));
					bucket.put(n, a);
				}
				return true;  // swap successful }
			}
			// keep trying
		}
	}
	/*
	 * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
	 */
	public void relax(int w, int x, Node prev) {
		GreaterThanCAS(w, x, prev);
	}

	public void relax_requests(HashMap<Pair<Node,Node>, HashSet<Integer>> req) {
		pool = new HashSet<>();
		for(Pair<Node,Node> n: req.keySet()) {
			HashSet<Integer> weights = req.get(n);
			for (int w: weights) {
				Thread t = new Thread(() -> relax(n.getValue().getID(), w,n.getKey() ));
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

	public void relax_requests_s(HashMap<Pair<Node,Node>, HashSet<Integer>> req) {
		pool = new HashSet<>();
		for(Pair<Node,Node> n: req.keySet()) {
			HashSet<Integer> weights = req.get(n);
			for (int w: weights) {
				relax(n.getValue().getID(), w,n.getKey());
			}
		}
	}
	public HashMap<Integer, Integer> delta_stepping(Graph g) {
		//Splitting Adjacency list to heavy and light edges for each node
		for(Node node: g.getVertexList()) {
			int n = node.getID();
			property_map.get(n).setWeight(Integer.MAX_VALUE);
			for(Node dest: node.getAdjacent().keySet()) {
				if(node.getAdjacent().get(dest)  > delta) {
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
		relax(source, 0, null);
		int ctr = 0;
		HashMap<Pair<Node,Node>, HashSet<Integer>> requests = new HashMap<>();
		int in = 0;
		Set<Node> s = new HashSet<>();
		while(!bucket.isEmpty()) {
			s.clear();
 			in = Collections.min(bucket.keySet());
			requests.clear();
			while(bucket.get(in) != null && !bucket.get(in).isEmpty()) {
				for (Node v: bucket.get(in)) {
					HashSet<Node> ladj = light.get(v);
					if(ladj != null)
						setupRequests(g, requests, v, ladj);
				}
				s.addAll(bucket.get(in));
				bucket.get(in).clear();
				relax_requests(requests);
			}
			requests.clear();
			for (Node v: s) {
				HashSet<Node> wadj = heavy.get(v);
				if(wadj != null)
					setupRequests(g, requests, v, wadj);
			}
			relax_requests(requests);
			if(bucket.get(in) != null && bucket.get(in).isEmpty())
				bucket.remove(in);
//			in++;
		}
		HashMap<Integer, Integer> out = new HashMap<>();
		for(int i = 0; i < g.nodes().size(); i ++) {
			out.put(i, g.getVertexList().get(i).getWeight().get());
			g.getVertexList().get(i).setWeight(Integer.MAX_VALUE);
		}
		return out;
	}

	private void setupRequests(Graph g, HashMap<Pair<Node, Node>, HashSet<Integer>> requests, Node v, HashSet<Node> adj) {
		for(Node w: adj) {
			if (requests.containsKey(new Pair<>(v, w))) {
				requests.get(new Pair<>(v, w)).add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
			} else {
				HashSet<Integer> a = new HashSet<>();
				a.add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
				requests.put(new Pair<>(v, w), a);
			}
		}
	}


}
