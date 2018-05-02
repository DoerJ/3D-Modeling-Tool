package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer implements LineRenderer{
	
	private BresenhamLineRenderer() {
		
	}
	
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		
		int argbColor = p1.getColor().asARGB();		// color of pixel(white)
		int slope_num = 2 * (p2.getIntY() - p1.getIntY());
		int initial_x = p1.getIntX();
		int initial_y = p1.getIntY();
		int delta_x = p2.getIntX() - p1.getIntX();
		int error = slope_num - delta_x;
		int k = slope_num - 2 * delta_x;
		for(; initial_x <= p2.getIntX(); initial_x++) {
			
			drawable.setPixel(initial_x, initial_y, 0, argbColor);
			if(error >= 0) {
				
				error += k;
				initial_y ++;
			}
			else {
				
				error += slope_num;
			}
		}
		/*
		int slope_den = p2.getIntX() - p1.getIntX();
		int slope_num = p2.getIntY() - p1.getIntY();
		int y_int = p1.getIntY();
		int y_num = 0;
		int y_den = slope_den;
		int x = p1.getIntX();
		int x1 = p2.getIntX();
		int argbColor = p1.getColor().asARGB();		// color of pixel(white)
		while(x <= x1) {
			
			// both integer and decimal part of y are reserved, thus the better the accuracy 
			drawable.setPixel(x, (int)((y_int + y_num / y_den) + 0.5), 0.0, argbColor);
			x ++;
			y_num = y_num + slope_num;
			if(y_num > y_den) {
				
				y_num = y_num - y_den;
				y_int ++;
			}
		}
		*/
		
	}
	
	public static LineRenderer make() {
		
		return new AnyOctantLineRenderer(new BresenhamLineRenderer());
	}

}
