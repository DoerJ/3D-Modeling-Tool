package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;

public class TriangleFilledPolygonRenderer implements PolygonRenderer{
	
	public static final int NUM_VERTICES = 3;
	Polygon polygon;
	Drawable panel;
	Vertex3D[] points = new Vertex3D[3];
	Color color;
	
	private TriangleFilledPolygonRenderer() {
	}
	
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
		
		this.polygon = polygon;
		this.panel = panel;
		this.color = polygon.get(0).getColor();
		Chain left_chain = polygon.leftChain();
		Chain right_chain = polygon.rightChain();
		
		// check if triangle has a side parallel to x axis
		Vertex3D[] points = new Vertex3D[3];
		points[0] = polygon.get(0);
		points[1] = polygon.get(1);
		points[2] = polygon.get(2);
		//System.out.println(points[0].toString());
		//System.out.println(points[1].toString());
		//System.out.println(points[2].toString());
		int y1 = points[0].getIntY();
		int y2 = points[1].getIntY();
		int y3 = points[2].getIntY();
		
		if(left_chain.get(0).getIntY() != left_chain.get(1).getIntY()) {	
			
			fillPolygon(left_chain, right_chain, panel);
		}
		
		else if(left_chain.get(0).getIntY() == left_chain.get(1).getIntY()) {
			
			renderSpecialTriangle(polygon, panel);
		}	
	}
	
	private void renderSpecialTriangle(Polygon polygon, Drawable panel) {
		
		int random_color = color.random().asARGB();
		Chain right_chain = polygon.rightChain();
		Chain left_chain = polygon.leftChain();
		Vertex3D p_left;
		Vertex3D p_right;
		Vertex3D p_bottom;
		
		if(polygon.get(0).getX() == polygon.get(1).getX() || 
				polygon.get(0).getX() == polygon.get(2).getX() ||
				polygon.get(1).getX() == polygon.get(2).getX()) {
			
			// if triangle is a perpendicular triangle
			p_left = left_chain.get(0);
			p_right = left_chain.get(1);		
			p_bottom = left_chain.get(2);	
		}
		else {
			
			p_left = left_chain.get(1);			// top_left vertex
			p_right = left_chain.get(0);		// top_right vertex
			p_bottom = left_chain.get(2);		// bottom_vertex
		}
		
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
		double y = y_top;
		double left = x_left;
		double right = x_right;
		for(; y > y_bottom; y--) {
			for(; x < right; x++) {
				
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, color.asARGB());
			}
			
			left += slope_left;
			right += slope_right;
			x = left;
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
			int random_color = color.random().asARGB();
			
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
					first_right_slope, second_right_slope, panel, random_color);
		}
		// right slope is unchanged
		else {
			
			// left_chain contains 3 vertices, right_chain contains 2 vertices
			Vertex3D p_top = left_chain.get(0);		// top point
			Vertex3D p_middle = left_chain.get(1);		// middle point
			Vertex3D p_bottom = right_chain.get(1);		// bottom point
			
			// get color
			int argb = color.random().asARGB();
			
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
					first_left_slope, second_left_slope, panel, argb);
		}
		
	}
	
	private void renderLeftPolygon(int x_top, int y_top, int y_middle, int y_bottom, double left_slope, 
			double upper_right_slope, double lower_right_slope, Drawable panel, int random_color) {
		
		double x = x_top;
		double x_left = x_top;
		double x_right = x_top;
		double y = y_top;
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			for(; x < x_right; x++) {
				
				// set pixel
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, color.asARGB());
			}
			x_left += left_slope;
			x_right += upper_right_slope;
			x = x_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			for(; x < x_right; x++) {
				
				// set pixel
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, color.asARGB());
			}
			x_left += left_slope;
			x_right += lower_right_slope;
			x = x_left;
		}
	}
	
	private void renderRightPolygon(int x_top, int y_top, int y_middle, int y_bottom, double right_slope, 
			double upper_left_slope, double lower_left_slope, Drawable panel, int random_color) {
		
		double x = x_top;
		double y = y_top;
		double x_left = x_top;
		double x_right = x_top;
		
		// fill the upper part of triangle
		for(; y > y_middle; y--) {
			for(; x < x_right; x++) {
				
				// set pixels
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, color.asARGB());
			}
			x_left += upper_left_slope;
			x_right += right_slope;
			x = x_left;
		}
		
		// fill the lower part of triangle
		for(; y > y_bottom; y--) {
			for(; x < x_right; x++) {
				
				// set pixels
				panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, color.asARGB());
			}
			x_left += lower_left_slope;
			x_right += right_slope;
			x = x_left;
		}	
	}
	
	public static PolygonRenderer make() {
		
		return new TriangleFilledPolygonRenderer();
	}	
}