package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class AntialiasingLineRenderer implements LineRenderer{
    
	private static final double pi = Math.PI;
	private static final double radius = 0.5;
	private static final double diameter = 1.0;
	private static final double area = pi * Math.pow(radius, 2);	// area of one circular pixel 
	
    private AntialiasingLineRenderer() {
    	
    }

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
    	
    	int y_counter = p1.getIntY() - 2;
    	int x_counter = p1.getIntX() - 2;
    	int y_end = p2.getIntY() + 2;
    	int x_end = p2.getIntX() + 2;
    	
    	for (; y_counter < y_end; y_counter++) {
            for (; x_counter < x_end; x_counter++) {
            	
            	// calculate the distance from the center of pixel to line
                double d = Distance(x_counter, y_counter, p1, p2);
                // calculate the proportion of the intersection area
                double fraction = Fraction(d);
                Color oldColor = Color.fromARGB(panel.getPixel(x_counter, y_counter));
                Color color = p1.getColor().blendInto(fraction, oldColor);	
               
                if(oldColor.asARGB() < color.asARGB()) {
                    panel.setPixel(x_counter, y_counter, 0.0, color.asARGB());
                }
            }
            
            x_counter = p1.getIntX() - 2;
        }
    }
    
 // calculate the distance between the center of pixel and line
    private double Distance(double x_val, double y_val_pixel, Vertex3D p1, Vertex3D p2) {
    	
        double delta_x = p2.getIntX() - p1.getIntX();
        double delta_y = p2.getIntY() - p1.getIntY();
        double slope = delta_y / delta_x;
        double angle1 = Math.atan(delta_y / delta_x);  
        //System.out.println("angle is: " + Math.toDegrees(angle1));
        double y_val = p1.getIntY();
        double x = p1.getIntX();
        
        while(x <= x_val){
        	
             x ++;
             y_val += slope;
        }
        double distance = Math.abs(y_val_pixel - y_val) * Math.cos(angle1);
        //System.out.println("distance is: " + distance);
        return distance;
    }
    private double Fraction(double d){
    	
        double proportion;
        
        // if line is one pixel away from the center 
        if(d >= diameter) {
        	
            proportion = 0.0;
        }
        // if line goes through the center
        else if(d == 0.0) {
        	
            proportion = 1.0;
        }
        // if line is tangent to the circular pixel
        else if(d == radius) {
        	
            proportion = 0.5;
        }
        // if line is 0.5 away from the circular pixel
        else if(d > radius && d < diameter) {
        	
            double theta = Math.acos((d - radius) / radius);
            double pie_area = (theta / pi) * area;
            double triangle_area = (d - radius) * (Math.sqrt(Math.pow(radius, 2) - Math.pow((d - radius), 2)));
            proportion = (pie_area - triangle_area) / area;
        }
        // if line is 0 and 0.5 away from the circular pixel
        else {
        	
            double delta = Math.acos((radius - d) / radius);
            double pi_area = (1 - (delta / pi)) * area;
            double triangle_area = (radius - d) * (Math.sqrt(Math.pow(radius, 2) - Math.pow((radius - d), 2)));
            proportion = (pi_area + triangle_area) / area;
        }
        
        return proportion;
    }

    public static LineRenderer make(){
        return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
    }
}
