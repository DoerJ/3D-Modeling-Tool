package client.testPages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ParallelogramTest {
	
	private static final int NUM_LINES = 50;
	private final Drawable panel;
	private final LineRenderer renderer;
	
	public ParallelogramTest(Drawable panel, LineRenderer renderer) {
		
		this.panel = panel;
		this.renderer = renderer;
		
		renderLeftParallelogram();
		renderRightParallelogram();
	}
	
	// render the parallelogram on the left
	public void renderLeftParallelogram() {
		
		double line_counter = 0.0;
		
		for(; line_counter < NUM_LINES; line_counter++) {
			
			Vertex3D first_endpoint = StartEndpoint(20.0, 220.0 - line_counter);
			Vertex3D second_endpoint = FinishEndpoint(150.0, 150.0 - line_counter);
			renderer.drawLine(first_endpoint, second_endpoint, panel);	
		}
	}
	
	// render the parallelogram on the right 
	public void renderRightParallelogram() {
		
		double line_counter = 0.0;
		for(; line_counter < NUM_LINES; line_counter++) {
			
			Vertex3D first_endpoint = StartEndpoint(160.0 + line_counter, 30.0);
			Vertex3D second_endpoint = FinishEndpoint(240.0 + line_counter, 260.0);
			renderer.drawLine(first_endpoint, second_endpoint, panel);
		}
	}
	
	private Vertex3D StartEndpoint(double x_start, double y_start) {
		
		return new Vertex3D(x_start, y_start, 0, Color.WHITE);
	}
	
	private Vertex3D FinishEndpoint(double x_end, double y_end) {
		
		return new Vertex3D(x_end, y_end, 0, Color.WHITE);
	}

}
