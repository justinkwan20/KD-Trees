import edu.princeton.cs.algs4.MaxPQ;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class KdTreePointST<Value> implements PointST<Value> {
    private Node root;
    private int N;
    
    // 2d-tree (generalization of a BST in 2d) representation.
    private class Node {
        private Point2D point;   // the point
        private Value value;   // the symbol table maps the point to this value
        private RectHV rect; // the axis-aligned rectangle corresponding to 
                             // this node
        private Node lb;     // the left/bottom subtree
        private Node rt;     // the right/top subtree

        // Construct a node given the point, the associated value, and the 
        // axis-aligned rectangle corresponding to the node.
        private Node(Point2D p, Value val, RectHV rect) {
            this.point = p;
            this.value = val;
            this.rect = rect;
        }
    }

    // Construct an empty symbol table of points.
    public KdTreePointST() {
        this.root = null;
        this.N = 0;
    }

    // Is the symbol table empty?
    public boolean isEmpty() { 
        return size() == 0;
    }

    // Number of points in the symbol table.
    public int size() {
        return N;
    }

    // Associate the value val with point p.
    public void put(Point2D p, Value val) {
        RectHV rectangle = new RectHV(Double.NEGATIVE_INFINITY, 
        Double.NEGATIVE_INFINITY, 
        Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        root = put(root, p, val, rectangle, true);
    }

    // Helper for put(Point2D p, Value val).
    private Node put(Node n, Point2D p, Value val, RectHV rect, boolean lr) {
        if (n == null) {
            N += 1;
            return new Node(p, val, rect);
        }
        if (n.point.equals(p)) {
            n.value = val;
        } else if (lr && p.x() < n.point.x() || !lr &&  p.y() < n.point.y()) {
            RectHV subRect = (lr) 
            ? new RectHV(rect.xmin(), rect.ymin(), n.point.x(), rect.ymax())
            : new RectHV(rect.xmin(), rect.ymin(), rect.xmax(), n.point.y());
            n.lb = put(n.lb, p, val, subRect, !lr);
        } else {
            RectHV subRect = (lr) 
            ? new RectHV(n.point.x(), rect.ymin(), rect.xmax(), rect.ymax())
            : new RectHV(rect.xmin(), n.point.y(), rect.xmax(), rect.ymax());
            n.rt = put(n.rt, p, val, subRect, !lr);
        }
        return n;
    }

    // Value associated with point p.
    public Value get(Point2D p) {
        return get(root, p, true);
    }

    // Helper for get(Point2D p).
    private Value get(Node n, Point2D p, boolean lr) {
        if (n == null) {
            return null;
        }
        if (n.point.equals(p)) {
            return n.value;
        }
        else if (lr && p.x() < n.point.x() || !lr && p.y() < n.point.y()) {
            return get(n.lb, p, !lr);
        }
        return get(n.rt, p, !lr);
    }

    // Does the symbol table contain the point p?
    public boolean contains(Point2D p) {
        return get(p) != null;
    }

    // All points in the symbol table, in level order.
    public Iterable<Point2D> points() {
        Queue<Node> qIn = new Queue<Node>();
        Queue<Point2D> qOut = new Queue<Point2D>();
        qIn.enqueue(root);
        while (!qIn.isEmpty()) {
            Node n = qIn.dequeue();
            if (n.lb != null) {
                qIn.enqueue(n.lb);
            }
            if (n.rt != null) {
                qIn.enqueue(n.rt);
            }
            qOut.enqueue(n.point);
        }
        return qOut;
    }

    // All points in the symbol table that are inside the rectangle rect.
    public Iterable<Point2D> range(RectHV qRect) {
        Queue<Point2D> q = new Queue<Point2D>();
        range(root, qRect, q);
        return q;
    }

    // Helper for public range(RectHV rect).
    private void range(Node n, RectHV qRect, Queue<Point2D> q) {
        if (n == null || !n.rect.intersects(qRect)) {
            return;
        }
        if (qRect.contains(n.point)) {
            q.enqueue(n.point);
        }
        range(n.lb, qRect, q);
        range(n.rt, qRect, q);
    }

    // A nearest neighbor to point p; null if the symbol table is empty.
    public Point2D nearest(Point2D p) {
        return nearest(root, p, null, Double.POSITIVE_INFINITY, true);
    }
    
    // Helper for public nearest(Point2D p).
    private Point2D nearest(Node n, Point2D p, Point2D nearest, 
                            double nearestDistance, boolean lr) {
        Point2D nearestt = nearest;
        double nearestDistances = nearestDistance;
        if (n == null || nearestDistances < n.rect.distanceSquaredTo(p)) {
            return nearestt;
        }
        if (!n.point.equals(p) 
        && n.point.distanceSquaredTo(p) < nearestDistances) {
            nearestt = n.point;
            nearestDistances = n.point.distanceSquaredTo(p);
        }
        Node first = n.lb;
        Node second = n.rt;
        if (lr && p.x() >= n.point.x() || !lr && p.y() >= n.point.y()) {
            first = n.rt;
            second = n.lb;
        }
        nearestt = nearest(first, p, nearestt, nearestDistances, !lr);
        return nearest(second, p, nearestt, p.distanceSquaredTo(nearestt), !lr);
    }

    // k points that are closest to point p.
    public Iterable<Point2D> nearest(Point2D p, int k) {
        MaxPQ<Point2D> pq = new MaxPQ<Point2D>(p.distanceToOrder());
        nearest(root, p, k, pq, true);
        return pq;
    }

    // Helper for public nearest(Point2D p, int k).
    private void nearest(Node n, Point2D p, int k, MaxPQ<Point2D> pq, 
                         boolean lr) {
        if (n == null || pq.size() >= k 
        && pq.max().distanceSquaredTo(p) < n.rect.distanceSquaredTo(p)) {
            return;
        }
        if (!n.point.equals(p)) {
            pq.insert(n.point);
        }
        if (pq.size() > k) {
            pq.delMax();    
        }
        boolean leftBottom;
        if (lr && p.x() < n.point.x() || !lr && p.y() < n.point.y()) {
            nearest(n.lb, p, k, pq, !lr);
            leftBottom = true;
        } else {
            nearest(n.rt, p, k, pq, !lr);
            leftBottom = false;
        }
        nearest(leftBottom ? n.rt : n.lb, p, k, pq, !lr);
    }

    // Test client. [DO NOT EDIT]
    public static void main(String[] args) {
        KdTreePointST<Integer> st = new KdTreePointST<Integer>();
        double qx = Double.parseDouble(args[0]);
        double qy = Double.parseDouble(args[1]);
        double rx1 = Double.parseDouble(args[2]);
        double rx2 = Double.parseDouble(args[3]);
        double ry1 = Double.parseDouble(args[4]);
        double ry2 = Double.parseDouble(args[5]);
        int k = Integer.parseInt(args[6]);
        Point2D query = new Point2D(qx, qy);
        RectHV rect = new RectHV(rx1, ry1, rx2, ry2);
        int i = 0;
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D p = new Point2D(x, y);
            st.put(p, i++);
        }
        StdOut.println("st.empty()? " + st.isEmpty());
        StdOut.println("st.size() = " + st.size());
        StdOut.println("First " + k + " values:");
        i = 0;
        for (Point2D p : st.points()) {
            StdOut.println("  " + st.get(p));
            if (i++ == k) {
                break;
            }
        }
        StdOut.println("st.contains(" + query + ")? " + st.contains(query));
        StdOut.println("st.range(" + rect + "):");
        for (Point2D p : st.range(rect)) {
            StdOut.println("  " + p);
        }
        StdOut.println("st.nearest(" + query + ") = " + st.nearest(query));
        StdOut.println("st.nearest(" + query + ", " + k + "):");
        for (Point2D p : st.nearest(query, k)) {
            StdOut.println("  " + p);
        }
    }
}
