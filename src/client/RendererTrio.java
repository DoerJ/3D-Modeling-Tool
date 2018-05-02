package client;
import polygon.PolygonRenderer;
import line.BresenhamLineRenderer;
import polygon.*;
import line.LineRenderer;

public class RendererTrio {
	
	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireFrameRenderer;
	private PolygonRenderer phongRenderer;
	
	
	public RendererTrio() {
		this.lineRenderer = BresenhamLineRenderer.make();
		this.filledRenderer = ZbufferPolygonRenderer.make();
		//this.filledRenderer = BlerpPolygonRenderer.make();
		this.phongRenderer = BlerpPolygonRenderer.make();
		this.wireFrameRenderer = wireframeRenderer.make();
	}
	
	public LineRenderer getLineRenderer() {	
		return lineRenderer;
	}
	
	public PolygonRenderer getFilledRenderer() {
		return filledRenderer;
	}
	
	public PolygonRenderer getWireframeRenderer() {
		return wireFrameRenderer;
	}
	public PolygonRenderer getPhongRenderer() {
		return phongRenderer;
	}
}
