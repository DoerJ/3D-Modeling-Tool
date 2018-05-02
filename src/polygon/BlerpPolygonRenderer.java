package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;

public class BlerpPolygonRenderer implements PolygonRenderer{
	
	public static final int NUM_VERTICES = 3;
	private Polygon polygon;
	private Drawable panel;
	private Vertex3D[] points;
	private Color color;
	private Vertex3D[] check_color;
	
	public BlerpPolygonRenderer() {
		check_color = new Vertex3D[100000];
		points = new Vertex3D[3];
	}
	
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
		
		this.polygon = polygon;
		this.panel = panel;
		Chain left_chain = polygon.leftChain();
		Chain right_chain = polygon.rightChain();
		
		// check if triangle has a side parallel to x axis
		Vertex3D[] points = new Vertex3D[3];
		points[0] = polygon.get(0);
		points[1] = polygon.get(1);
		points[2] = polygon.get(2);
		
		if(left_chain.get(0).getIntY() != left_chain.get(1).getIntY()) {	
			
			fillPolygon(left_chain, right_chain, panel);
		}
		
		else if(left_chain.get(0).getIntY() == left_chain.get(1).getIntY()) {
			
			renderSpecialTriangle(polygon, panel);
		}	
	}
	
	private void renderSpecialTriangle(Polygon polygon, Drawable panel) {
		
		Chain right_chain = polygon.rightChain();
		Chain left_chain = polygon.leftChain();
		Vertex3D p_left;
		Vertex3D p_right;
		Vertex3D p_bottom;
		
		if(polygon.get(0).getX() == polygon.get(1).getX() || 
				polygon.get(0).getX() == polygon.get(2).getX() ||
				polygon.get(1).getX() == polygon.get(2).getX()) {
			
			// if triangle is a perpendicular triangle
			p_left = left_chain.get(1);
			p_right = right_chain.get(0);		
			p_bottom = left_chain.get(2);	
		}
		else {
			
			p_left = left_chain.get(1);			// top_left vertex
			p_right = left_chain.get(0);		// top_right vertex
			p_bottom = right_chain.get(1);		// bottom_vertex
		}
		
		// now we have p_left, p_right, and p_bottom
		// check if same vertices have been stored
		int i = 0;
		while(check_color[i] != null) {
			if(p_left.getIntX() == check_color[i].getIntX()
					 && p_left.getIntY() == check_color[i].getIntY()) {
				p_left = new Vertex3D(p_left.getX(), p_left.getY(), 0, check_color[i].getColor());
			}
			if(p_bottom.getIntX() == check_color[i].getIntX()
					 && p_bottom.getIntY() == check_color[i].getIntY()) {
				p_bottom = new Vertex3D(p_bottom.getX(), p_bottom.getY(), 0, check_color[i].getColor());
			}
			if(p_right.getIntX() == check_color[i].getIntX()
					 && p_right.getIntY() == check_color[i].getIntY()) {
				p_right = new Vertex3D(p_right.getX(), p_right.getY(), 0, check_color[i].getColor());
			}	
			i ++;
		}
		// now index i indicate the null space in array
		check_color[i] = p_left;
		check_color[i + 1] = p_bottom;
		check_color[i + 2] = p_right;
		
		// get color
		Color left_color = p_left.getColor();
		Color bottom_color = p_bottom.getColor();
		Color right_color = p_right.getColor();
		
		
		// calculate right_slope and left_slope
		double x_left = p_left.getIntX();
		double x_right = p_right.getIntX();
		double x_bottom = p_bottom.getIntX();
		double y_top = p_left.getIntY();
		double y_bottom = p_bottom.getIntY();
		double delta_x_l = x_bottom - x_left;
		double delta_x_r = x_bottom - x_right;
		double delta_y = y_top - y_bottom;
		double slope_left = delta_x_l / delta_y;
		double slope_right = delta_x_r / delta_y;
		
		double x = x_left;
		
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
		
		double y = y_top;
		double left = x_left;
		double right = x_right;
		for(; y > y_bottom; y--) {
			
			double disX = x_right - x_left;
			double disX_r = x_right_r - x_left_r;
			double disX_g = x_right_g - x_left_g;
			double disX_b = x_right_b - x_left_b;
			
			double r_slope = disX_r / disX;
			double g_slope = disX_g / disX;
			double b_slope = disX_b / disX;
			
			for(; x < right; x++) {
				
				Color argb = new Color(r, g, b);
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
				
				r += r_slope;
				g += g_slope;
				b += b_slope;
			}
			
			left += slope_left;
			right += slope_right;
			x = left;
			
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
			
			// check if same vertices have been stored
			int i = 0;
			while(check_color[i] != null) {
				if(p_top.getIntX() == check_color[i].getIntX()
						 && p_top.getIntY() == check_color[i].getIntY()) {
					p_top = new Vertex3D(p_top.getX(), p_top.getY(), 0, check_color[i].getColor());
				}
				if(p_bottom.getIntX() == check_color[i].getIntX()
						 && p_bottom.getIntY() == check_color[i].getIntY()) {
					p_bottom = new Vertex3D(p_bottom.getX(), p_bottom.getY(), 0, check_color[i].getColor());
				}
				if(p_middle.getIntX() == check_color[i].getIntX()
						 && p_middle.getIntY() == check_color[i].getIntY()) {
					p_middle = new Vertex3D(p_middle.getX(), p_middle.getY(), 0, check_color[i].getColor());
				}	
				i ++;
			}
			// now index i indicate the null space in array
			check_color[i] = p_top;
			check_color[i + 1] = p_bottom;
			check_color[i + 2] = p_middle;
			
			// get color
			Color top_color = p_top.getColor();
			Color bottom_color = p_bottom.getColor();
			Color middle_color = p_middle.getColor();
			
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
			
			// -----------------------------------------------------------------CHECK!
			renderLeftPolygon(x_top, y_top, y_middle, y_bottom, left_slope, 
					first_right_slope, second_right_slope, panel, top_color, 
					bottom_color, middle_color);
		}
		// right slope is unchanged
		else {
			
			// left_chain contains 3 vertices, right_chain contains 2 vertices
			Vertex3D p_top = left_chain.get(0);		// top point
			Vertex3D p_middle = left_chain.get(1);		// middle point
			Vertex3D p_bottom = right_chain.get(1);		// bottom point
			
			// check if same vertices have been stored
			int i = 0;
			while(check_color[i] != null) {
				if(p_top.getIntX() == check_color[i].getIntX()
						 && p_top.getIntY() == check_color[i].getIntY()) {
					p_top = new Vertex3D(p_top.getX(), p_top.getY(), 0, check_color[i].getColor());
				}
				if(p_bottom.getIntX() == check_color[i].getIntX()
						 && p_bottom.getIntY() == check_color[i].getIntY()) {
					p_bottom = new Vertex3D(p_bottom.getX(), p_bottom.getY(), 0, check_color[i].getColor());
				}
				if(p_middle.getIntX() == check_color[i].getIntX()
						 && p_middle.getIntY() == check_color[i].getIntY()) {
					p_middle = new Vertex3D(p_middle.getX(), p_middle.getY(), 0, check_color[i].getColor());
				}	
				i ++;
			}
			
			// now index i indicate the null space in array
			check_color[i] = p_top;
			check_color[i + 1] = p_bottom;
			check_color[i + 2] = p_middle;
			
			// get color
			Color top_color = p_top.getColor();
			Color bottom_color = p_bottom.getColor();
			Color middle_color = p_middle.getColor();
			
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
			
			renderRightPolygon(x_top, y_top, y_middle, y_bottom, right_slope, 
					first_left_slope, second_left_slope, panel, top_color, bottom_color, middle_color);
		}
		
	}
	
	private void renderLeftPolygon(int x_top, int y_top, int y_middle, int y_bottom, double left_slope, 
			double upper_right_slope, double lower_right_slope, Drawable panel, Color top_color, Color bottom_color, 
			Color middle_color) {
		
		double x = x_top;
		
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
			
			for(; x < x_right; x++) {
				
				Color argb = new Color(r, g, b);
				// set pixel
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
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
			
			for(; x < x_right; x++) {
				
				Color argb = new Color(r, g, b);
				// set pixel
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
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
		}
	}
	
	private void renderRightPolygon(int x_top, int y_top, int y_middle, int y_bottom, double right_slope, 
			double upper_left_slope, double lower_left_slope, Drawable panel, Color top_color, Color bottom_color, 
			Color middle_color) {
		
		double x = x_top;
		
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
			
			for(; x < x_right; x++) {
				
				Color argb = new Color(r, g, b);
				// set pixels
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
				
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
			
			for(; x < x_right; x++) {
				
				Color argb = new Color(r, g, b);
				// set pixels
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, argb.asARGB());
				
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
		}	
	}
	
	public static PolygonRenderer make() {
		
		return new BlerpPolygonRenderer();
	}	
}