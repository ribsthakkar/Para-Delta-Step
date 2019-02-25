import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Main {

	public static void main(String[] args)  {

		System.out.println("Hello World!");

		Graph graph = new Graph(true, 7);

		graph.addArc(1, 2, 2);
		graph.addArc(1, 3, 12);
		graph.addArc(1, 4, 7);
		graph.addArc(3, 2, 1);

		graph.addArc(5, 6, 4);

		graph.addArc(5, 7, 5);

		graph.addArc(4, 5, 1);

		graph.addArc(6, 4, 1);

		graph.addArc(5, 1, 1);

		System.out.println(graph.toString());

		Delta d = new Delta(5, 5, graph.getVertexList());
		long totalTime = 0;
		long dTotalTime = 0;
		long startTime;
		long endTime;
		for(int i = 0; i < 100; i ++) {
			startTime = System.nanoTime();
			d.delta_stepping(graph);
			endTime = System.nanoTime();
			totalTime += endTime - startTime;

			startTime = System.nanoTime();
			HashMap<Integer, Integer> dij = graph.dijsktra(5);
			endTime = System.nanoTime();
			dTotalTime += endTime - startTime;
//			for(Node node:d.property_map) {
//				int out = dij.get(node.getID());
//				int other = d.property_map.get(node.getID()).getWeight();
//				if(out != other) {
//					System.out.println("Incorrect execution");
//					System.out.println("Dijkstra gave " + out);
//					System.out.println("Delta gave " + other);
//				}
//			}

		}
		long time = (totalTime) / 100;
		long dTime = (dTotalTime) / 100;
		System.out.printf("Delta takes Average %d nanoseconds\n", time);
		System.out.printf("Dijkstra takes Average %d nanoseconds\n", dTime);
//
//		System.out.println(graph.dijsktra(5));
//
//		d.delta_stepping(graph);
//
//		for(Node node:d.property_map) {
//			System.out.println("Cost to Node " + node + " is "+ node.getWeight());
//		}
	}
}
