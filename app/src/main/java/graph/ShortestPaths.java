/** Author: Brandon Connely
 * Date: 3/12/26
 * Purpose: A4
 */
package graph;

import heap.Heap;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;

/** Provides an implementation of Dijkstra's single-source shortest paths
 * algorithm.
 * Sample usage:
 *   Graph g = // create your graph
 *   ShortestPaths sp = new ShortestPaths();
 *   Node a = g.getNode("A");
 *   sp.compute(a);
 *   Node b = g.getNode("B");
 *   LinkedList<Node> abPath = sp.getShortestPath(b);
 *   double abPathLength = sp.getShortestPathLength(b);
 *   */
public class ShortestPaths {
    // stores auxiliary data associated with each node for the shortest
    // paths computation:
    private HashMap<Node,PathData> paths;

    /** Compute the shortest path to all nodes from origin using Dijkstra's
     * algorithm. Fill in the paths field, which associates each Node with its
     * PathData record, storing total distance from the source, and the
     * backpointer to the previous node on the shortest path.
     * Precondition: origin is a node in the Graph.*/
    public void compute(Node origin) {
        // F = frontier, f = current frontier node, w = neighbor, d = distance, bp = backpointer.
        paths = new HashMap<Node,PathData>(); // S = { }; (implicit per design decision bullet point 3)
        Heap<Node,Double> F = new Heap<Node,Double>();
        paths.put(origin, new PathData(0.0, null)); // v.d = 0; v.bp = null;
        F.add(origin, 0.0); // F = {v};

        while (F.size() > 0) { // while (F != {})
            Node f = F.poll(); // f = node in F with min d value;
            PathData fData = paths.get(f);

            for (Map.Entry<Node,Double> edge : f.getNeighbors().entrySet()) { // for each neighbor w of f 
                Node w = edge.getKey();
                double newDistance = fData.d + edge.getValue();

                if (!paths.containsKey(w)) { // if (w not in S or F) 
                    paths.put(w, new PathData(newDistance, f)); // w.d = f.d + weight(f, w); w.bp = f;
                    F.add(w, newDistance); // add w to F;
                } 

                else if (F.contains(w) && newDistance < paths.get(w).d) { // else if (f.d + weight(f,w) < w.d) 
                    paths.get(w).d = newDistance; // w.d = f.d + weight(f,w);
                    paths.get(w).bp = f; // w.bp = f;
                    F.changePriority(w, newDistance);
                }
            }
        }
    }

    /** Returns the length of the shortest path from the origin to destination.
     * If no path exists, return Double.POSITIVE_INFINITY.
     * Precondition: destination is a node in the graph, and compute(origin)
     * has been called. */
    public double shortestPathLength(Node destination) {
        if (!paths.containsKey(destination)) {
            return Double.POSITIVE_INFINITY;
        }
        return paths.get(destination).d;
    }

    /** Returns a LinkedList of the nodes along the shortest path from origin
     * to destination. This path includes the origin and destination. If origin
     * and destination are the same node, it is included only once.
     * If no path to it exists, return null.
     * Precondition: destination is a node in the graph, and compute(origin)
     * has been called. */
    public LinkedList<Node> shortestPath(Node destination) {
        if (!paths.containsKey(destination)) {
            return null;
        }

        LinkedList<Node> result = new LinkedList<Node>();
        Node current = destination;

        while (current != null) {
            result.addFirst(current);
            current = paths.get(current).bp;
        }

        return result;
    }


    /** Inner class representing data used by Dijkstra's algorithm in the
     * process of computing shortest paths from a given source node. */
    class PathData {
        double d; // distance of the shortest path from source
        Node bp; // previous node in the path from the source

        /** constructor: initialize distance and previous node */
        public PathData(double dist, Node prev) {
            d = dist;
            bp = prev;
        }
    }


    /** Static helper method to open and parse a file containing graph
     * information. Can parse either a basic file or a DB1B CSV file with
     * flight data. See GraphParser, BasicParser, and DB1BParser for more.*/
    protected static Graph parseGraph(String fileType, String fileName) throws
        FileNotFoundException {
        // create an appropriate parser for the given file type
        GraphParser parser;
        if (fileType.equals("basic")) {
            parser = new BasicParser();
        } else if (fileType.equals("db1b")) {
            parser = new DB1BParser();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + fileType);
        }

        // open the given file
        parser.open(new File(fileName));

        // parse the file and return the graph
        return parser.parse();
    }

    public static void main(String[] args) {
      // read command line args
      String fileType = args[0];
      String fileName = args[1];
      String origCode = args[2];

      String destCode = null;
      if (args.length == 4) {
          destCode = args[3];
      }

      // parse a graph with the given type and filename
      Graph graph;
      try {
          graph = parseGraph(fileType, fileName);
      } catch (FileNotFoundException e) {
          System.out.println("Could not open file " + fileName);
          return;
      }
      graph.report();


      // Create a ShortestPaths object, use it to compute shortest paths data from the origin node given by origCode.
      ShortestPaths sp = new ShortestPaths();
      Node origin = graph.getNode(origCode);
      sp.compute(origin);

      // If destCode was not given, print each reachable node followed by the length of the shortest path to it from the origin.
      if (destCode == null) {
          System.out.println();
          System.out.println("Shortest paths from " + origCode + ":");
          for (Node node : graph.getNodes().values()) {
              if (!node.equals(origin) && sp.shortestPathLength(node) != Double.POSITIVE_INFINITY) {
                  System.out.println(node + ": " + sp.shortestPathLength(node));
              }
          }
          System.out.println(origin + ": " + sp.shortestPathLength(origin));
      }

      // If destCode was given, print the nodes in the path from origCode to destCode, followed by the total path length.  If no path exists, print a message saying so.
      else {
          System.out.println();
          Node destination = graph.getNode(destCode);
          LinkedList<Node> path = sp.shortestPath(destination);

          if (path == null) {
              System.out.println("No path from " + origCode + " to " + destCode);
          } 
          else {
              for (Node node : path) {
                  System.out.print(node + " ");
              }
              System.out.println(sp.shortestPathLength(destination));
          }
      }
    }
}
