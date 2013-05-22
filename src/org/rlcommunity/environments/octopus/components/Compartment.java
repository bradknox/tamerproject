package org.rlcommunity.environments.octopus.components;

import java.awt.Polygon;
import java.util.*;
import java.awt.Shape;
import java.awt.geom.*;

public class Compartment{
    
    private static final double ANGLE_CONSTANT = 0.1 / 90.0;
    private static final double NEIGHBOR_ANGLE_SCALE = 0.2;
    
    private Node d, dt, tv, v;
    private List<Node> nodeList;
    private PressureInfluence pressure;
    
    private MuscleInfluence dorsal, transversal, ventral;
    private AxialFrictionInfluence dorsalFriction, ventralFriction;
    private double desiredArea;
    
    private double area;
    private Map<Node, Vector2D> normals;
    
    private List<Polygon> pressureShapes;
    
    /**
     * Constructs a new compartment from four nodes
     * @param d Dorsal node
     * @param dt Dorsal transversal Node
     * @param tv Ventral transversal node
     * @param v Ventral node
     */
    public Compartment(Node d, Node dt, Node tv, Node v) {
        this.d = d;
        this.dt = dt;
        this.tv = tv;
        this.v = v;
        nodeList = Arrays.asList(d, dt, tv, v);
        
        // Internal pressures
        pressure = new PressureInfluence();
        for (Node n: nodeList) {
            n.addInfluence(pressure);
        }
        
        // Muscles
        dorsal = new LongitudinalMuscleInfluence(d, dt, dt.getPosition().subtract(tv.getPosition()).norm());
        d.addInfluence(dorsal);
        dt.addInfluence(dorsal);
        transversal = new TransversalMuscleInfluence(dt, tv, dt.getPosition().subtract(d.getPosition()).norm());
        dt.addInfluence(transversal);
        tv.addInfluence(transversal);
        ventral = new LongitudinalMuscleInfluence(tv, v, dt.getPosition().subtract(tv.getPosition()).norm());
        tv.addInfluence(ventral);
        v.addInfluence(ventral);
        
        // Friction
        dorsalFriction = new AxialFrictionInfluence(dt, d);
        dt.addInfluence(dorsalFriction);
        ventralFriction = new AxialFrictionInfluence(tv, v);
        tv.addInfluence(ventralFriction);
        
        normals = new HashMap<Node, Vector2D>();
        
        pressureShapes = new ArrayList<Polygon>();
        
        computeArea();
        desiredArea = area;
    }
    
    public void setAction(double da, double ta, double va) {
        dorsal.setAction(da);
        transversal.setAction(ta);
        ventral.setAction(va);
    }
    
    public void updateInfluences() {
        computeArea();
        
        pressureShapes.clear();
        pressure.update();
        
        dorsal.update();
        transversal.update();
        ventral.update();
    }
    
    public final double getArea() {
        computeArea();
        return area;
    }
    
    private final void computeArea() {
        // The area of a quadrilateral is 0.5*p*q*sin(theta)
        // Where p and q are the length of the 2 diagonals and theta is the angle between them
        // If we rotate P in the right direction, then area = 0.5*p*q*cos(theta) where theta is 
        // the angle between Q and the rotated P, but this is simply 0.5 * dot product of P' and Q
        // Note, it is possible because of the cos to get a negative area, when this happens, it means
        // that the quadrilateral has flipped and that the normally inward pointing normals are
        // now pointing outward, so if we get a negative area, we should flip the force vectors...
        Vector2D P = tv.getPosition().subtract(d.getPosition());
        Vector2D Q = dt.getPosition().subtract(v.getPosition());
        area = 0.5*P.rotate90().dot(Q);
        if(Math.signum(area) != Math.signum(desiredArea) && desiredArea != 0) {
            System.err.println("Compartment:" + this.toString() + " has flipped inside out. Area:" + area);
        }
    }

    /**
     * Compute the (inward) normal at each node by averaging the unit
     * direction vectors of the two edges connected to it.
     */
    private void computeNormals() {
        for (int i = 0, n = nodeList.size(); i < n; i++) {
            Node prevNode = nodeList.get(i);
            Node curNode = nodeList.get((i+1) % n);
            Node nextNode = nodeList.get((i+2) % n);
            
            Vector2D prevEdge = curNode.getPosition()
                .subtract(prevNode.getPosition());
            Vector2D nextEdge = nextNode.getPosition()
                .subtract(curNode.getPosition());
            
            double crossProd = prevEdge.crossMag(nextEdge);
            Vector2D normal = prevEdge.normalize()
                .subtract(nextEdge.normalize())
                .scaleTo(Math.signum(crossProd));
            
            normals.put(curNode, normal);
        }
    }
    
    /**
     * Compute the (inward) normal of each node by taking a weighted average
     * of the (inward) surface normals of the edges connected to it.
     * Longer edges are given more weight.
     */
    private void computeWeightedNormals() {
        for (int i = 0, n = nodeList.size(); i < n; i++) {
            Node prevNode = nodeList.get(i);
            Node curNode = nodeList.get((i+1) % n);
            Node nextNode = nodeList.get((i+2) % n);
            
            Vector2D prevVector = curNode.getPosition()
                .subtract(prevNode.getPosition()).rotate270();
            Vector2D nextVector = nextNode.getPosition()
                .subtract(curNode.getPosition()).rotate270();
            
            normals.put(curNode, prevVector.add(nextVector).normalize());
        }
    }
    
    public Shape getShape() {
        GeneralPath gp = new GeneralPath();
        gp.moveTo((float) d.getPosition().getX(), (float) d.getPosition().getY());
        for (Node n: Arrays.asList(dt, tv, v)) {
            gp.lineTo((float) n.getPosition().getX(), (float) n.getPosition().getY());
        }
        gp.closePath();
        return gp;
    }
    public List<Vector2D> getShapeForDrawing() {
        List<Vector2D> theList = new ArrayList<Vector2D>();
        theList.add(d.getPosition());
        for (Node n: Arrays.asList(dt, tv, v)) {
        theList.add(n.getPosition());
        }
        return theList;
    }
    
    public final List<Polygon> getPressureShapes() {
        return new ArrayList<Polygon>(pressureShapes);
    }
    
    private static Shape makeLine(Vector2D base, Vector2D disp) {
        double x1 = base.getX();
        double y1 = base.getY();
        double x2 = x1 + disp.getX();
        double y2 = y1 + disp.getY();
        return new Line2D.Double(x1, y1, x2, y2);
    }
    
    private static double sqr(double x) {
        return x * x;
    }
    
    
    private class PressureInfluence implements Influence {
        
        private Map<Node, Vector2D> forces;
        
        public PressureInfluence() {
            forces = new HashMap<Node, Vector2D>();
        }
        
        public void update() {
            // See Compute Area for reason of the absolute value and signum of area...
            double pressureForce = Constants.get().getPressure() * (Math.abs(area) - Math.abs(desiredArea));
            pressureForce = Math.signum(area)*Math.signum(pressureForce)*Math.sqrt(Math.abs(pressureForce));

            // The pressure is applied on every segment proportionally to its area.
            for (int i = 0, n = nodeList.size(); i < n; i++) {
                Node prevNode = nodeList.get(i);
                Node curNode = nodeList.get((i+1) % n);
                Node nextNode = nodeList.get((i+2) % n);
                
                Vector2D prevVector = curNode.getPosition()
                    .subtract(prevNode.getPosition()).rotate270();
                Vector2D nextVector = nextNode.getPosition()
                    .subtract(curNode.getPosition()).rotate270();
                forces.put(curNode, prevVector.scale(pressureForce).add(nextVector.scale(pressureForce)));
            }
        }
        
        public Vector2D getForce(Node target) {
            return forces.containsKey(target) ?
                forces.get(target) :
                Vector2D.ZERO;
        }
    }
    
    private class AngleInfluence implements Influence {
        
        private Node main, previous, next;
        private Map<Node, Vector2D> forces;
        
        public AngleInfluence(Node previous, Node main, Node next) {
            this.previous = previous;
            this.main = main;
            this.next = next;
            
            forces = new HashMap<Node, Vector2D>();
        }
        
        public void update() {
            Vector2D prevVector = main.getPosition().subtract(previous.getPosition());
            Vector2D nextVector = next.getPosition().subtract(main.getPosition());
            
            double angleRad = Math.acos(
                    prevVector.dot(nextVector)
                    / (prevVector.norm() * nextVector.norm())
                );
            double angle = Math.toDegrees(angleRad);
            if (prevVector.crossMag(nextVector) > 0) {
                angle = 360.0 - angle;
            }
            
            double forceMag = ANGLE_CONSTANT * (angle - 90.0);
            
            forces.put(previous, prevVector.rotate270().scaleTo(NEIGHBOR_ANGLE_SCALE * forceMag));
            forces.put(main, normals.get(main).scaleTo(forceMag));
            forces.put(next, nextVector.rotate270().scaleTo(NEIGHBOR_ANGLE_SCALE * forceMag));
        }
        
        public Vector2D getForce(Node target) {
            return forces.containsKey(target) ?
                forces.get(target) :
                Vector2D.ZERO;
        }
    }
}
