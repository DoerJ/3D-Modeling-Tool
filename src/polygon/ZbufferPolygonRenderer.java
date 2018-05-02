
package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;
import client.interpreter.*;

public class ZbufferPolygonRenderer implements PolygonRenderer{
	
	public static final int NUM_VERTICES = 3;
	Polygon polygon;
	Drawable panel;
	Vertex3D[] points = new Vertex3D[3];
	Color color;
	
	private ZbufferPolygonRenderer() {
		
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
		if(points[0].getY() == points[1].getY() && points[1].getY() == points[2].getY()) {
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
		
		Vertex3D p_left = polygon.get(0);
		Vertex3D p_right = polygon.get(1);
		Vertex3D p_bottom = polygon.get(2);
		
		if(polygon.get(0).getIntX() == polygon.get(1).getIntX() || 
				polygon.get(0).getIntX() == polygon.get(2).getIntX() ||
				polygon.get(1).getIntX() == polygon.get(2).getIntX()) {
			
			// if triangle is a perpendicular triangle
			p_left = left_chain.get(1);
			p_right = right_chain.get(0);		
			p_bottom = right_chain.get(1);	
		}
		else if(left_chain.get(0).getIntX() <= left_chain.get(1).getIntX()){
			p_left = left_chain.get(0);		// top_left vertex
			p_right = left_chain.get(1);		// top_right vertex
			p_bottom = right_chain.get(1);		// bottom_vertex
			
			System.out.println("--------------------------------");
			System.out.println("p_left is: " + p_left.toString());
			System.out.println("p_right is: " + p_right.toString());
			System.out.println("p_bottom is: " + p_bottom.toString());
			System.out.println("--------------------------------");
		}
		else if(left_chain.get(0).getIntX() >= left_chain.get(1).getIntX()) {
			p_left = left_chain.get(1);
			p_right = left_chain.get(0);
			p_bottom = right_chain.get(1);
		}
		
		// now we have p_left, p_right, and p_bottom
		// check if same vertices have been stored
		
		// get color
		Color left_color = p_left.getColor();
		Color bottom_color = p_bottom.getColor();
		Color right_color = p_right.getColor();
		
		
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
		double y_top = p_right.getIntY();
		double y_bottom = p_bottom.getIntY();
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
		
		// rgb of left vertex
		double left_r = left_color.getR();
		double left_g = left_color.getG();
		double left_b = left_color.getB();
				
		// rgb of bottom vertex
		double bottom_r = bottom_color.getR();
		double bottom_g = bottom_color.getG();
		double bottom_b = bottom_color.getB();
				
		// rgb of right vertex
		double right_r = right_color.getR();
		double right_g = right_color.getG();
		double right_b = right_color.getB();
		
		// rgb of x
		double x_left_r = left_r;
		double x_right_r = right_r;
		double x_left_g = left_g;
		double x_right_g = right_g;
		double x_left_b= left_b;
		double x_right_b = right_b;
		
		// lerp on left slope
		double disleft_r = bottom_r - left_r;
		double disleft_g = bottom_g - left_g;
		double disleft_b = bottom_b - left_b;
		double disY_left = y_top - y_bottom;
		double R_left_slope = disleft_r / disY_left;
		double G_left_slope = disleft_g / disY_left;
		double B_left_slope = disleft_b / disY_left;
		
		// lerp on right slope
		double disright_r = bottom_r - right_r;
		double disright_g = bottom_g - right_g;
		double disright_b = bottom_b - right_b;
		double disY_right = y_top - y_bottom;
		double R_right_slope = disright_r / disY_right;
		double G_right_slope = disright_g / disY_right;
		double B_right_slope = disright_b / disY_right;
		
		double r = x_left_r;
		double g = x_left_g;
		double b = x_left_b;
		
		double y;
		double x;
		for(y = y_top; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			for(x = x_left; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
		        
				Color argb = new Color(r, g, b);
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			
			x_left += slope_left;
			x_right += slope_right;
			
			// color gradient
			x_left_r += R_left_slope;
			x_left_g += G_left_slope;
			x_left_b += B_left_slope;
									
			x_right_r += R_right_slope;
			x_right_g += G_right_slope;
			x_right_b += B_right_slope;
						
			r = x_left_r;
			g = x_left_g;
			b = x_left_b;
			
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
					first_right_slope, second_right_slope, panel, top_color, 
					bottom_color, middle_color, z_top, z_left_slope, z_right_upper_slope, 
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
					first_left_slope, second_left_slope, panel, top_color, bottom_color, middle_color, 
					z_top, z_right_slope, z_left_upper_slope, z_left_lower_slope);
		}
		
	}
	
	private void renderLeftPolygon(int x_top, int y_top, int y_middle, int y_bottom, double left_slope, 
			double upper_right_slope, double lower_right_slope, Drawable panel, Color top_color, Color bottom_color, 
			Color middle_color, double z_top, double z_left_slope, double z_right_upper_slope, 
			double z_right_lower_slope) {
		
		double x = x_top;
		
		// z
				double z = z_top;
				double z_left = z_top;
				double z_right = z_top;
		
		// rgb of top vertex
		double top_r = top_color.getR();
		double top_g = top_color.getG();
		double top_b = top_color.getB();
		
		// rgb of x
		double x_left_r = top_r;
		double x_right_r = top_r;
		double x_left_g = top_g;
		double x_right_g = top_g;
		double x_left_b= top_b;
		double x_right_b = top_b;
		
		// rgb of bottom vertex
		double bottom_r = bottom_color.getR();
		double bottom_g = bottom_color.getG();
		double bottom_b = bottom_color.getB();
		
		// rgb of middle vertex
		double middle_r = middle_color.getR();
		double middle_g = middle_color.getG();
		double middle_b = middle_color.getB();
		
		// lerp on left slope
		double disleft_r = bottom_r - top_r;
		double disleft_g = bottom_g - top_g;
		double disleft_b = bottom_b - top_b;
		double disY_left = y_top - y_bottom;
		double R_left_slope = disleft_r / disY_left;
		double G_left_slope = disleft_g / disY_left;
		double B_left_slope = disleft_b / disY_left;
		
		// lerp on right upper slope
		double disright_upper_r = middle_r - top_r;
		double disright_upper_g = middle_g - top_g;
		double disright_upper_b = middle_b - top_b;
		double disY_rightUpper = y_top - y_middle;
		double R_right_upper_slope = disright_upper_r / disY_rightUpper;
		double G_right_upper_slope = disright_upper_g / disY_rightUpper;
		double B_right_upper_slope = disright_upper_b / disY_rightUpper;
		
		// lerp on right lower slope
		double disright_lower_r = bottom_r - middle_r;
		double disright_lower_g = bottom_g - middle_g;
		double disright_lower_b = bottom_b - middle_b;
		double disY_rightLower = y_middle - y_bottom;
		double R_right_lower_slope = disright_lower_r / disY_rightLower;
		double G_right_lower_slope = disright_lower_g / disY_rightLower;
		double B_right_lower_slope = disright_lower_b / disY_rightLower;
		
		double x_left = x_top;
		double x_right = x_top;
		double y = y_top;
		
		double r = x_left_r;
		double g = x_left_g;
		double b = x_left_b;
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
		        
				Color argb = new Color(r, g, b);
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, argb.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			x_left += left_slope;
			x_right += upper_right_slope;
			x = x_left;
			
			// color gradient
			x_left_r += R_left_slope;
			x_left_g += G_left_slope;
			x_left_b += B_left_slope;
			
			x_right_r += R_right_upper_slope;
			x_right_g += G_right_upper_slope;
			x_right_b += B_right_upper_slope;
			
			r = x_left_r;
			g = x_left_g;
			b = x_left_b;
			
			z_left += z_left_slope;
			z_right += z_right_upper_slope;
			z = z_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				Color argb = new Color(r, g, b);
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, argb.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			x_left += left_slope;
			x_right += lower_right_slope;
			x = x_left;
			
			// color gradient
			x_left_r += R_left_slope;
			x_left_g += G_left_slope;
			x_left_b += B_left_slope;
						
			x_right_r += R_right_lower_slope;
			x_right_g += G_right_lower_slope;
			x_right_b += B_right_lower_slope;
			
			r = x_left_r;
			g = x_left_g;
			b = x_left_b;
			
			z_left += z_left_slope;
			z_right += z_right_lower_slope;
			z = z_left;
		}
	}
	
	private void renderRightPolygon(int x_top, int y_top, int y_middle, int y_bottom, double right_slope, 
			double upper_left_slope, double lower_left_slope, Drawable panel, Color top_color, Color bottom_color, 
			Color middle_color, double z_top, double z_right_slope, double z_left_upper_slope, double z_left_lower_slope) {
		
		double x = x_top;
		
		// z
		double z = z_top;
		double z_left = z_top;
		double z_right = z_top;
		
		// rgb of top vertex
		double top_r = top_color.getR();
		double top_g = top_color.getG();
		double top_b = top_color.getB();
		
		// rgb of x
		double x_left_r = top_r;
		double x_right_r = top_r;
		double x_left_g = top_g;
		double x_right_g = top_g;
		double x_left_b= top_b;
		double x_right_b = top_b;
		
		// rgb of bottom vertex
		double bottom_r = bottom_color.getR();
		double bottom_g = bottom_color.getG();
		double bottom_b = bottom_color.getB();
		
		// rgb of middle vertex
		double middle_r = middle_color.getR();
		double middle_g = middle_color.getG();
		double middle_b = middle_color.getB();
		
		// lerp on left upper slope
		double disleft_upper_r = middle_r - top_r;
		double disleft_upper_g = middle_g - top_g;
		double disleft_upper_b = middle_b - top_b;
		double disY_leftUpper = y_top - y_middle;
		double R_left_upper_slope = disleft_upper_r / disY_leftUpper;
		double G_left_upper_slope = disleft_upper_g / disY_leftUpper;
		double B_left_upper_slope = disleft_upper_b / disY_leftUpper;
		
		// lerp on left lower slope
		double disleft_lower_r = bottom_r - middle_r;
		double disleft_lower_g = bottom_g - middle_g;
		double disleft_lower_b = bottom_b - middle_b;
		double disY_leftLower = y_middle - y_bottom;
		double R_left_lower_slope = disleft_lower_r / disY_leftLower;
		double G_left_lower_slope = disleft_lower_g / disY_leftLower;
		double B_left_lower_slope = disleft_lower_b / disY_leftLower;
		
		// lerp on right slope
		double disright_r = bottom_r - top_r;
		double disright_g = bottom_g - top_g;
		double disright_b = bottom_b - top_b;
		double disY_right = y_top - y_bottom;
		double R_right_slope = disright_r / disY_right;
		double G_right_slope = disright_g / disY_right;
		double B_right_slope = disright_b / disY_right;
		
		double y = y_top;
		double x_left = x_top;
		double x_right = x_top;
		
		double r = x_left_r;
		double g = x_left_g;
		double b = x_left_b;
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				Color argb = new Color(r, g, b);
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)(x), (int)Math.round(y), 0, argb.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			x_left += upper_left_slope;
			x_right += right_slope;
			x = x_left;
			
			// color gradient
			x_left_r += R_left_upper_slope;
			x_left_g += G_left_upper_slope;
			x_left_b += B_left_upper_slope;
									
			x_right_r += R_right_slope;
			x_right_g += G_right_slope;
			x_right_b += B_right_slope;
						
			r = x_left_r;
			g = x_left_g;
			b = x_left_b;
			
			z_left += z_left_upper_slope;
			z_right += z_right_slope;
			z = z_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			double delta_z = z_right - z_left;
			double z_slope = delta_z / disX;
			
			for(; x < x_right; x++) {
				
				double camera_bottom = (panel.getHeight() - SimpInterpreter.camera_height) / 2;
		        double camera_top = panel.getHeight() - camera_bottom;
		        double camera_left = (panel.getWidth() - SimpInterpreter.camera_width) / 2;
		        double camera_right = panel.getWidth() - camera_left;
				Color argb = new Color(r, g, b);
				if((int)Math.round(y) > camera_bottom && (int)Math.round(y) < camera_top && (int)Math.round(x) > camera_left && (int)Math.round(x) < camera_right && z <= -1
						&& z >= -200) {
					if(z >= SimpInterpreter.z_buffer[(int)(x)][(int)(y)]) {
					// set pixel
					panel.setPixel((int)x, (int)(y), 0, argb.asARGB());
					SimpInterpreter.z_buffer[(int)(x)][(int)(y)] = (int)z;
				}
					z += z_slope;
					}
				
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			x_left += lower_left_slope;
			x_right += right_slope;
			x = x_left;
			
			// color gradient
			x_left_r += R_left_lower_slope;
			x_left_g += G_left_lower_slope;
			x_left_b += B_left_lower_slope;
												
			x_right_r += R_right_slope;
			x_right_g += G_right_slope;
			x_right_b += B_right_slope;
									
			r = x_left_r;
			g = x_left_g;
			b = x_left_b;
			
			z_left += z_left_lower_slope;
			z_right += z_right_slope;
			z = z_left;
		}	
	}
	
	public static PolygonRenderer make() {
		
		return new ZbufferPolygonRenderer();
	}	
}

// --------------------------------------------------------------------------------
/*
package polygon;

import client.interpreter.SimpInterpreter;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ZbufferPolygonRenderer implements PolygonRenderer {
	
	
    private ZbufferPolygonRenderer() {
    	
    }
 
    double delta_r_top;// = p_right.getColor().getIntR() - p_left.getColor().getIntR();
    double delta_g_top;// = p_right.getColor().getIntG() - p_left.getColor().getIntG();
    double delta_b_top;// = p_right.getColor().getIntB() - p_left.getColor().getIntB();
    double R_slope_top = 0 ;//= delta_r_top / (p_right.getIntY() - p_left.getIntY());
    double G_slope_top = 0;//= delta_g_top / (p_right.getIntY() - p_left.getIntY());
    double B_slope_top = 0;// = delta_b_top / (p_right.getIntY() - p_left.getIntY());

    double delta_r_left ;// = p_left.getColor().getIntR() - p_bottom.getColor().getIntR();
    double delta_g_left ;// = p_left.getColor().getIntG() - p_bottom.getColor().getIntG();
    double delta_b_left;// = p_left.getColor().getIntB() - p_bottom.getColor().getIntB();
    double R_slope_left = 0 ;//= delta_r_left / (p_left.getIntY() - p_bottom.getIntY());
    double G_slope_left = 0;// = delta_g_left / (p_left.getIntY() - p_bottom.getIntY());
    double B_slope_left = 0;// = delta_b_left / (p_left.getIntY() - p_bottom.getIntY());

    double delta_r_right;// = p_right.getColor().getIntR() - p_bottom.getColor().getIntR();
    double delta_g_right;// = p_right.getColor().getIntG() - p_bottom.getColor().getIntG();
    double delta_b_right;// = p_right.getColor().getIntB() - p_bottom.getColor().getIntB();
    double R_slope_right = 0;// = delta_r_right / (p_right.getIntY() - p_bottom.getIntY());
    double G_slope_right = 0;// = delta_g_right / (p_right.getIntY() - p_bottom.getIntY());
    double B_slope_right = 0;//= delta_b_right / (p_right.getIntY() - p_bottom.getIntY());

    @Override
    public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
        Chain left_chain = polygon.leftChain();
        if(left_chain.get(0).getY() != left_chain.get(1).getY()) {
            fillPolygon(polygon, panel);
        }else if(left_chain.get(0).getY() == left_chain.get(1).getY()) {
            x_same_triangle(polygon, panel);
        }
    }

    private void x_same_triangle(Polygon polygon, Drawable panel) {
        Chain left_chain = polygon.leftChain();
        Vertex3D p_left;
        Vertex3D p_right;
        Vertex3D p_bottom;


        if(polygon.get(0).getX() == polygon.get(1).getX() ||
                polygon.get(0).getX() == polygon.get(2).getX() ||
                polygon.get(1).getX() == polygon.get(2).getX()) {
            //System.out.println("RENDERING");
            p_left = left_chain.get(0);
            p_right = left_chain.get(1);
            p_bottom = left_chain.get(2);
        }
        else {
            //System.out.println("RENDERING");
        	for(int i = 0 ; i < polygon.length(); i++){
        		System.out.println("printing before bug: " + polygon.get(i));
        	}
        	
            p_right = left_chain.get(0);
            p_left = left_chain.get(1);
            p_bottom = left_chain.get(2);
        }

        delta_r_top = p_left.getColor().getIntR() - p_right.getColor().getIntR();
        delta_g_top = p_left.getColor().getIntG() - p_right.getColor().getIntG();
        delta_b_top = p_left.getColor().getIntB() - p_right.getColor().getIntB();
        R_slope_top = delta_r_top / (p_right.getIntY() - p_left.getIntY());
        G_slope_top = delta_g_top / (p_right.getIntY() - p_left.getIntY());
        B_slope_top = delta_b_top / (p_right.getIntY() - p_left.getIntY());

        delta_r_left = p_bottom.getColor().getIntR() - p_left.getColor().getIntR();
        delta_g_left = p_bottom.getColor().getIntG() - p_left.getColor().getIntG();
        delta_b_left = p_bottom.getColor().getIntB() - p_left.getColor().getIntB();
        R_slope_left = delta_r_left / (p_left.getIntY() - p_bottom.getIntY());
        G_slope_left = delta_g_left / (p_left.getIntY() - p_bottom.getIntY());
        B_slope_left = delta_b_left / (p_left.getIntY() - p_bottom.getIntY());

        delta_r_right = p_bottom.getColor().getIntR() - p_right.getColor().getIntR();
        delta_g_right = p_bottom.getColor().getIntG() - p_right.getColor().getIntG();
        delta_b_right = p_bottom.getColor().getIntB() - p_right.getColor().getIntB();
        R_slope_right = delta_r_right / (p_right.getIntY() - p_bottom.getIntY());
        G_slope_right = delta_g_right / (p_right.getIntY() - p_bottom.getIntY());
        B_slope_right = delta_b_right / (p_right.getIntY() - p_bottom.getIntY());

        double x_left = p_left.getIntX();
        double x_right = p_right.getIntX();
        double x_bottom = p_bottom.getIntX();
        double y_top = p_left.getIntY();
        double y_bottom = p_bottom.getIntY();
        //added
        double z_left = p_left.getIntZ();
        double z_right = p_right.getIntZ();
        double z_bottom = p_bottom.getIntZ();

        double delta_L = x_bottom - x_left;
        double delta_R = x_bottom - x_right;
        double delta_y = y_top - y_bottom;
        double slope_left = delta_L / delta_y;
        double slope_right = delta_R / delta_y;
        // added
        double z_slope_left = (z_bottom - z_left) / delta_y;
        double z_slope_right = (z_bottom - z_right) / delta_y;

        double x_L = x_left;
        double x_R = x_right;
        double z_L = z_left;
        double z_R = z_right;
        double r_L = p_left.getColor().getIntR();
        double g_L = p_left.getColor().getIntG();
        double b_L = p_left.getColor().getIntB();
        double r_R = p_right.getColor().getIntR();
        double g_R = p_right.getColor().getIntG();
        double b_R = p_right.getColor().getIntB();

        for(double y = y_top; y > y_bottom; y--) {
            Vertex3D p1 = new Vertex3D(x_L, y, z_L, Color.fromARGB(Color.makeARGB((int)Math.round(r_L),(int)Math.round(g_L),(int)Math.round(b_L))));
            Vertex3D p2 = new Vertex3D(x_R, y, z_R, Color.fromARGB(Color.makeARGB((int)Math.round(r_R),(int)Math.round(g_R),(int)Math.round(b_R))));
            render_bet_points(p1, p2, panel);
            x_L += slope_left;
            x_R += slope_right;
            // added
            z_L += z_slope_left;
            z_R += z_slope_right;
            r_L += R_slope_left;
            g_L += G_slope_left;
            b_L += B_slope_left;
            r_R += R_slope_right;
            g_R += G_slope_right;
            b_R += B_slope_right;
        }
    }

    private void fillPolygon(Polygon polygon, Drawable panel) {
        Chain left_chain = polygon.leftChain();
        Chain right_chain = polygon.rightChain();
        int count_left = left_chain.length();
        if(count_left == 2) {
            Vertex3D p_top = left_chain.get(0);
            Vertex3D p_bottom = left_chain.get(1);
            Vertex3D p_middle = right_chain.get(1);

            int x_top = p_top.getIntX();
            int x_middle = p_middle.getIntX();
            int x_bottom = p_bottom.getIntX();
            int y_top = p_top.getIntY();
            int y_middle = p_middle.getIntY();
            int y_bottom = p_bottom.getIntY();
            double delta_L = p_bottom.getIntX() - p_top.getIntX();
            double delta_y = p_top.getIntY() - p_bottom.getIntY();
            double delta_x_ru = x_middle - x_top;
            double delta_y_ru = y_top - y_middle;
            double delta_x_rl = x_bottom - x_middle;
            double delta_y_rl = y_middle - y_bottom;
            double left_slope = delta_L / delta_y;
            double first_right_slope = delta_x_ru / delta_y_ru;
            double second_right_slope = delta_x_rl / delta_y_rl;
            // =======================================================================
            delta_r_top = p_middle.getColor().getIntR() - p_top.getColor().getIntR(); // changed order <=============
            delta_g_top = p_middle.getColor().getIntG() - p_top.getColor().getIntG();
            delta_b_top = p_middle.getColor().getIntB() - p_top.getColor().getIntB();
            R_slope_top = delta_r_top / (p_top.getIntY() - p_middle.getIntY());
            G_slope_top = delta_g_top / (p_top.getIntY() - p_middle.getIntY());
            B_slope_top = delta_b_top / (p_top.getIntY() - p_middle.getIntY());

            delta_r_left = p_bottom.getColor().getIntR() - p_top.getColor().getIntR();
            delta_g_left = p_bottom.getColor().getIntG() - p_top.getColor().getIntG();
            delta_b_left = p_bottom.getColor().getIntB() - p_top.getColor().getIntB();
            R_slope_left = delta_r_left / (p_top.getIntY() - p_bottom.getIntY());
            G_slope_left = delta_g_left / (p_top.getIntY() - p_bottom.getIntY());
            B_slope_left = delta_b_left / (p_top.getIntY() - p_bottom.getIntY());

            delta_r_right = p_bottom.getColor().getIntR() - p_middle.getColor().getIntR();
            delta_g_right = p_bottom.getColor().getIntG() - p_middle.getColor().getIntG();
            delta_b_right = p_bottom.getColor().getIntB() - p_middle.getColor().getIntB();
            R_slope_right = delta_r_right / (p_middle.getIntY() - p_bottom.getIntY());
            G_slope_right = delta_g_right / (p_middle.getIntY() - p_bottom.getIntY());
            B_slope_right = delta_b_right / (p_middle.getIntY() - p_bottom.getIntY());

            render1stPolygon(x_top, y_top, y_middle, y_bottom, left_slope,
                    first_right_slope, second_right_slope, R_slope_top, G_slope_top, B_slope_top,
                    R_slope_left, G_slope_left, B_slope_left, R_slope_right, G_slope_right, B_slope_right,
                    p_top, p_middle, p_bottom, panel);
        }else if(count_left == 3){
            Vertex3D p_top = left_chain.get(0);
            Vertex3D p_middle = left_chain.get(1);
            Vertex3D p_bottom = right_chain.get(1);

            int x_top = p_top.getIntX();
            int x_middle = p_middle.getIntX();
            int x_bottom = p_bottom.getIntX();
            int y_top = p_top.getIntY();
            int y_middle = p_middle.getIntY();
            int y_bottom = p_bottom.getIntY();
            double delta_X = x_bottom - x_top;
            double delta_Y = y_top - y_bottom;
            double delta_x_lu = x_middle - x_top;
            double delta_y_lu = y_top - y_middle;
            double delta_x_ll = x_bottom - x_middle;
            double delta_y_ll = y_middle - y_bottom;
            double right_slope = delta_X / delta_Y;
            double first_left_slope = delta_x_lu / delta_y_lu;
            double second_left_slope = delta_x_ll / delta_y_ll;

            delta_r_top = p_middle.getColor().getIntR() - p_top.getColor().getIntR(); // changed order <<=====================
            delta_g_top = p_middle.getColor().getIntG() - p_top.getColor().getIntG();
            delta_b_top = p_middle.getColor().getIntB() - p_top.getColor().getIntB();
            R_slope_top = delta_r_top / (p_top.getIntY() - p_middle.getIntY());
            G_slope_top = delta_g_top / (p_top.getIntY() - p_middle.getIntY());
            B_slope_top = delta_b_top / (p_top.getIntY() - p_middle.getIntY());

            delta_r_left = p_bottom.getColor().getIntR() - p_middle.getColor().getIntR();
            delta_g_left = p_bottom.getColor().getIntG() - p_middle.getColor().getIntG();
            delta_b_left = p_bottom.getColor().getIntB() - p_middle.getColor().getIntB();
            R_slope_left = delta_r_left / (p_middle.getIntY() - p_bottom.getIntY());
            G_slope_left = delta_g_left / (p_middle.getIntY() - p_bottom.getIntY());
            B_slope_left = delta_b_left / (p_middle.getIntY() - p_bottom.getIntY());

            delta_r_right = p_bottom.getColor().getIntR() - p_top.getColor().getIntR();
            delta_g_right = p_bottom.getColor().getIntG() - p_top.getColor().getIntG();
            delta_b_right = p_bottom.getColor().getIntB() - p_top.getColor().getIntB();
            R_slope_right = delta_r_right / (p_top.getIntY() - p_bottom.getIntY());
            G_slope_right = delta_g_right / (p_top.getIntY() - p_bottom.getIntY());
            B_slope_right = delta_b_right / (p_top.getIntY() - p_bottom.getIntY());

            render2ndPolygon(x_top, y_top, y_middle, y_bottom, right_slope,
                    first_left_slope, second_left_slope, R_slope_top, G_slope_top, B_slope_top,
                    R_slope_left, G_slope_left, B_slope_left, R_slope_right, G_slope_right, B_slope_right,p_top, p_middle, p_bottom, panel);
        }

    }

    private void render1stPolygon(int x_top, int y_top, int y_middle, int y_bottom, double left_slope,
                                  double upper_right_slope, double lower_right_slope, double R_slope_top,double G_slope_top,
                                  double B_slope_top,double R_slope_left,double G_slope_left,double B_slope_left,
                                  double R_slope_right,double G_slope_right,double B_slope_right, Vertex3D p_top,Vertex3D p_middle,
                                  Vertex3D p_bottom, Drawable panel) {
        double x = x_top;
        double x_left = x_top;
        double x_right = x_top;
        double y = y_top;

        // added
        double z_top = p_top.getIntZ();
        double z_middle = p_middle.getIntZ();
        double z_bottom = p_bottom.getIntZ();

        double delta_Y = y_top - y_bottom;
        double delta_y_ru = y_top - y_middle;
        double delta_y_rl = y_middle - y_bottom;

        double delta_Z_Left = z_bottom - z_top;
        double delta_Z_R_T = z_middle - z_top;
        double delta_Z_R_L = z_bottom - z_middle;

        double z_L_Slope = delta_Z_Left / delta_Y;
        double z_R_Top_Slope = delta_Z_R_T / delta_y_ru;
        double z_R_Lower_Slope = delta_Z_R_L/ delta_y_rl;

        double z_L = p_top.getIntZ();
        double z_R_u = p_top.getIntZ();
        double z_R_l = p_middle.getIntZ();

        //=====================================
        double r_L = p_top.getColor().getIntR();
        double g_L = p_top.getColor().getIntG();
        double b_L = p_top.getColor().getIntB();
        double r_R_u = p_top.getColor().getIntR();
        double g_R_u = p_top.getColor().getIntG();
        double b_R_u = p_top.getColor().getIntB();
        double r_R_l = p_middle.getColor().getIntR();
        double g_R_l = p_middle.getColor().getIntG();
        double b_R_l = p_middle.getColor().getIntB();

        for(; y > y_middle; y--) {
            Vertex3D p1 = new Vertex3D(x, y, z_L, Color.fromARGB(Color.makeARGB((int)Math.round(r_L),(int)Math.round(g_L),
                    (int)Math.round(b_L))));
            Vertex3D p2 = new Vertex3D(x_right, y, z_R_u, Color.fromARGB(Color.makeARGB((int)Math.round(r_R_u),(int)Math.round(g_R_u),
                    (int)Math.round(b_R_u))));
            render_bet_points(p1, p2, panel);
            x_left += left_slope;
            x_right += upper_right_slope;
            z_L += z_L_Slope;
            z_R_u += z_R_Top_Slope;
            x = x_left;
            r_L += R_slope_left;
            r_R_u += R_slope_top;
            g_L += G_slope_left;
            g_R_u += G_slope_top;
            b_L += B_slope_left;
            b_R_u += B_slope_top;
        }
        for(; y > y_bottom; y--) {
            Vertex3D p1 = new Vertex3D(x, y, z_L, Color.fromARGB(Color.makeARGB((int)Math.round(r_L),(int)Math.round(g_L),
                    (int)Math.round(b_L))));
            Vertex3D p2 = new Vertex3D(x_right, y, z_R_l, Color.fromARGB(Color.makeARGB((int)Math.round(r_R_l),(int)Math.round(g_R_l),
                    (int)Math.round(b_R_l))));
            render_bet_points(p1, p2, panel);
            x_left += left_slope;
            x_right += lower_right_slope;
            z_L += z_L_Slope;
            z_R_l += z_R_Lower_Slope;
            x = x_left;
            r_L += R_slope_left;
            g_L += G_slope_left;
            b_L += B_slope_left;
            r_R_l += R_slope_right;
            g_R_l += G_slope_right;
            b_R_l += B_slope_right;
        }
    }

    private void render2ndPolygon(int x_top, int y_top, int y_middle, int y_bottom, double right_slope,
                                  double upper_left_slope, double lower_left_slope, double R_slope_top, double G_slope_top, double B_slope_top,
                                  double R_slope_left, double G_slope_left, double B_slope_left, double R_slope_right, double G_slope_right, double B_slope_right,
                                  Vertex3D p_top, Vertex3D p_middle, Vertex3D p_bottom ,Drawable panel) {
        double x = x_top;
        double y = y_top;
        double x_left = x_top;
        double x_right = x_top;

        double r_L_u = p_top.getColor().getIntR();
        double g_L_u = p_top.getColor().getIntG();
        double b_L_u = p_top.getColor().getIntB();
        double r_R = p_top.getColor().getIntR();
        double g_R = p_top.getColor().getIntG();
        double b_R = p_top.getColor().getIntB();
        double r_L_l = p_middle.getColor().getIntR();
        double g_L_l = p_middle.getColor().getIntG();
        double b_L_l = p_middle.getColor().getIntB();

        // added
        double z_top = p_top.getIntZ();
        double z_middle = p_middle.getIntZ();
        double z_bottom = p_bottom.getIntZ();

        double delta_Y = y_top - y_bottom;
        double delta_y_lu = y_top - y_middle;
        double delta_y_ll = y_middle - y_bottom;

        double delta_Z_R = z_bottom - z_top;
        double delta_Z_L_T = z_middle - z_top;
        double delta_Z_L_L = z_bottom - z_middle;

        double z_R_Slope = delta_Z_R / delta_Y;
        double z_L_Top_Slope = delta_Z_L_T / delta_y_lu;
        double z_L_Lower_Slope = delta_Z_L_L/ delta_y_ll;

        double z_R = p_top.getIntZ();
        double z_L_u = p_top.getIntZ();
        double z_L_l = p_middle.getIntZ();

        for(; y > y_middle; y--) {
            Vertex3D p1 = new Vertex3D(x, y, z_L_u, Color.fromARGB(Color.makeARGB((int)Math.round(r_L_u),(int)Math.round(g_L_u),
                    (int)Math.round(b_L_u))));
            Vertex3D p2 = new Vertex3D(x_right, y, z_R, Color.fromARGB(Color.makeARGB((int)Math.round(r_R),(int)Math.round(g_R),
                    (int)Math.round(b_R))));
            render_bet_points(p1, p2, panel);
            x_left += upper_left_slope;
            x_right += right_slope;
            z_L_u += z_L_Top_Slope;
            z_R += z_R_Slope;
            x = x_left;
            r_L_u += R_slope_top;
            g_L_u += G_slope_top;
            b_L_u += B_slope_top;
            r_R += R_slope_right;
            g_R += G_slope_right;
            b_R += B_slope_right;
        }
        for(; y > y_bottom; y--) {
            Vertex3D p1 = new Vertex3D(x, y, z_L_l, Color.fromARGB(Color.makeARGB((int)Math.round(r_L_l),(int)Math.round(g_L_l),
                    (int)Math.round(b_L_l))));
            Vertex3D p2 = new Vertex3D(x_right, y, z_R, Color.fromARGB(Color.makeARGB((int)Math.round(r_R),(int)Math.round(g_R),
                    (int)Math.round(b_R))));
            render_bet_points(p1, p2, panel);
            x_left += lower_left_slope;
            x_right += right_slope;
            z_L_l += z_L_Lower_Slope;
            z_R += z_R_Slope;
            x = x_left;
            r_L_l += R_slope_left;
            g_L_l += G_slope_left;
            b_L_l += B_slope_left;
            r_R += R_slope_right;
            g_R += G_slope_right;
            b_R += B_slope_right;
        }
    }


    private void render_bet_points(Vertex3D p1, Vertex3D p2, Drawable drawable){
        double deltaX = p2.getIntX() - p1.getIntX();
        double deltaY = p2.getIntY() - p1.getIntY();
        double deltaZ = p2.getIntZ() - p1.getIntZ();// added
        double deltaR = p2.getColor().getIntR() - p1.getColor().getIntR();
        double deltaG = p2.getColor().getIntG() - p1.getColor().getIntG();
        double deltaB = p2.getColor().getIntB() - p1.getColor().getIntB();

        double slope = deltaY / deltaX;
        double z_slope = deltaZ / deltaX;// added
        double R_slope = deltaR / deltaX;
        double G_slope = deltaG / deltaX;
        double B_slope = deltaB / deltaX;

        double y = p1.getIntY();
        double z = p1.getZ();//added
        int x = p1.getIntX();
        double r = p1.getColor().getIntR();
        double g = p1.getColor().getIntG();
        double b = p1.getColor().getIntB();

        while(x <= p2.getIntX()){
            if((int)Math.round(y) >= 0 && (int)Math.round(y) < 650 && (int)Math.round(x) >= 0 && (int)Math.round(x) < 650){
                    if(z >= SimpInterpreter.z_buffer[(int)Math.round(x)][(int)Math.round(y)]) {
                        drawable.setPixel(x, (int) Math.round(y), 0.0, Color.makeARGB((int) Math.round(r), (int) Math.round(g), (int) Math.round(b)));
                        SimpInterpreter.z_buffer[(int)Math.round(x)][(int)Math.round(y)] = z;
                    }
            }
            x++;
            y += slope;
            z += z_slope;
            r += R_slope;
            g += G_slope;
            b += B_slope;
        }
    }
    public static PolygonRenderer make() {
    	return new ZbufferPolygonRenderer();
    	}
}
*/

