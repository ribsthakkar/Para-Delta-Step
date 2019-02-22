import java.util.ArrayList;

public class Node implements Comparable<Node> {

    private int weight;
    private int id;
    private boolean visit;

    @Override
    public int compareTo(Node o) {
        return this.weight - o.weight;
    }
}
