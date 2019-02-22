"""
This code is an implementation of Prateek Srivastava's sequential delta stepping algorithm with a personal Graph class implementation.
The goal is to make it parallelized. Prateek's impelementation is sequential.
Prateek's code can be found at: https://github.com/prateek22sri/Graph-Delta-Stepping-SSSP/blob/master/deltaStepping.py

"""

from math import floor, sqrt
import Graph as gr
import time
from pprint import pprint
from threading import Thread
from threading import active_count
from threading import RLock
from threading import current_thread
import multiprocessing
from multiprocessing.managers import SyncManager
from time import sleep


class Algorithm:
    """
    """

    def __init__(self):
        """
        """
        self.distances = {}
        self.delta = 5
        self.property_map = {}
        self.workItems = []
        self.source_vertex = 2
        self.infinity = float("inf")
        self.totalNodes = 0
        self.totalEdges = 0
        self.B = {}
        self.lock = RLock()
        self.pool = set()

    def relax(self, w, x):
        """
        This function relaxes a bucket i.e. if the distance of a vertex is less than the already existing distance in
        the property map then, the vertex is removed from the bucket and reinserted in the new bucket
        x is the distance of the vertex and w is the index of the vertex in the property map
        """
        # print("w=", w, "x=", x)
        # self.lock.acquire()
        # while True:
        # print(current_thread(), active_count())
        #     break
        # sleep(2)
        if x < self.property_map[w]:
            # check if there is an entry of w in the dictionary B
            if self.property_map[w] != self.infinity:
                if w in self.B[floor(self.property_map[w] / self.delta)]:
                    # check if the vertex is in the wrong bucket
                    if floor(x / self.delta) != floor(self.property_map[w] / self.delta):
                        self.B[floor(self.property_map[w] / self.delta)].remove(w)
                if floor(x / self.delta) not in self.B:
                    self.B[floor(x / self.delta)] = {w}
                else:
                    if w not in self.B[floor(x / self.delta)]:
                        self.B[floor(x / self.delta)].add(w)
            # if the dictionary entry does not exist
            else:
                if floor(x / self.delta) not in self.B:
                    self.B[floor(x / self.delta)] = {w}
                else:
                    if w not in self.B[floor(x / self.delta)]:
                        self.B[floor(x / self.delta)].add(w)

            # update the property map
            self.property_map[w] = x
        # self.lock.release()

    def find_requests(self, vertices, kind, g):
        """
        returns a dictionary of neighboring edges with their weights but according to the kind i.e. light or heavy
        :param vertices:
        :param kind:
        :param g:
        :return:
        """

        tmp = {}
        # print("vertices=", vertices, "kind=", kind)
        for u in vertices:
            for v in g.neighbors(u):
                # print(u, self.property_map[u], g.get_edge_data(u, v)['weight'])
                edge_weight = self.property_map[u] + g.get_edge_data(u, v)['weight']
                if kind == 'light':
                    if g.get_edge_data(u, v)['weight'] <= self.delta:
                        if v in tmp:
                            if edge_weight < tmp[v]:
                                tmp[v] = edge_weight
                        else:
                            tmp[v] = edge_weight
                elif kind == 'heavy':
                    if g.get_edge_data(u, v)['weight'] > self.delta:
                        if v in tmp:
                            if edge_weight < tmp[v]:
                                tmp[v] = edge_weight
                        else:
                            tmp[v] = edge_weight
                else:
                    return "Error: No such kind of edges " + kind
        # print("tmp=", tmp)
        return tmp

    def relax_requests(self, request):
        """
        :param request:
        :return:
        """
        # pool = set()
        for key, value in request.items():
            t = Thread(target=self.relax, args=[key, value])
            t.start()
            self.pool.add(t)
            # self.relax(key, value)
        # print(len(request))
        # for thr in pool:
        #     thr.start()
        # for thr in pool:
        #     thr.join()
    def delta_stepping(self, g):
        """
        This is the main function to implement the algorithm
        :param g:
        :return:
        """
        for node in g.nodes():
            self.property_map[node] = self.infinity
        # print(self.B, self.property_map)
        self.relax(self.source_vertex, 0)
        # print(self.B, self.property_map)
        ctr = 0
        while self.B:
            # print("Parent Iteration=", ctr)
            # print("bucket=", self.B)
            i = min(self.B.keys())
            sub_ctr = 0
            r = []

            while i in self.B:
                # print("Child Iteration=", sub_ctr)
                # print("B[i]=", self.B[i])
                req = self.find_requests(self.B[i], 'light', g)
                # print("req=", req)
                r += self.B[i]
                del self.B[i]
                self.relax_requests(req)
                sub_ctr += 1
                # print(self.B)
            # print("child ends")
            # print("r=", r)
            req = self.find_requests(r, 'heavy', g)
            self.relax_requests(req)
            ctr += 1
        print(active_count())
        for thr in self.pool:
            thr.join()

    def validate(self, g):
        """
        :param g:
        :return:
        """
        self.property_map = {k: v for k, v in self.property_map.items() if v != self.infinity}
        print("Dijkstra Time")
        print(time.time())
        d = g.dijkstra(self.source_vertex)
        print(time.time())
        p = {k: v for k, v in d.items() if v != self.infinity}
        if p == self.property_map:
            return True
        else:
            print("Error: The algorithm is faulty!!!")
            for i in range(1, len(p)):
                if p[i] != self.property_map.get(i, None):
                    print("vertex ", i, " value in ground truth is ", p[i], " and value in delta stepping is ",
                          self.property_map.get(i, None))
            return False


def main():
    # make_graph = False
    # g = nx.read_edgelist('sample2', nodetype=int, data=(('weight', int),), create_using=nx.DiGraph())

    # print("\nGraph Information..")
    # print("===================\n")
    # print(nx.info(g))
    # print("\nCalculating shortest path..")
    details = list(input().split())
    num_nodes = int(details[2])
    num_edges = int(details[3])
    g = gr.Graph(num_nodes)
    for edge in range(num_edges):
        e = list(input().split())
        g.add_edge(int(e[1]), int(e[2]), int(e[3]))
    a = Algorithm()
    a.source_vertex = int(details[4])
    print("Delta Stepping Time")
    print(time.time())
    a.delta_stepping(g)
    print(time.time())
    print("\nValidating Solution..")
    if not a.validate(g):
        exit(1)
    else:
        print('Implementation correct')
        # print("\nThe shortest path from ", a.source_vertex, " is:")
        # pprint(a.property_map)

    # # visualize the graph
    # if make_graph:
    #     pos = nx.spring_layout(g, k=5 / sqrt(g.order()))
    #     nx.draw_networkx(g, pos)
    #     edge_labels = dict([((u, v,), d['weight'])
    #                         for u, v, d in g.edges(data=True)])
    #     nx.draw_networkx_edge_labels(g, pos=pos, edge_labels=edge_labels, label_pos=0.3, font_size=7)
    #     plt.show(block=False)
    #     plt.savefig("sample1_graph.png")


if __name__ == '__main__':
    main()