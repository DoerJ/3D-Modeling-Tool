package client.testPages;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.PolygonRenderer;
import polygon.Polygon;

public class StarburstPolygonTest {
		
		private final Drawable panel;
		private final PolygonRenderer polygonRenderer;
		Vertex3D center;
		private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;
		private static final int NUM_TRIANGLES = 90;
		
		public StarburstPolygonTest(Drawable panel, PolygonRenderer polygonRenderer) {
			
			this.panel = panel;
			this.polygonRenderer = polygonRenderer;
			
			// get the center 
			makeCenter();
			// draw and fill polygons
			render();
		}
		
		// get center of circle (return a Vertex3D)
		private void makeCenter() {
			int centerX = panel.getWidth() / 2;
			int centerY = panel.getHeight() / 2;
			center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
		}
		
		private void render() {		
			double radius = computeRadius();
			double angleDifference = (2.0 * Math.PI) / NUM_TRIANGLES;
			double angle = 0.0;
			
			for(int ray = 0; ray < NUM_TRIANGLES; ray++) {
				Vertex3D first_radialPoint = radialPoint(radius, angle);
				angle = angle + angleDifference;
				Vertex3D second_radialPoint = radialPoint(radius, angle);
				// we have three Vertex3D now, form a polygon project
				Polygon polygon = Polygon.make(first_radialPoint, second_radialPoint, center);
				polygonRenderer.drawPolygon(polygon, panel);
			}
		}
		
		private double computeRadius() {
			int width = panel.getWidth();
			int height = panel.getHeight();
			
			int minDimension = width < height ? width : height;
			
			return (minDimension / 2.0) * FRACTION_OF_PANEL_FOR_DRAWING;
		}
		
		private Vertex3D radialPoint(double radius, double angle) {
			double x = center.getX() + radius * Math.cos(angle);	// the end-point on center
			double y = center.getY() + radius * Math.sin(angle);	// the end-point on circular edge
			return new Vertex3D(x, y, 0, Color.WHITE);		// we have two end-points to draw a line using line renderer
		}
}
