package edu.wpi.first.pathweaver.path;

import javafx.geometry.Point2D;

public class PathUtil {
    public static Point2D rawThetaOptimization(Point2D p1, Point2D p2, Point2D p3) {
        Point2D p1Scaled = new Point2D(0, 0);
        Point2D p2Scaled = p2.subtract(p1).multiply(1 / p3.distance(p1));
        Point2D p3Shifted = p3.subtract(p1);
        Point2D p3Scaled = p3Shifted.multiply(1 / p3.distance(p1)); // scale

        //refactor later
        // Point2D q = new Point2D(0, 0); // for reference
        Point2D r = new Point2D(p2Scaled.getX() * p3Scaled.getX() + p2Scaled.getY() * p3Scaled.getY(),
                -p2Scaled.getX() * p3Scaled.getY() + p2Scaled.getY() * p3Scaled.getX());
        // Point2D s = new Point2D(1, 0); // for reference

        double beta = 1 - 2 * r.getX();
        double gamma = Math.pow(4 * (r.getX() - Math.pow(r.distance(p1Scaled), 2)) - 3, 3) / 27;
        double lambda = Math.pow(-gamma, 1.0 / 6.0);

        double sqrtGamma = Math.sqrt(-gamma - Math.pow(beta, 2));
        double phi1 = Math.atan2(sqrtGamma, beta) / 3;
        double ur = lambda * Math.cos(phi1);
        double ui = lambda * Math.sin(phi1);
        double phi2 = Math.atan2(-sqrtGamma, beta) / 3;

        double zr = lambda * Math.cos(phi2);
        double zi = lambda * Math.sin(phi2);

        double t1 = 1.0 / 2 + ur + zr / 2;
        double t2 = 1.0 / 2 - (1.0 / 4) * (ur + zr + Math.sqrt(3) * (ui - zi));
        double t3 = 1.0 / 2 - (1.0 / 4) * (ur + zr - Math.sqrt(3) * (ui - zi));

        double t;
        if (t1 > 0 && t1 < 1) {
            t = t1;
        } else if (t2 > 0 && t2 < 1) {
            t = t2;
        } else {
            t = t3;
        }

        Point2D a1 = p2.subtract(p1).subtract(p3Shifted.multiply(t)).multiply(1 / (t * t - t));
        Point2D a2 = p3Shifted.subtract(a1);

        return a1.multiply(2 * t).add(a2).multiply(1. / 3);
    }
}
