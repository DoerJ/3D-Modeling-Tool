package polygon;

import windowing.graphics.Color;
import geometry.Vertex3D;
import line.LerpLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;
import polygon.PolygonRenderer;
import polygon.Chain;
import polygon.Polygon;

public class WireframePolygonRenderer implements PolygonRenderer {
	
	Polygon polygon;
	Drawable panel;
	private PolygonRenderer render = BlerpPolygonRenderer.make();
	
	// constructor
	private WireframePolygonRenderer() {
	}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
		
		this.polygon = polygon;
		this.panel = panel;
		
		render.drawPolygon(polygon, panel);
	}
		
	public static WireframePolygonRenderer make() {		
		return new WireframePolygonRenderer();
	}
}
