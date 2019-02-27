import org.omg.PortableInterceptor.INACTIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Node implements Comparable<Node> {

    private AtomicInteger weight;
    private int id;
    private boolean visit;
    private HashMap<Node, Integer> adjacent;
    private ReentrantLock lock;
    private AtomicReference<Node> prev;

    public Node(int id) {
        this.id = id;
        visit = false;
        adjacent = new HashMap<>();
        weight = new AtomicInteger(Integer.MAX_VALUE);
        lock = new ReentrantLock();
        prev = new AtomicReference<>(null);
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
    public void setPrev(Node prev) {
//        System.out.println("I am " + this);
//        System.out.println("Original prev is " + this.prev.get());
//        System.out.println("New prev is " + prev);
        this.prev.set(prev);

    }

    public AtomicReference<Node> getPrev() {
        return prev;
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
        if(this.weight.get() < o.weight.get()) {
            return -1;
        } else if(this.weight.get() > o.weight.get()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "" + id;
    }

}
