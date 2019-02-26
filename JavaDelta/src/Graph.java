import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Graph {


	/**
	 * This list holds all the vertices so that we can iterate over them in the
	 * toString function
	 */
	private ArrayList<Node> vertexList;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> edgeList;
	private boolean directed;
	private int numEdges;
	private int numNodes;

	public Graph(boolean isDirected, int nodes) {
		directed = isDirected;
		edgeList = new ConcurrentHashMap<>();
		vertexList = new ArrayList<>();
		numNodes = nodes;
		Node n;
		for(int i = 0; i < numNodes + 1; i++) {
			n = new Node(i);
			vertexList.add(n);
		}
	}

	public void addArc(int source, int end, int weight) {
		if (!directed) {
			return;
		}
		vertexList.get(source).addEdge(vertexList.get(end), weight);
		if(edgeList.containsKey(source)) {
			Map<Integer, Integer> m = edgeList.get(source);
			if(m.containsKey(end) && m.get(end) < weight) {
				m.put(end, weight);
			} else {
				m.put(end, weight);
			}
		} else {
			ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>();
			m.put(end, weight);
			edgeList.put(source, m);
		}
	}

	public void unmarkAll() {
		for(Node n: vertexList) {
			n.unmark();
		}
	}
	public ArrayList<Node> nodes() {
		return vertexList;
	}
	public HashMap<Node, Integer> getAdjacentVertices(int source) {
		return vertexList.get(source).getAdjacent();
	}

	public HashMap<Integer, Integer> dijsktra(int source) {
		PriorityQueue<Node> q = new PriorityQueue<>();
		q.add(vertexList.get(source));
		vertexList.get(source).setWeight(0);
		Node current;
		while(!q.isEmpty()) {
			current = q.remove();
			if(current.isVisited()) {
				continue;
			}
			current.mark();
			for(Node adjacent:current.getAdjacent().keySet()) {
				int weight = current.getAdjacent().get(adjacent);
				if(!adjacent.isVisited()) {
					if(adjacent.getWeight() > current.getWeight() + weight) {
						adjacent.setWeight(current.getWeight() + weight);
					}
					q.add(adjacent);
				}
			}
		}
		unmarkAll();
		HashMap<Integer, Integer> out = new HashMap<>();
		for(int i = 0; i < numNodes + 1; i ++) {
			out.put(i, vertexList.get(i).getWeight());
			vertexList.get(i).setWeight(Integer.MAX_VALUE);
		}
		return out;
	}

	public int getEdgeWeight(int source, int dest) {
		return edgeList.get(source).get(dest);
	}

	public ArrayList<Node> getVertexList() {
		return vertexList;
	}
}