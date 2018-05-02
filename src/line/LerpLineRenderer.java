package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import java.lang.Math;

import client.interpreter.SimpInterpreter;
import windowing.graphics.Color;


public class LerpLineRenderer implements LineRenderer {
	
	// constructor
	private LerpLineRenderer() {
		
	}
	
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		//System.out.println("printing p1:");
		// get the rgb value of end-points
		double r1 = p1.getColor().getR();
		double g1 = p1.getColor().getG();
		double b1 = p1.getColor().getB();
		
		double r2 = p2.getColor().getR();
		double g2 = p2.getColor().getG();
		double b2 = p2.getColor().getB();
		
		double disR = r2 - r1;
		double disG = g2 - g1;
		double disB = b2 - b1;
		
		double disX = p2.getX() - p1.getX();		// distance from p1 to p2 in x direction
		double disY = p2.getY() - p1.getY();		// distance from p1 to p2 in y direction
		
		// distance from p1 to p2 in z direction
		double disZ = p2.getZ() - p1.getZ();
		double z_slope = disZ / disX;	// slope of z
		double slope = disY / disX;		// slope of line
		
		double R_slope = disR / disX;
		double G_slope = disG / disX;
		double B_slope = disB / disX;
		
		double r = r1;
		double g = g1;
		double b = b1;
		
		double x = p1.getX();
		double x1 = p2.getX();
		double z = p1.getZ();
		double y = p1.getY();
		double y1 = p2.getY();
		
//		System.out.println("------------------------");
//		System.out.println("The x of point1 is: " + x + " and the x of point2(x1) is: " + x1);
//		System.out.println("------------------------");
		
		// special cases, where p1.x = p2.x
		if(disX == 0) {
			RenderStraightedge(y, y1, z, x, r, g, b, drawable);
		}
		// if p1.x != p2.x
		else {
		
		while(x < x1 && x >= 0 && y >= 0 && x <= 650 && y <= 650) {
			int argbColor = new Color(r, g, b).asARGB();
			// add z-buffer to check whether each pixel should be drawn
			if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
			// check whether z is greater than the corresponding z-value in z-buffer
			drawable.setPixel((int)(x), (int)(y), z, argbColor);
			SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = z;
			}
			x ++;
			y += slope;
			z += z_slope;
			r += R_slope;
			g += G_slope;
			b += B_slope;	
			}
		// if p2.getX() < p1.getX()
		x = p2.getX();
		x1 = p1.getX();
		y = p2.getY();
		z = p2.getZ();
		while(x < x1 && x >= 0 && y >= 0 && x <= 650 && y <= 650) {
			int argbColor = new Color(r, g, b).asARGB();
			// add z-buffer to check whether each pixel should be drawn
			if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
			// check whether z is greater than the corresponding z-value in z-buffer
			drawable.setPixel((int)(x), (int)(y), z, argbColor);
			SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = z;
			}
			x ++;
			y += slope;
			z += z_slope;
			r -= R_slope;
			g -= G_slope;
			b -= B_slope;	
			}
			
		}
	}
	
	private void RenderStraightedge(double y, double y1, double z, double x, double r, 
			double g, double b, Drawable drawable) {
		double y_low;
		double y_high;
		// decide y_low and y_high
		if(y > y1) {
			y_low = y1;
			y_high = y;
		}
		else {
			y_high = y1;
			y_low = y;
		}
		double y_move = y_low;
				while(y_move < y_high && y >= 0 && y1 >= 0 && y <= 650 && y1 <= 650 && x >= 0 && x <= 650) {
					// add z-buffer to check whether a pixel should be drawn
					if(z >= SimpInterpreter.z_buffer[(int)x][(int)y_move]) {
					int argbColor = new Color(r, g, b).asARGB();
					drawable.setPixel((int)x, (int)y_move, z, argbColor);
					SimpInterpreter.z_buffer[(int)x][(int)y_move] = z;
					}
					y_move ++;
				}
		}
	
	public static LineRenderer make() {
		
		return new LerpLineRenderer();
	}

}
