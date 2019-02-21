This graph input follows DIMACS format

Sample Input:

p sp 5 6 1
a 1 2 2
a 2 5 5
a 2 3 4
a 1 4 1
a 4 3 3
a 3 5 1

First Line has 3 numbers after the intial letters: n e s
n = number of nodes
e = number of edges
s = source for algorithm

the following e lines have the format after the letter 'a': u v w
u = start
v = dest
w = weight of edge

To run, use python3 and run deltaStepping.py:

python3 deltaStepping.py
