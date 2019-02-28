import heapq


class Graph():
    def __init__(self, nodes):
        self.num = nodes
        self._nodes = list(Node(x) for x in range(self.num + 1))
        self._edges = dict()

    def add_edge(self, start, end, weight):
        self._nodes[start].add_edge(self._nodes[end], weight)
        if (start, end) in self._edges:
            d = self._edges[(start, end)]
            if d['weight'] < weight:
                d['weight'] = weight
        else:
            self._edges[(start, end)] = {'weight': weight}


    def get_edge_data(self, u, v):
        return self._edges[(u,v)]

    def unmark_all(self):
        for node in self._nodes:
            node.unmark()

    def nodes(self):
        return list(node.id for node in self._nodes)

    def neighbors(self, vert):
        return list(node .id for node in self._nodes[vert].adjacent)

    def dijkstra(self, start):
        q = []
        q.append(self._nodes[start])
        self._nodes[start].weight = 0
        heapq.heapify(q)
        while len(q):
            current = heapq.heappop(q)
            if current.visit:
                continue
            current.mark()
            for adj, edge_weight in current.adjacent.items():
                if not adj.visit:
                    if adj.weight > current.weight + edge_weight:
                        adj.weight = current.weight + edge_weight
                        adj.prev = current
                    heapq.heappush(q, adj)
        self.unmark_all()
        d = dict()
        for i in range(1, len(self._nodes)):
            d[i] = self._nodes[i].weight
        return d

    def pathfromHere(self, current):
        current = self._nodes[current]
        path = [current]
        while current.prev:
            path.append(current.prev)
            current = current.prev
        return reversed(path)


class Node():
    def __init__(self,x):
        self.id = x
        self.visit = False
        self.adjacent = {}
        self.weight = float('inf')
        self.prev = None

    # Updating adjacent edge if we have a more minimum cost one
    def add_edge(self, other, weight):
        if other not in self.adjacent:
            self.adjacent[other] = weight
        elif self.adjacent[other] > weight:
            self.adjacent[other] = weight

    # Node comparator functions for HeapQ
    def __lt__(self, other):
        return self.weight < other.weight

    def __gt__(self, other):
        return self.weight > other.weight

    # def __eq__(self, other):
    #     return self.weight == other.weight

    def __le__(self, other):
        return self.weight <= other.weight

    def __ge__(self, other):
        return self.weight >= other.weight

    def __ne__(self, other):
        return self.weight != other.weight

    def __hash__(self):
        return self.id

    def mark(self):
        self.visit = True

    def unmark(self):
        self.visit = False

    def __str__(self):
        return str(self.id)