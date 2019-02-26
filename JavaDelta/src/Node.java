import org.omg.PortableInterceptor.INACTIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Node implements Comparable<Node> {

    private AtomicInteger weight;
    private int id;
    private boolean visit;
    private HashMap<Node, Integer> adjacent;

    public Node(int id) {
        this.id = id;
        visit = false;
        adjacent = new HashMap<>();
        weight = new AtomicInteger(Integer.MAX_VALUE);
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
        this.weight.set(weight);
    }
    public boolean isVisited() {
        return visit;
    }
    public int getWeight() {
        return weight.get();
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
