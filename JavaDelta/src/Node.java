import org.omg.PortableInterceptor.INACTIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Node implements Comparable<Node> {

    private AtomicInteger weight;
    private int id;
    private boolean visit;
    private HashMap<Node, Integer> adjacent;
    private ReentrantLock lock;

    public Node(int id) {
        this.id = id;
        visit = false;
        adjacent = new HashMap<>();
        weight = new AtomicInteger(Integer.MAX_VALUE);
        lock = new ReentrantLock();
    }

    public void addEdge(Node other, int weight) {
        if(!adjacent.containsKey(other)) {
            adjacent.put(other, weight);
        } else if(adjacent.get(other) < weight) {
            adjacent.put(other, weight);
        }
    }
    public void mark() {
        visit = true;
    }
    public void unmark() {
        visit = false;
    }

    public HashMap<Node, Integer> getAdjacent() {
        return adjacent;
    }
    public void setWeight(int weight) {
//        System.out.println("HERE");
//        System.out.println(this.weight.get());
        this.weight.set(weight);
    }
    public boolean isVisited() {
        return visit;
    }
    public AtomicInteger getWeight() {
        return weight;
    }
    public int getID() {
        return id;
    }
    @Override
    public int compareTo(Node o) {
        return this.weight.get() - o.weight.get();
    }

    @Override
    public String toString() {
        return "" + id;
    }

}
