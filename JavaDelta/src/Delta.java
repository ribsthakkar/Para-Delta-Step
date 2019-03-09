import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.*;

public class Delta {
//    private int delta, source;
//    private ConcurrentHashMap<Integer, Set<Node>> bucket;
//    private ConcurrentHashMap<Node, Set<Node>> light;
//    private ConcurrentHashMap<Node,Set<Node>> heavy;
//    public ArrayList<Node> property_map;
//    private ExecutorService pool;

    private int delta;
    private int source;
    private int nThreads = 8;
    private int numBuckets;
    private Set<Node>[] buckets;
    private ExecutorService exec;
    private ExecutorCompletionService<Void> completionService;
    private Queue<Node> vertQueue;
    private Runnable lightRelax;
    private Runnable heavyRelax;
    public ArrayList<Node> property_map;
    private volatile boolean allAdded;
    private int maxEdge;
    private int in;

    public Delta(int d, int source, Graph g) {
//        this.source = source;
//        delta = d;
//        bucket = new ConcurrentHashMap<>();
//        property_map = vertices;
//        pool = Executors.newFixedThreadPool(8);
//        light = new ConcurrentHashMap<>();
//        heavy = new ConcurrentHashMap<>();

        delta = d;
        exec = Executors.newFixedThreadPool(nThreads);
        completionService = new ExecutorCompletionService<>(exec);
        property_map = g.getVertexList();
        vertQueue = new ConcurrentLinkedQueue<>();
        lightRelax = new LightRelaxRequests(vertQueue);
        heavyRelax = new HeavyRelaxRequests(vertQueue);
        this.source = source;
        maxEdge = 0;
        for(Node v: property_map) {
            for(Node w: v.getAdjacent().keySet()) {
                maxEdge = Integer.max(maxEdge, g.getEdgeWeight(v.getID(), w.getID()));
            }
        }
        numBuckets = maxEdge/delta + 2;
        buckets = new Set[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            buckets[i] = new ConcurrentSkipListSet();
        }
        in = 0;
    }

//    private boolean GreaterThanCAS(int node, int newValue, Node prev) {
//        while(true) {
//            int local = property_map.get(node).getWeight().get();
//            if(newValue >= local) {
//                return false; // swap failed
//            }
//            if (property_map.get(node).getWeight().compareAndSet(local, newValue)) {
//                if(local < newValue) {
//                    System.out.println("BROKEN");
//                    System.out.println("Node " + node + " was updating from " + local + " to " + newValue);
//                    System.exit(1);
//                }
//                property_map.get(node).setPrev(prev);
//                int i = local / delta;
//                int n = newValue / delta;
//                if(i != n) {
//                    try {
//                        bucket.get(i).remove(property_map.get(node));
//                    } catch (NullPointerException ignored) {
//                    }
//                    bucket.putIfAbsent(n, Collections.newSetFromMap(new ConcurrentHashMap<>()));
//                    bucket.get(n).add(property_map.get(node));
//                    return true;  // swap successful }
//                    }
//            }
//            // keep trying
//        }
//    }
    /*
     * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
     */

//    public void relax_requests(Map<Pair<Node,Node>, Set<Integer>> req) {
//        for(Pair<Node,Node> n: req.keySet()) {
////                System.out.println(n);
//                Set<Integer> weights = req.getOrDefault(n, new HashSet<>());
//                req.remove(n);
////                for(int weight: weights) {
//                    Thread t = new Thread(() -> {
//                        relax(n.getValue().getID(), Collections.min(weights), n.getKey());
//                    });
//                    pool.submit(t);
////                }
//            }
//
////        try {
////            for(Thread t:pool) {
////                t.join();
////            }
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//    }

//    public void relax_requests_s(Map<Pair<Node,Node>, Set<Integer>> req) {
//        for(Pair<Node,Node> n: req.keySet()) {
//            Set<Integer> weights = req.getOrDefault(n, new HashSet<>());
////            for (int w: weights) {
////                relax(n.getValue().getID(), w,n.getKey());
////            }
//            relax(n.getValue().getID(), Collections.min(weights), n.getKey());
//        }
//    }
    public HashMap<Integer, Integer> delta_stepping(Graph g) {
        //Splitting Adjacency list to heavy and light edges for each node
//        for(Node node: g.getVertexList()) {
//            int n = node.getID();
//            property_map.get(n).setWeight(Integer.MAX_VALUE);
//            for(Node dest: node.getAdjacent().keySet()) {
//                if(node.getAdjacent().get(dest)  > delta) {
//                    splitEdges(node, dest, heavy);
//                } else {
//                    splitEdges(node, dest, light);
//                }
//            }
//        }
//        Map<Pair<Node,Node>, Set<Integer>> requests = new ConcurrentHashMap<>();
        relax(property_map.get(source), 0, null);
//        int in = 0;
        List<Set<Node>> s = new ArrayList<>();
//        Set<Node> s = Collections.newSetFromMap(new ConcurrentHashMap<>());

        while (in < numBuckets) {
//            s.clear();
//            try {in = Collections.min(bucket.keySet());} catch (NoSuchElementException e){continue;}
            Set<Node> position = buckets[in];
            buckets[in] = new ConcurrentSkipListSet<Node>();
            while(!position.isEmpty()) {
                s.add(position);
                relaxLight(position);
                position = buckets[in];
                buckets[in] = new ConcurrentSkipListSet();
            }

            relaxHeavy(s);
            s.clear();
            updateBucketIn();


//            while (bucket.get(in) != null && !bucket.get(in).isEmpty()) {
////                System.out.println("In=" + in + " and this contains " + bucket.get(in));
////                System.out.println(bucket.get(in));
//                for (Node v : bucket.get(in)) {
//                    Set<Node> ladj = light.get(v);
//                    if (ladj != null)
//                        setupRequests(g, requests, v, ladj);
//                }
//                s.addAll(bucket.get(in));
//                bucket.get(in).clear();
//                relax_requests(requests);
//            }
//            for (Node v : s) {
//                Set<Node> hadj = heavy.get(v);
//                if (hadj != null)
//                    setupRequests(g, requests, v, hadj);
//            }
//            relax_requests(requests);
        }
        exec.shutdown();
        try {
            System.out.println("Waiting");
//            System.out.println("Buck Size is " + bucket.size());
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Terminated");
//            if(bucket.size() > 0) {
//                System.out.println(bucket);
//                System.out.println(s);
//                System.out.println(requests);
//                System.out.println("BUCKET STILL HAS WORK");
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<Integer, Integer> out = new HashMap<>();
        for(int i = 0; i < g.nodes().size(); i ++) {
            out.put(i, g.getVertexList().get(i).getWeight().get());
        }
        return out;
    }

    private void updateBucketIn() {
        int count  =0;
        while(in < numBuckets && buckets[in].isEmpty()) {
            in++;
            if(in == numBuckets && count < 1) {
                in = 0;
                count++;
            }
        }
    }

    private void relaxLight(Set<Node> vert) {
        allAdded = false;
        int count = vert.size();
        int tasks;
        if(count > nThreads) {
            tasks = count;
            Iterator<Node> it = vert.iterator();
            for(int i = 0; i < tasks; i++) {
                vertQueue.add(it.next());
            }
            for (int i =0; i < tasks - 1; i ++) {
                completionService.submit(lightRelax, null);
            }
            while(it.hasNext()) {
                vertQueue.add(it.next());
            }
            completionService.submit(lightRelax, null);
        } else {
            tasks = count;
            vertQueue.addAll(vert);
            for (int i = 0; i < tasks; i ++) {
                completionService.submit(lightRelax, null);
            }
        }
        allAdded = true;
        for(int i = 0; i < count; i ++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void relaxHeavy(List<Set<Node>> remainder) {
        allAdded = false;
        int count = remainder.stream().mapToInt(Set::size).sum();
        int tasks;
        if(count >= nThreads) {
            tasks = count;
            Iterator<Set<Node>> it = remainder.iterator();
            Iterator<Node> it2 = null;
            for(int i =0; i < count && it.hasNext(); i++) {
                it2 = it.next().iterator();
                while(it2.hasNext() && i < count) {
                    vertQueue.add(it2.next());
                    i++;
                }
            }
            for (int i =0; i < tasks - 1; i ++) {
                completionService.submit(heavyRelax, null);
            }
            while(it2.hasNext()) {
                vertQueue.add(it2.next());
            }
            while (it.hasNext()) {
                vertQueue.addAll(it.next());
            }
            completionService.submit(heavyRelax, null);
        } else {
            tasks = count;
            Iterator<Set<Node>> it = remainder.iterator();
            while (it.hasNext()) {
                vertQueue.addAll(it.next());
            }
            for (int i =0; i < tasks; i ++) {
                completionService.submit(heavyRelax, null);
            }
        }
        allAdded = true;
        for(int i = 0; i < count; i ++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /*
     * This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
     */
    private void relax(Node dest, int weight, Node prev) {
//        if(prev != null)
//            System.out.println(prev.getID() + " called relax on " + destination + " with weight " +  weight);
//        else System.out.println("Intial call to relax with "+ destination + " with weight " + weight);
//        GreaterThanCAS(w, x, prev);
        int newBucket = (weight/delta) % numBuckets;
//        Node dest = property_map.get(destination);
//        System.out.println(dest.getWeight().get());
        synchronized (dest) {
            if(weight < dest.getWeight().get()) {
                if(dest.getWeight().get() != Integer.MAX_VALUE) {
                    buckets[(dest.getWeight().get()) % numBuckets].remove(dest);
                }
                dest.setWeight(weight);
                buckets[newBucket].add(dest);
                dest.setPrev(prev);
            }
        }
    }

    private class LightRelaxRequests implements Runnable {

        private Queue<Node> vert;

        LightRelaxRequests(Queue<Node> vert) {
            this.vert = vert;
        }
        @Override
        public void run() {
            while(true) {
                Node s = vert.poll();
                if(s == null) {
                    if(allAdded && vert.isEmpty())
                        break;
                } else {
                    for(Node d: s.getAdjacent().keySet()) {
                        if(s.getAdjacent().get(d) <= delta) {
                            relax(d, s.getAdjacent().get(d) + s.getWeight().get(), s);
                        }
                    }
                }
            }
        }
    }

    private class HeavyRelaxRequests implements Runnable {
        private Queue<Node> vert;

        HeavyRelaxRequests(Queue<Node> vert) {
            this.vert = vert;
        }

        @Override
        public void run() {
            while(true) {
                Node s = vert.poll();
                if(s == null) {
                    if(allAdded && vert.isEmpty())
                        break;
                } else {
                    for(Node d: s.getAdjacent().keySet()) {
                        if(s.getAdjacent().get(d) > delta) {
                            relax(d, s.getAdjacent().get(d) + s.getWeight().get(), s);
                        }
                    }
                }
            }
        }
    }

}