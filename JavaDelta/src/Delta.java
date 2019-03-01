import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;

public class Delta {
    private int delta, source;
    private ConcurrentHashMap<Integer, Set<Node>> bucket;
    private ConcurrentHashMap<Node, Set<Node>> light;
    private ConcurrentHashMap<Node,Set<Node>> heavy;
    public ArrayList<Node> property_map;
    private ExecutorService pool;

    public Delta(int d, int source, ArrayList<Node> vertices) {
        this.source = source;
        delta = d;
        bucket = new ConcurrentHashMap<>();
        property_map = vertices;
        pool = Executors.newFixedThreadPool(8);
        light = new ConcurrentHashMap<>();
        heavy = new ConcurrentHashMap<>();

    }

    private boolean GreaterThanCAS(int node, int newValue, Node prev) {
        while(true) {
            int local = property_map.get(node).getWeight().get();
            if(newValue >= local) {
                return false; // swap failed
            }
            if (property_map.get(node).getWeight().compareAndSet(local, newValue)) {
                if(local < newValue) {
                    System.out.println("BROKEN");
                    System.out.println("Node " + node + " was updating from " + local + " to " + newValue);
                    System.exit(1);
                }
                property_map.get(node).setPrev(prev);
                int i = local / delta;
                try {
                    bucket.get(i).remove(property_map.get(node));
                } catch (NullPointerException ignored) {}
                int n = newValue / delta;
                bucket.putIfAbsent(n,Collections.newSetFromMap(new ConcurrentHashMap<>()));
                bucket.get(n).add(property_map.get(node));
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

    public void relax_requests(Map<Pair<Node,Node>, Set<Integer>> req) {
        for(Pair<Node,Node> n: req.keySet()) {
                Set<Integer> weights = req.getOrDefault(n, new HashSet<>());
//                for(int weight: weights) {
                    Thread t = new Thread(() -> {
                        relax(n.getValue().getID(), Collections.min(weights), n.getKey());
                    });
                    pool.submit(t);
//                }
            }

//        try {
//            for(Thread t:pool) {
//                t.join();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void relax_requests_s(Map<Pair<Node,Node>, Set<Integer>> req) {
        for(Pair<Node,Node> n: req.keySet()) {
            Set<Integer> weights = req.getOrDefault(n, new HashSet<>());
//            for (int w: weights) {
//                relax(n.getValue().getID(), w,n.getKey());
//            }
            relax(n.getValue().getID(), Collections.min(weights), n.getKey());
        }
    }
    public HashMap<Integer, Integer> delta_stepping(Graph g) {
        //Splitting Adjacency list to heavy and light edges for each node
        for(Node node: g.getVertexList()) {
            int n = node.getID();
            property_map.get(n).setWeight(Integer.MAX_VALUE);
            for(Node dest: node.getAdjacent().keySet()) {
                if(node.getAdjacent().get(dest)  > delta) {
                    splitEdges(node, dest, heavy);
                } else {
                    splitEdges(node, dest, light);
                }
            }
        }
//		System.out.println(light.size());
//		System.out.println(heavy.size());
        relax(source, 0, null);
        Map<Pair<Node,Node>, Set<Integer>> requests = new ConcurrentHashMap<>();
        int in = 0;
        Set<Node> s = Collections.newSetFromMap(new ConcurrentHashMap<>());
        while(!bucket.isEmpty()) {
            s.clear();
 			in = Collections.min(bucket.keySet());
            requests.clear();
            while(bucket.get(in) != null && !bucket.get(in).isEmpty()) {
                for (Node v: bucket.get(in)) {
                    Set<Node> ladj = light.get(v);
                    if(ladj != null)
                        setupRequests(g, requests, v, ladj);
                }
                s.addAll(bucket.get(in));
                bucket.get(in).clear();
                relax_requests(requests);
//                System.out.println("Relaxing light");
            }
            requests.clear();
            for (Node v: s) {
                Set<Node> hadj = heavy.get(v);
                if(hadj != null)
                    setupRequests(g, requests, v, hadj);
            }
            relax_requests(requests);
//            System.out.println("Relaxing heavy");
            try {
                if (bucket.get(in).isEmpty())
                    bucket.remove(in);
            }catch (NullPointerException ignored) {}
//            in++;
        }
        pool.shutdown();
        try {
            System.out.println("Waiting");
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Terminated");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<Integer, Integer> out = new HashMap<>();
        for(int i = 0; i < g.nodes().size(); i ++) {
            out.put(i, g.getVertexList().get(i).getWeight().get());
        }
        return out;
    }

    private void splitEdges(Node node, Node dest, ConcurrentHashMap<Node, Set<Node>> buck) {
        buck.putIfAbsent(node, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        buck.get(node).add(dest);
    }

    private void setupRequests(Graph g, Map<Pair<Node, Node>, Set<Integer>> requests, Node v, Set<Node> adj) {
        Pair<Node,Node> p;
        for(Node w: adj) {
//            if (requests.containsKey(new Pair<>(v, w))) {
//                requests.get(new Pair<>(v, w)).add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
//            } else {
//                HashSet<Integer> a = new HashSet<>();
//                a.add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
//                requests.put(new Pair<>(v, w), a);
//            }
            p = new Pair<>(v, w);
            requests.putIfAbsent(p, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            requests.get(p).add(g.getEdgeWeight(v.getID(), w.getID()) + v.getWeight().get());
        }
    }


}