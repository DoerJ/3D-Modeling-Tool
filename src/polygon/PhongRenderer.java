
package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;
import client.interpreter.*;


public class PhongRenderer implements PolygonRenderer{
	
	public static final int NUM_VERTICES = 3;
	Polygon polygon;
	Drawable panel;
	Vertex3D[] points = new Vertex3D[3];
	Color color;
	
	private PhongRenderer() {
		
	}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
		
		this.polygon = polygon;
		this.panel = panel;
		Chain left_chain = polygon.leftChain();
		Chain right_chain = polygon.rightChain();
		
		//System.out.println("The left chain of polygon has: " + left_chain.length() + " vertices");
		//System.out.println("The right chain of polygon has: " + right_chain.length() + " vertices");
		
		// check if triangle has a side parallel to x axis
		Vertex3D[] points = new Vertex3D[3];
		points[0] = polygon.get(0);
		points[1] = polygon.get(1);
		points[2] = polygon.get(2);
		
		
		boolean tri_equal = false;
		if(points[0].getIntY() == points[1].getIntY() && points[1].getIntY() == points[2].getIntY()) {
			tri_equal = true;
		}
		// avoid the situation where the y values of all three vertices are the same
		if(tri_equal == false) {
		if(Math.round(left_chain.get(0).getY()) == Math.round(left_chain.get(1).getY())) {

			renderSpecialTriangle(polygon, panel);
		}	
		else {	
			fillPolygon(left_chain, right_chain, panel);
		}
		}
	}
	
	private void renderSpecialTriangle(Polygon polygon, Drawable panel) {
		
		//System.out.println("hello!");
		
		Chain right_chain = polygon.rightChain();
		Chain left_chain = polygon.leftChain();
		
		Vertex3D p_left;
		Vertex3D p_right;
		Vertex3D p_bottom;
		
		if(polygon.get(0).getIntX() == polygon.get(1).getIntX() || 
				polygon.get(0).getIntX() == polygon.get(2).getIntX() ||
				polygon.get(1).getIntX() == polygon.get(2).getIntX()) {
			
			// if triangle is a perpendicular triangle
			p_left = left_chain.get(1);
			p_right = right_chain.get(0);		
			p_bottom = left_chain.get(2);	
		}
		else {
			p_left = left_chain.get(0);		// top_left vertex
			p_right = left_chain.get(1);		// top_right vertex
			p_bottom = right_chain.get(1);		// bottom_vertex
			
		}	
		
		// get z
		double z_left = p_left.getIntZ();
		double z_right = p_right.getIntZ();
		double z_bottom = p_bottom.getIntZ();
		
		double delta_zl = z_bottom - z_left;
		double delta_zr = z_bottom - z_right;
		//
		// calculate right_slope and left_slope
		double x_left = p_left.getX();
		double x_right = p_right.getX();
		double x_bottom = p_bottom.getX();
		double y_top = p_left.getY();
		double y_bottom = p_bottom.getY();
		double delta_x_l = x_bottom - x_left;
		double delta_x_r = x_bottom - x_right;
		double delta_y = y_top - y_bottom;
		double slope_left = delta_x_l / delta_y;
		double slope_right = delta_x_r / delta_y;
		
		// get z_slope
		double z_l_slope = delta_zl / delta_y;
		double z_r_slope = delta_zr / delta_y;
		
		double left_z= z_left;
		double right_z = z_right;
		double z = z_left;
		
		double left = x_left;
		double right = x_right;
		double y;
		for(y = y_top; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			double x;
			for(x = x_left; x < right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
		        
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, Color.GREEN.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}

			}
			
			left += slope_left;
			right += slope_right;
			x = left;
			
			left_z += z_l_slope;
			right_z += z_r_slope;
			z = left_z;
		}
	}
	
	private void fillPolygon(Chain left_chain, Chain right_chain, Drawable panel) {
		
		int left_vertices = left_chain.length();
		int right_vertices = right_chain.length();
		// left slope is unchanged
		if(left_vertices == 2) {
			
			// left_chain contains 2 vertices, right_chain contains 3 vertices
			Vertex3D p_top = left_chain.get(0);		// top point
			Vertex3D p_bottom = left_chain.get(1);		// bottom point
			Vertex3D p_middle = right_chain.get(1);		// middle point
			
			// get color
			Color top_color = p_top.getColor();
			Color bottom_color = p_bottom.getColor();
			Color middle_color = p_middle.getColor();
			
			// get z
			double z_top = p_top.getIntZ();
			double z_middle = p_middle.getIntZ();
			double z_bottom = p_bottom.getIntZ();
			
			double delta_z_l = p_bottom.getIntZ() - p_top.getIntZ();
			double delta_z_ru = p_middle.getIntZ() - p_top.getIntZ();
			double delta_z_rl = p_bottom.getIntZ() - p_middle.getIntZ();
			//
			
			int x_top = p_top.getIntX();
			int x_middle = p_middle.getIntX();
			int x_bottom = p_bottom.getIntX();
			int y_top = p_top.getIntY();
			int y_middle = p_middle.getIntY();
			int y_bottom = p_bottom.getIntY();
			double delta_x_l = p_bottom.getIntX() - p_top.getIntX();
			double delta_y_l = p_top.getIntY() - p_bottom.getIntY();
			double delta_x_ru = x_middle - x_top;
			double delta_y_ru = y_top - y_middle;
			double delta_x_rl = x_bottom - x_middle;
			double delta_y_rl = y_middle - y_bottom;
			double left_slope = delta_x_l / delta_y_l;		
			double first_right_slope = delta_x_ru / delta_y_ru;
			double second_right_slope = delta_x_rl / delta_y_rl;
			
			// get z_slope
			// z_slope
						double z_left_slope = (z_bottom - z_top) / delta_y_l;
						double z_right_upper_slope = (z_middle - z_top) / delta_y_ru;
						double z_right_lower_slope = (z_bottom - z_middle) / delta_y_rl;
			
			// -----------------------------------------------------------------CHECK!
			renderLeftPolygon(x_top, y_top, y_middle, y_bottom, left_slope, 
					first_right_slope, second_right_slope, panel, z_top, z_left_slope, z_right_upper_slope, 
					z_right_lower_slope);
		}
		// right slope is unchanged
		else {
			
			// left_chain contains 3 vertices, right_chain contains 2 vertices
			Vertex3D p_top = left_chain.get(0);		// top point
			Vertex3D p_middle = left_chain.get(1);		// middle point
			Vertex3D p_bottom = right_chain.get(1);		// bottom point
			
			// get color
			Color top_color = p_top.getColor();
			Color bottom_color = p_bottom.getColor();
			Color middle_color = p_middle.getColor();
			
			// z_value
						double z_top = p_top.getIntZ();
						double z_middle = p_middle.getIntZ();
						double z_bottom = p_bottom.getIntZ();
						
						double delta_z_r = p_bottom.getIntZ() - p_top.getIntZ();
						double delta_z_lu = p_middle.getIntZ() - p_top.getIntZ();
						double delta_z_ll = p_bottom.getIntZ() - p_middle.getIntZ();
			
			int x_top = p_top.getIntX();
			int x_middle = p_middle.getIntX();
			int x_bottom = p_bottom.getIntX();
			int y_top = p_top.getIntY();
			int y_middle = p_middle.getIntY();
			int y_bottom = p_bottom.getIntY();
			double delta_x_r = x_bottom - x_top;
			double delta_y_r = y_top - y_bottom;
			double delta_x_lu = x_middle - x_top;
			double delta_y_lu = y_top - y_middle;
			double delta_x_ll = x_bottom - x_middle;
			double delta_y_ll = y_middle - y_bottom;
			double right_slope = delta_x_r / delta_y_r;
			double first_left_slope = delta_x_lu / delta_y_lu;
			double second_left_slope = delta_x_ll / delta_y_ll;
			
			// z_slope
						double z_right_slope = (z_bottom - z_top) / delta_y_r;
						double z_left_upper_slope = (z_middle - z_top) / delta_y_lu;
						double z_left_lower_slope = (z_bottom - z_middle) / delta_y_ll;
			
			renderRightPolygon(x_top, y_top, y_middle, y_bottom, right_slope, 
					first_left_slope, second_left_slope, panel, z_top, z_right_slope, z_left_upper_slope, z_left_lower_slope);
		}
		
	}
	
	private void renderLeftPolygon(int x_top, int y_top, int y_middle, int y_bottom, double left_slope, 
			double upper_right_slope, double lower_right_slope, Drawable panel, double z_top, double z_left_slope, double z_right_upper_slope, 
			double z_right_lower_slope) {
		
		double x = x_top;
		
		// z
				double z = z_top;
				double z_left = z_top;
				double z_right = z_top;

		
		double x_left = x_top;
		double x_right = x_top;
		double y = y_top;
	
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			
			double disX = x_right - x_left;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
		        
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, Color.GREEN.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
			}
			x_left += left_slope;
			x_right += upper_right_slope;
			x = x_left;
			
			z_left += z_left_slope;
			z_right += z_right_upper_slope;
			z = z_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, Color.GREEN.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
			}
			x_left += left_slope;
			x_right += lower_right_slope;
			x = x_left;
			
			z_left += z_left_slope;
			z_right += z_right_lower_slope;
			z = z_left;
		}
	}
	
	private void renderRightPolygon(int x_top, int y_top, int y_middle, int y_bottom, double right_slope, 
			double upper_left_slope, double lower_left_slope, Drawable panel, double z_top, double z_right_slope, double z_left_upper_slope, double z_left_lower_slope) {
		
		double x = x_top;
		
		// z
		double z = z_top;
		double z_left = z_top;
		double z_right = z_top;
		
		double y = y_top;
		double x_left = x_top;
		double x_right = x_top;
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			
			double disX = x_right - x_left;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, Color.GREEN.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
	
			}
			x_left += upper_left_slope;
			x_right += right_slope;
			x = x_left;

			
			z_left += z_left_upper_slope;
			z_right += z_right_slope;
			z = z_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)x, (int)(y), 0, Color.GREEN.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				
			}
			x_left += lower_left_slope;
			x_right += right_slope;
			x = x_left;
	
			z_left += z_left_lower_slope;
			z_right += z_right_slope;
			z = z_left;
		}	
	}
	
	public static PolygonRenderer make() {
		
		return new PhongRenderer();
	}	
}

