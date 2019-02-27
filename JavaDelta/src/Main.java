import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;


public class Main {

	public static void main(String[] args)  {

		int TESTS = 10000;
		System.out.println("Hello World!");
		Graph graph;
		String fileName = "wiki.dimacs";
		graph = new Graph(true);
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			stream.forEach(graph::addStringArc);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		graph = new Graph(true, 7);
//
//		graph.addArc(1, 2, 2);
//
//		graph.addArc(1, 3, 12);
//
//		graph.addArc(1, 4, 7);
//
//		graph.addArc(3, 2, 1);
//
//		graph.addArc(5, 6, 4);
//
//		graph.addArc(5, 7, 5);
//
//		graph.addArc(4, 5, 1);
//
//		graph.addArc(6, 4, 1);
//
//		graph.addArc(5, 1, 1);
		long totalTime = 0;
		long dTotalTime = 0;
		long startTime;
		long endTime;
		for(int i = 0; i < TESTS; i ++) {
			startTime = System.nanoTime();
			HashMap<Integer, Integer> dij = graph.dijsktra(5);
			endTime = System.nanoTime();
			dTotalTime += endTime - startTime;

			Delta d = new Delta(10, 5, graph.getVertexList());
			startTime = System.nanoTime();
			HashMap<Integer, Integer> delt = d.delta_stepping(graph);
			endTime = System.nanoTime();
			totalTime += endTime - startTime;
			int count = 0;
			if(delt != null && dij != null) {
				if (!delt.equals(dij)) {
//					System.exit(1);
 					System.out.println("Incorrect execution");
					for (int node : delt.keySet()) {
						int out = dij.get(node);
						int other = delt.get(node);
						if (out != other) {
//							System.out.println("Dijkstra gave " + out);
//							System.out.println("Delta gave " + other);
							count++;
						}
					}
				}
			}
//			System.out.println(i);
// 			System.out.println("We had " + count + " nodes with errors;");
		}
		long time = (totalTime) / TESTS;
		long dTime = (dTotalTime) / TESTS;
		System.out.printf("Delta takes Average %d nanoseconds\n", time);
		System.out.printf("Dijks takes Average %d nanoseconds\n", dTime);
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
