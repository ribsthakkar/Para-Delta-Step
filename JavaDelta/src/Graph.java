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
			} else if(!m.containsKey(end)) {
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
			current = q.poll();
			if(current.isVisited()) {
				continue;
			}
			current.mark();
			for(Node adjacent:current.getAdjacent().keySet()) {
				int edgeWeight = current.getAdjacent().get(adjacent);
				if(!adjacent.isVisited()) {
					if(adjacent.getWeight().get() > current.getWeight().get() + edgeWeight) {
						adjacent.setWeight(current.getWeight().get() + edgeWeight);
						adjacent.setPrev(current);
					}
					q.remove(adjacent);
					q.add(adjacent);
//					if(current.getID() == 49 && adjacent.getID() == 48)
//						System.out.println(q);
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
	private Stack<Node> getPath_s(int source) {
		Stack<Node> out = new Stack<>();
		out.push(vertexList.get(source));
		Node s = vertexList.get(source);
		while(s.getPrev().get() != null) {
			out.push(s.getPrev().get());
			s = s.getPrev().get();
		}

		return out;
	}

	public ArrayList<Node> getPath(int source) {
		Stack<Node> out = getPath_s(source);
		ArrayList<Node> o = new ArrayList<>();
		while (!out.isEmpty()){
			o.add(out.pop());
		}
		return o;
	}
	public int getPathCost(int source) {
		Stack<Node> out = getPath_s(source);
		Node first = out.pop();
		Node next = null;
		if(!out.isEmpty())
			next = out.pop();
		int total  = 0;

		while(first != null && next != null) {
			System.out.println("For edge (" + first + "," + next + ") it costs " + getEdgeWeight(first.getID(), next.getID()));
			total+= getEdgeWeight(first.getID(), next.getID());
			first = next;
			if(!out.isEmpty()) {
				next = out.pop();
			} else {
				next = null;
			}
		}

		return total;
	}

	public void resetPrev() {
		for(int i = 0; i < numNodes + 1; i ++) {
			vertexList.get(i).setPrev(null);
		}
	}
}