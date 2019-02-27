import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Graph {


	/**
	 * This list holds all the vertices so that we can iterate over them in the
	 * toString function
	 */
	private ArrayList<Node> vertexList;
	private HashMap<Integer, HashMap<Integer, Integer>> edgeList;
	private boolean directed;
	private int numEdges;
	private int numNodes;

	public Graph(boolean isDirected, int nodes) {
		directed = isDirected;
		edgeList = new HashMap<>();
		vertexList = new ArrayList<>();
		numNodes = nodes;
		Node n;
		for(int i = 0; i < numNodes + 1; i++) {
			n = new Node(i);
			vertexList.add(n);
		}
	}
	public Graph(boolean isDirected) {
		directed = isDirected;
		edgeList = new HashMap<>();
		vertexList = new ArrayList<>();
		numNodes = 0;

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
			HashMap<Integer, Integer> m = new HashMap<>();
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
					if(adjacent.getWeight().get() > current.getWeight().get() + weight) {
						adjacent.setWeight(current.getWeight().get() + weight);
					}
					q.add(adjacent);
				}
			}
		}
		unmarkAll();
		HashMap<Integer, Integer> out = new HashMap<>();
		for(int i = 0; i < numNodes + 1; i ++) {
			out.put(i, vertexList.get(i).getWeight().get());
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

	public void addStringArc(String s) {
		String[] arr = s.split("\\s+");
		if(arr[0].equals("p")) {
			numNodes = Integer.parseInt(arr[2]);
			Node n;
			for(int i = 0; i < numNodes + 1; i++) {
				n = new Node(i);
				vertexList.add(n);
			}
		} else if(arr[0].equals("a")){
			int source = Integer.parseInt(arr[1]);
			int dest = Integer.parseInt(arr[2]);
			int weight = Integer.parseInt(arr[3]);
			addArc(source, dest, weight);
		}
	}
}