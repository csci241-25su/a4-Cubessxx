/** Author: Brandon Connely
 * Date: 4/1/26
 * Purpose: A4 
 */
package graph;

import static org.junit.Assert.*;
import org.junit.FixMethodOrder;

import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.URL;
import java.io.FileNotFoundException;

import java.util.LinkedList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShortestPathsTest {

    /* Performs the necessary gradle-related incantation to get the
       filename of a graph text file in the src/test/resources directory at
       test time.*/
    private String getGraphResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return resource.getPath();
    }

    /* Returns the Graph loaded from the file with filename fn located in
     * src/test/resources at test time. */
    private Graph loadBasicGraph(String fn) {
        Graph result = null;
        String filePath = getGraphResource(fn);
        filePath = filePath.replace("%20", " "); // Fix bug for file paths involving spaces(C:/Brandon Connely) causing failing gradle tests
        try {
          result = ShortestPaths.parseGraph("basic", filePath);
        } catch (FileNotFoundException e) {
          fail("Could not find graph " + fn);
        }
        return result;
    }

    /** Origin node should have length 0 path to itself. */
    @Test
    public void test00() {
        Graph g = new Graph();
        Node a = g.getNode("A");
        ShortestPaths sp = new ShortestPaths();

        sp.compute(a);

        LinkedList<Node> path = sp.shortestPath(a);
        assertEquals(path.size(), 1);
        assertEquals(path.getFirst(), a);
        assertEquals(sp.shortestPathLength(a), 0.0, 1e-6);
    }

    /** Minimal test case to check the path from A to B in Simple0.txt */
    @Test
    public void test01() {
        Graph g = loadBasicGraph("Simple0.txt");
        ShortestPaths sp = new ShortestPaths();
        Node a = g.getNode("A");

        sp.compute(a);

        Node b = g.getNode("B");
        Node c = g.getNode("C");

        LinkedList<Node> abPath = sp.shortestPath(b);
        assertEquals(abPath.size(), 2);
        assertEquals(abPath.getFirst(), a);
        assertEquals(abPath.getLast(), b);
        assertEquals(sp.shortestPathLength(b), 1.0, 1e-6);

        LinkedList<Node> acPath = sp.shortestPath(c);
        assertEquals(acPath.size(), 2);
        assertEquals(acPath.getFirst(), a);
        assertEquals(acPath.getLast(), c);
        assertEquals(sp.shortestPathLength(c), 2.0, 1e-6);
    }

    /** Unreachable nodes should return null path and have infinite length. */
    @Test
    public void test02() {
        Graph g = new Graph();
        Node a = g.getNode("A");
        Node b = g.getNode("B");
        Node c = g.getNode("C");
        g.addEdge(a, b, 4);

        ShortestPaths sp = new ShortestPaths();
        sp.compute(a);

        LinkedList<Node> abPath = sp.shortestPath(b);
        assertEquals(abPath.size(), 2);
        assertEquals(abPath.getFirst(), a);
        assertEquals(abPath.getLast(), b);
        assertEquals(sp.shortestPathLength(b), 4.0, 1e-6);

        assertNull(sp.shortestPath(c));
        assertTrue(Double.isInfinite(sp.shortestPathLength(c)));
    }

    /** compute should replace old path data when called again with a new origin. */
    @Test
    public void test03() {
        Graph g = new Graph();
        Node a = g.getNode("A");
        Node b = g.getNode("B");
        Node c = g.getNode("C");
        g.addEdge(a, b, 1);
        g.addEdge(b, c, 2);

        ShortestPaths sp = new ShortestPaths();
        sp.compute(a);

        LinkedList<Node> fromAPath = sp.shortestPath(c);
        assertEquals(fromAPath.size(), 3);
        assertEquals(fromAPath.get(0), a);
        assertEquals(fromAPath.get(1), b);
        assertEquals(fromAPath.get(2), c);
        assertEquals(sp.shortestPathLength(c), 3.0, 1e-6);

        sp.compute(b);

        assertNull(sp.shortestPath(a));
        assertTrue(Double.isInfinite(sp.shortestPathLength(a)));

        LinkedList<Node> fromBPath = sp.shortestPath(c);
        assertEquals(fromBPath.size(), 2);
        assertEquals(fromBPath.getFirst(), b);
        assertEquals(fromBPath.getLast(), c);
        assertEquals(sp.shortestPathLength(c), 2.0, 1e-6);
    }

    /* Pro tip: unless you include @Test on the line above your method header,
     * gradle test will not run it! This gets me every time. */

}

       
