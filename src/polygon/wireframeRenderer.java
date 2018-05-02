package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import line.LerpLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;

public class wireframeRenderer implements PolygonRenderer {
	
	Polygon polygon;
	Drawable panel;
	private LineRenderer render = LerpLineRenderer.make(); 
	
	// constructor
	private wireframeRenderer() {
	}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
		
		this.polygon = polygon;
		this.panel = panel;
		
		Vertex3D p1 = polygon.get(0);
		Vertex3D p2 = polygon.get(1);
		Vertex3D p3 = polygon.get(2);
		
		//System.out.println("printing!");
		//System.out.println("The first vertex in wireframeRenderer is : " + p1);
		render.drawLine(p1, p2, panel);
		render.drawLine(p2, p3, panel);
		render.drawLine(p1, p3, panel);
	}
		
	public static wireframeRenderer make() {		
		return new wireframeRenderer();
	}
}
