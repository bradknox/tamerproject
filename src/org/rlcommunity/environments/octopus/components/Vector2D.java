package org.rlcommunity.environments.octopus.components;

import java.util.*;
import java.awt.geom.*;
import java.io.Serializable;

public class Vector2D implements Serializable{
    
    public final static Vector2D ZERO = new Vector2D(0.0, 0.0);
    
    private double x, y;
    private double norm;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.norm = Math.sqrt(x*x + y*y);
    }
    
    public static Vector2D polar(double r, double angleRad) {
        return new Vector2D(r * Math.cos(angleRad), r * Math.sin(angleRad));
    }
    
    public double getX() { return x; }
    
    public double getY() { return y; } 
    
    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }
    
    public Vector2D addScaled(Vector2D v, double s) {
        return new Vector2D(this.x + s*v.x, this.y + s*v.y);
    }
    
    public Vector2D subtract(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }
    
    public Vector2D scale(double s) {
        return new Vector2D(s*x, s*y);
    }
    
    public double norm() {
        return norm;
    }
    
    private double angle() {
        return Math.atan2(y, x);
    }
    
    public Vector2D normalize() {
        return scaleTo(1.0);
    }
    
    public Vector2D scaleTo(double l) {
        if (norm > 0.0) {
            return scale(l/norm);
        } else {
            return this;
        }
    }
    
    public double dot(Vector2D v) {
        return this.x*v.x + this.y*v.y;
    }
    
    public double crossMag(Vector2D v) {
        return this.x*v.y - v.x*this.y;
    }
    
    /**
     * Rotate a vector 90 degrees counterclockwise 
     */
    public Vector2D rotate90() {
        return new Vector2D(-y, x);
    }
    
    /**
     * Rotate a vector 270 degrees counterclockwise 
     */
    public Vector2D rotate270() {
        return new Vector2D(y, -x);
    }
    
    public static Vector2D fromDuple(List<Double> duple) {
        return new Vector2D(duple.get(0), duple.get(1));
    }
    
    public Point2D toPoint2D() {
        return new Point2D.Double(x, y);
    }
    
    public String toString() {
        return String.format("[%f, %f]", x, y);
    }
}
