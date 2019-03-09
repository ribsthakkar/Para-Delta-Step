import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class ODelta {
    /**
     * Error message for reporting the existence of an edge with negative weight.
     */
    private static final String NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED = "Negative edge weight not allowed";
    /**
     * Error message for reporting that delta must be positive.
     */
    private static final String DELTA_MUST_BE_NON_NEGATIVE = "Delta must be non-negative";
    /**
     * Default value for {@link #parallelism}.
     */
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    /**
     * Empirically computed amount of tasks per worker thread in
     * the {@link ForkJoinPool} that yields good performance.
     */
    private static final int TASKS_TO_THREADS_RATIO = 20;

    /**
     * The bucket width. A bucket with index $i$ therefore stores
     * a vertex v if and only if v is queued and tentative distance
     * to v $\in[i\cdot\Delta,(i+1)\cdot\Delta]$
     */
    private int delta;
    /**
     * Maximum number of threads used in the computations.
     */
    private int parallelism;

    /**
     * Number of buckets in the bucket structure.
     */
    private int numOfBuckets;
    /**
     */
    private int maxEdgeWeight;
    /**
     * Map to store predecessor for each vertex in the shortest path tree.
     */
    /**
     * Buckets structure.
     */
    private final Map<Integer, Set<Node>> bucketStructure = new ConcurrentHashMap<>();
    /**
     * Executor to which relax tasks will be submitted.
     */
    private ThreadPoolExecutor executor;
    /**
     * Decorator for {@link #executor} that enables to keep track of
     * when all submitted tasks are finished.
     */
    private ExecutorCompletionService<Void> completionService;
    /**
     * Queue of vertices which edges should be relaxed on current iteration.
     */
    private Queue<Node> verticesQueue;
    /**
     * Task for light edges relaxation.
     */
    private LightRelaxTask lightRelaxTask;
    /**
     * Task for light edges relaxation.
     */
    private HeavyRelaxTask heavyRelaxTask;
    /**
     * Indicates when all the vertices are been added to the
     * {@link #verticesQueue} on each iteration.
     */
    private volatile boolean allVerticesAdded;

    private Set<Node> incompleteNodes;

    private static CountDownLatch latch;

    private int firstNonEmptyBucket;
    private ReentrantLock l = new ReentrantLock();
    /**
     * Constructs a new instance of the algorithm for a given graph, delta, parallelism.
     * If delta is $0.0$ it will be computed during the algorithm execution. In general
     * if the value of $\frac{maximum edge weight}{maximum outdegree}$ is known beforehand,
     * it is preferable to specify it via this constructor, because processing the whole graph
     * to compute this value may significantly slow down the algorithm.
     *
     * @param graph       the graph
     * @param delta       bucket width
     * @param parallelism maximum number of threads used in the computations
     */
    private Graph graph;
    public ODelta(Graph graph, int delta, int parallelism) {
        if (delta < 0) {
            throw new IllegalArgumentException(DELTA_MUST_BE_NON_NEGATIVE);
        }
        this.delta = delta;
        this.parallelism = parallelism;
        executor =  new ThreadPoolExecutor (parallelism, parallelism, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
        completionService = new ExecutorCompletionService<>(executor);
        verticesQueue = new ConcurrentLinkedQueue<>();
        lightRelaxTask = new LightRelaxTask(verticesQueue);
        heavyRelaxTask = new HeavyRelaxTask(verticesQueue);
        this.graph = graph;
        maxEdgeWeight = getMaxEdgeWeight();
        incompleteNodes = Collections.newSetFromMap(new ConcurrentHashMap<>());
        firstNonEmptyBucket = 0;
    }

    /**
     *
     * @return max edge weight
     */
    private int getMaxEdgeWeight() {
        int maxEdge = 0;
        for(Node v: graph.getVertexList()) {
            for(Node w: v.getAdjacent().keySet()) {
                maxEdge = Integer.max(maxEdge, graph.getEdgeWeight(v.getID(), w.getID()));
            }
        }
//        System.out.println(maxEdge);
        return maxEdge;
    }


    public HashMap<Integer, Integer> computeShortestPaths(int src) {

        numOfBuckets = maxEdgeWeight / delta;
//        bucketStructure = new ConcurrentHashMap<Integer, Set<Node>>();
        for (int i = 0; i < numOfBuckets; i++) {
            bucketStructure.put(i, new ConcurrentSkipListSet());
        }
//        System.out.println(numOfBuckets);
        Node source = graph.getVertexList().get(src);
        relax(null, source, 0);

//        AtomicInteger firstNonEmptyBucket = new AtomicInteger(0);
//        ExecutorService e = Executors.newFixedThreadPool(1);
//        e.submit(() -> {
//            while (true) {
//                int i = 0;
//                    for (Set s : bucketStructure.values()) {
//                        if (!s.isEmpty()) {
//                            firstNonEmptyBucket.set(i);
////                        System.out.println(s);
//                            break;
//                        }
//                        i++;
//                }
//                if(i == numOfBuckets)
//                    firstNonEmptyBucket.set(-1);
//            }
//        });
        List<Set<Node>> removed = new ArrayList<>();
        while (firstNonEmptyBucket < numOfBuckets) {
            // the content of a bucket is replaced
            // in order not to handle the same vertices
            // multiple times
//            System.out.println(firstNonEmptyBucket);
            Set<Node> bucketElements = getContentAndReplace(firstNonEmptyBucket);
            while (!bucketElements.isEmpty()) {  // reinsertions may occur
                removed.add(bucketElements);
                findAndRelaxLightRequests(bucketElements);
                bucketElements = getContentAndReplace(firstNonEmptyBucket);
            }

            findAndRelaxHeavyRequests(removed);
            removed.clear();
//                while (executor.getQueue().size() > 0 && executor.getActiveCount() > 0) {
//                    System.out.println("here");
//                    System.out.println(executor.getQueue().size());
//                    System.out.println(executor.getActiveCount());
//                }
            updateNonEmptyIndex();
//            if(firstNonEmptyBucket == numOfBuckets && removed.size() > 0) {
//                firstNonEmptyBucket = 0;
//                System.out.println("here");
//                System.out.println(incompleteNodes);
////                for(Set s: bucketStructure) {
////                    System.out.println(s);
////                }
//            }
        }
        shutDownExecutor(executor);
        for(Set s: bucketStructure.values()) {
            System.out.println(s);
        }
        HashMap<Integer, Integer> out = new HashMap<>();
        for(int i = 0; i < graph.nodes().size(); i ++) {
            out.put(i, graph.getVertexList().get(i).getWeight().get());
        }
        return out;
    }

    private void updateNonEmptyIndex() {
        int timesChecked = 0;
        while (firstNonEmptyBucket < numOfBuckets
                && bucketStructure.get(firstNonEmptyBucket).isEmpty()) { // skip empty buckets
            ++firstNonEmptyBucket;
            if (firstNonEmptyBucket == numOfBuckets && timesChecked < 0) {
                firstNonEmptyBucket = 0;
                timesChecked++;
            }
        }
        //        System.out.println(firstNonEmptyBucket);
//        System.out.println(bucketStructure.get(firstNonEmptyBucket));
    }

    /**
     * Shuts down the {@link #executor}.
     */
    private void shutDownExecutor(ExecutorService f) {
        System.out.println("Shuttingdown");
        f.shutdown();
        executor.shutdown();
        try { // wait till the executor is shut down
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages edge relaxations. Adds all elements from
     * {@code vertices} to the {@link #verticesQueue}
     * and submits as many {@link #lightRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param vertices vertices
     */
    private void findAndRelaxLightRequests(Set<Node> vertices) {
        l.lock();
        allVerticesAdded = false;
        int numOfVertices = vertices.size();
        int numOfTasks;
//        lightRelaxTask.updateLatch(numOfVertices);
        if (numOfVertices >= parallelism) {
            // use as available tasks
            numOfTasks = parallelism;
            Iterator<Node> iterator = vertices.iterator();
            // provide some work to the workers
            addSetVertices(iterator, parallelism);
            submitTasks(lightRelaxTask, parallelism - 1); // one thread should
            // submit rest of vertices
            addSetRemaining(iterator);
            submitTasks(lightRelaxTask, 1); // use remaining thread for relaxation
        } else {
            // only several relaxation tasks are needed
            numOfTasks = numOfVertices;
            addSetRemaining(vertices.iterator());
            submitTasks(lightRelaxTask, numOfVertices);
        }

        allVerticesAdded = true;
        waitForTasksCompletion(numOfTasks);
        l.unlock();
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Manages execution of edges relaxation. Adds all
     * elements from {@code vertices} to the {@link #verticesQueue}
     * and submits as many {@link #heavyRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param verticesSets set of sets of vertices
     */
    private void findAndRelaxHeavyRequests(List<Set<Node>> verticesSets) {
        l.lock();
        allVerticesAdded = false;
        int numOfVertices = verticesSets.stream().mapToInt(Set::size).sum();
//        heavyRelaxTask.updateLatch(numOfVertices);
        int numOfTasks;
        if (numOfVertices >= parallelism) {
            // use as available tasks
            numOfTasks = parallelism;
            Iterator<Set<Node>> setIterator = verticesSets.iterator();
            // provide some work to the workers
            Iterator<Node> iterator = addSetsVertices(setIterator, parallelism);
            submitTasks(heavyRelaxTask, parallelism - 1);// one thread should
            // submit rest of vertices
            addSetRemaining(iterator);
            addSetsRemaining(setIterator);
            submitTasks(heavyRelaxTask, 1); // use remaining thread for relaxation
        } else {
            // only several relaxation tasks are needed
            numOfTasks = numOfVertices;
            addSetsRemaining(verticesSets.iterator());
            submitTasks(heavyRelaxTask, numOfVertices);
        }

        allVerticesAdded = true;
        waitForTasksCompletion(numOfTasks);
        l.unlock();
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue}
     * provided by the {@code iterator}.
     *
     * @param iterator      vertices iterator
     * @param numOfVertices vertices amount
     */
    private void addSetVertices(Iterator<Node> iterator, int numOfVertices) {
        for (int i = 0; i < numOfVertices && iterator.hasNext(); i++) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds all remaining vertices to the {@link #verticesQueue}
     * provided by the {@code iterator}.
     *
     * @param iterator vertices iterator
     */
    private void addSetRemaining(Iterator<Node> iterator) {
        while (iterator.hasNext()) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue}
     * that are contained in the sets provided by the {@code setIterator}.
     * Returns iterator of the set which vertex was added last.
     *
     * @param setIterator   sets of vertices iterator
     * @param numOfVertices vertices amount
     * @return iterator of the last set
     */
    private Iterator<Node> addSetsVertices(Iterator<Set<Node>> setIterator, int numOfVertices) {
        int i = 0;
        Iterator<Node> iterator = null;
        while (setIterator.hasNext() && i < numOfVertices) {
            iterator = setIterator.next().iterator();
            while (iterator.hasNext() && i < numOfVertices) {
                verticesQueue.add(iterator.next());
                i++;
            }
        }
        return iterator;
    }

    /**
     * Adds all remaining vertices to the {@link #verticesQueue}
     * that are contained in the sets provided by the {@code setIterator}.
     *
     * @param setIterator sets of vertices iterator
     */
    private void addSetsRemaining(Iterator<Set<Node>> setIterator) {
        while (setIterator.hasNext()) {
            verticesQueue.addAll(setIterator.next());
        }
    }


    /**
     * Submits the {@code task} {@code numOfTasks} times to the {@link #completionService}.
     *
     * @param task       task to be submitted
     * @param numOfTasks amount of times task should be submitted
     */
    private void submitTasks(Runnable task, int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            completionService.submit(task, null);
        }
    }

    /**
     * Takes {@code numOfTasks} tasks from the {@link #completionService}.
     *
     * @param numOfTasks amount of tasks
     */
    private void waitForTasksCompletion(int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void relax(Node v, Node w, int distance) {
//        System.out.println("Called relax " + v + " with w " + w + " with distance " + distance);
        int updatedBucket = bucketIndex(distance);
        synchronized (w) { // to make relaxation updates thread-safe
            int oldData = w.getWeight().get();
            if (distance < oldData) {
                if (!(oldData == Integer.MAX_VALUE)) {
                    bucketStructure.get(bucketIndex(oldData)).remove(w);
                }
                bucketStructure.putIfAbsent(updatedBucket, new ConcurrentSkipListSet<>());
                bucketStructure.get(updatedBucket).add(w);
                w.setWeight(distance);
                w.setPrev(v);
                incompleteNodes.add(w);
            }
        }
    }

    /**
     * Calculates bucket index for a given {@code distance}.
     *
     * @param distance distance
     * @return bucket index
     */
    private int bucketIndex(int distance) {
//        System.out.println(distance);
//        System.out.println(delta);
//        System.out.println(numOfBuckets);
//        if(distance/delta < firstNonEmptyBucket)
//                firstNonEmptyBucket = distance/delta;
        return (distance / delta) % numOfBuckets;
    }

    /**
     * Replaces the bucket at the {@code bucketIndex} index with a new instance of the {@link ConcurrentSkipListSet}.
     * Return the reference to the set that was previously in the bucket.
     *
     * @param bucketIndex bucket index
     * @return content of the bucket
     */
    private Set getContentAndReplace(int bucketIndex) {
            Set<Node> result = bucketStructure.get(bucketIndex);
            incompleteNodes.removeAll(result);
            bucketStructure.put(bucketIndex, new ConcurrentSkipListSet<Node>());
            return result;
    }

    /**
     * Task that is submitted to the {@link #completionService}
     * during shortest path computation for light relax requests relaxation.
     */
    class LightRelaxTask implements Runnable {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<Node> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        LightRelaxTask(Queue<Node> vertices) {
            this.vertices = vertices;
        }
        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run() {

            while (true) {
                Node v = vertices.poll();
                if (v == null) { // we might have a termination situation
                    if (allVerticesAdded && vertices.isEmpty()) { // need to check
                        // is the queue is empty, because some vertices might have been added
                        // while passing from first if condition to the second
                        break;
                    }
                } else {
                    for (Node w : graph.getAdjacentVertices(v.getID()).keySet()) {
                        if (graph.getEdgeWeight(v.getID(), w.getID()) <= delta) {
                            relax(v, w, v.getWeight().get() + graph.getEdgeWeight(v.getID(), w.getID()));
                        }
                    }
                }
            }
//            latch.countDown();
//            System.out.println("Updated latch at " + latch);
        }
    }

    /**
     * Task that is submitted to the {@link #completionService}
     * during shortest path computation for heavy relax requests relaxation.
     */
    class HeavyRelaxTask implements Runnable {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<Node> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        HeavyRelaxTask(Queue<Node> vertices) {
            this.vertices = vertices;
        }
        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run() {

            while (true) {
                Node v = vertices.poll();
                if (v == null) { // we might have a termination situation
                    if (allVerticesAdded && vertices.isEmpty()) { // need to check
                        // is the queue is empty, because some vertices might have been added
                        // while passing from first if condition to the second
                        break;
                    }
                } else {
                    for (Node w : graph.getAdjacentVertices(v.getID()).keySet()) {
                        if (graph.getEdgeWeight(v.getID(), w.getID()) > delta) {
                            relax(v, w, v.getWeight().get() + graph.getEdgeWeight(v.getID(), w.getID()));
                        }
                    }
                }
            }
//            latch.countDown();
//            System.out.println("Updated latch at " + latch);
        }
    }
}