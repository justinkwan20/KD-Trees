import edu.princeton.cs.algs4.*;

/*************************************************************************
 *  Compilation:  javac NearestNeighborVisualizer.java
 *  Execution:    java NearestNeighborVisualizer <input> <k>
 *  Dependencies: PointST.java KdTreeST.java Point2D.java In.java StdDraw.java
 *
 *  Read points from a file (specified as the first command-line argument) and
 *  draw to standard draw. Highlight the k (specified as the second 
 *  command-line argument) points closest to the mouse.
 *
 *  The nearest neighbors according to the brute-force algorithm are drawn
 *  in red; the nearest neighbors using the kd-tree algorithm are drawn in blue.
 *
 *************************************************************************/

public class NearestNeighborVisualizer {

    public static void main(String[] args) {
        String filename = args[0];
        int k = Integer.parseInt(args[1]);
        In in = new In(filename);

        StdDraw.show(0);

        // initialize the two data structures with point from standard input
        BrutePointST<Integer> brute = new BrutePointST<Integer>();
        KdTreePointST<Integer> kdtree = new KdTreePointST<Integer>();
        for (int i = 0; !in.isEmpty(); i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdtree.put(p, i);
            brute.put(p, i);
        }

        while (true) {

            // the location (x, y) of the mouse
            double x = StdDraw.mouseX();
            double y = StdDraw.mouseY();
            Point2D query = new Point2D(x, y);

            // draw all of the points
            StdDraw.clear();
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(.01);
            for (Point2D p : brute.points())
                p.draw();

            // draw in red the k nearest neighbors according to the brute-force algorithm
            StdDraw.setPenRadius(.03);
            StdDraw.setPenColor(StdDraw.RED);
            if (k == 1) {
                Point2D p = brute.nearest(query);
                p.draw();
            }
            else 
                for (Point2D p : brute.nearest(query, k))
                    p.draw();
            StdDraw.setPenRadius(.02);

            // draw in blue the k nearest neighbors according to the kd-tree algorithm
            StdDraw.setPenColor(StdDraw.BLUE);
            if (k == 1) {
                Point2D p = kdtree.nearest(query);
                p.draw();
            }
            else 
                for (Point2D p : kdtree.nearest(query, k))
                    p.draw();
            StdDraw.show(0);
            StdDraw.show(40);
        }
    }
}
