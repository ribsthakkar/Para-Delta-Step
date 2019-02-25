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

		Delta d = new Delta(5, 5, );
		long totalTime = 0;
		long dTotalTime = 0;
		long startTime;
		long endTime;
		for(int i = 0; i < 10000000; i ++) {
			startTime = System.nanoTime();
			d.delta_stepping(graph);
			endTime = System.nanoTime();
			totalTime += endTime - startTime;

			startTime = System.nanoTime();
			HashMap<Integer, Integer> dij = graph.dijsktra(5);
			endTime = System.nanoTime();
			dTotalTime += endTime - startTime;
			for(int node:d.property_map.keySet()) {
				int out = dij.get(node);
				int other = d.property_map.get(node);
				if(out != other) {
					System.out.println("Incorrect execution");
					System.out.println("Dijkstra gave " + out);
					System.out.println("Delta gave " + other);
				}
			}

		}
		long time = (totalTime) / 10000000;
		long dTime = (dTotalTime) / 10000000;
		System.out.printf("Delta takes Average %d nanoseconds\n", time);
		System.out.printf("Dijkstra takes Average %d nanoseconds\n", dTime);
//
//		System.out.println(graph.dijsktra(5));
//		d.delta_stepping(graph);
//
//		for(int node:d.property_map.keySet()) {
//			System.out.println("Cost to Node " + node + " is "+ d.property_map.get(node));
//		}
	}
}
