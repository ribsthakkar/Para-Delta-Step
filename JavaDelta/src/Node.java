import java.util.*;
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
    public synchronized void setPrev(Node prev) {
//        System.out.println("I am " + this);
//        System.out.println("Original prev is " + this.prev.get());
//        System.out.println("New prev is " + prev);
//        lock.lock();
        this.prev.set(prev);
//        lock.unlock();

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
        return Integer.compare(this.weight.get(), o.weight.get());
    }

//    public int compare(Node node1, Node node2) {
//        if (node1.weight.get() < node2.weight.get())
//            return -1;
//        if (node1.weight.get() > node2.weight.get())
//            return 1;
//        return 0;
//    }
    public boolean equals(Object o) {
        return ((Node)o).getID() == this.getID();
    }
    @Override
    public String toString() {
        return "ID:" + id + " Weight:" + weight ;
//        return "" + id;
    }

}
