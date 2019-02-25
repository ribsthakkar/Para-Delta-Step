import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {

		System.out.println("Hello World!");

		Graph<Integer> graph = new Graph<>(true);
		ArrayList<Edge<Integer>> connected = new ArrayList<>();
		connected.add(new Edge<>(2, 2));
		connected.add(new Edge<>(3, 12));
		connected.add(new Edge<>(4, 7));
		graph.add(1, connected);

		graph.addArc(3, 2, 1);

		graph.addArc(8, 6, 4);

		graph.addArc(8, 7, 5);

		graph.addArc(4, 8, 1);

		graph.addArc(6, 4, 1);

		graph.addArc(8, 1, 1);

		System.out.println(graph.toString());

		System.out.println(Main.dijkstraShortestPath(graph, 8));
		Delta d = new Delta(5, 8, graph.numNodes, graph.numEdges);
		d.delta_stepping(graph);
		for(int node:d.property_map.keySet()) {
			System.out.println("Cost to Node " + node + " is "+ d.property_map.get(node));
		}
	}

	public static <V> HashMap<V, Double> dijkstraShortestPath(Graph<V> graph, V source) {
		HashMap<V, Double> distances = new HashMap<V, Double>();
		ArrayList<V> queue = new ArrayList<V>();
		ArrayList<V> visited = new ArrayList<V>();
		queue.add(0, source);
		distances.put(source, 0.0);
		while (!queue.isEmpty()) {

			V currentVertex = queue.remove(queue.size() - 1);

			// to save time we initialize all the distances to infinity as we go
			distances.putIfAbsent(currentVertex, Double.POSITIVE_INFINITY);
			for (V adjacentVertex : graph.getAdjacentVertices(currentVertex)) {
				distances.putIfAbsent(adjacentVertex, Double.POSITIVE_INFINITY);

				// if the distance between the source and the adjacent vertex is
				// greater than the distance between the source and the current
				// vertex PLUS the weight between the current and adjacent
				// vertex, then we have found a shorter path than already
				// existed
				if (true) {

					if (distances.get(adjacentVertex) > graph.getDistanceBetween(currentVertex, adjacentVertex)
							+ distances.get(currentVertex)) {

						distances.put(adjacentVertex,
								graph.getDistanceBetween(currentVertex, adjacentVertex) + distances.get(currentVertex));
					}
				}

				if (!visited.contains(adjacentVertex) && !queue.contains(adjacentVertex)) {
					queue.add(0, adjacentVertex);
				}
			}
			visited.add(currentVertex);

		}

		// since the above statments only added the vertices as needed,
		// verticies that are completely unconnected to the source are not added
		// yet, so this adds them now
		for (V v : graph.getVertexList()) {
			if (!distances.containsKey(v)) {
				distances.put(v, Double.POSITIVE_INFINITY);
			}
		}

		return distances;
	}
}
