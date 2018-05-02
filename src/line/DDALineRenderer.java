package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import java.lang.Math;


public class DDALineRenderer implements LineRenderer {
	
	// constructor
	private DDALineRenderer() {
		
	}
	
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		
		double disX = p2.getIntX() - p1.getIntX();		// distance from p1 to p2 in x direction
		double disY = p2.getIntY() - p1.getIntY();		// distance from p1 to p2 in y direction
		double slope = disY / disX;		// slope of line
		int argbColor = p1.getColor().asARGB();		// color of pixel(white)
		int x = p1.getIntX();
		double y = p1.getIntY();
		int x1 = p2.getIntX();
		while(x <= x1) {
			
			drawable.setPixel(x, (int)Math.round(y), 0.0, argbColor);
			x ++;
			y += slope;
		}
	}
	
	public static LineRenderer make() {
		
		return new AnyOctantLineRenderer(new DDALineRenderer());
	}

}
