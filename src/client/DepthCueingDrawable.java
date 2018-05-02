package client;

import windowing.drawable.Drawable;
import windowing.graphics.*;

public class DepthCueingDrawable implements Drawable{
	
	private double max_depth;
	private double min_depth;
	private Color color;
	private Drawable panel;
	
	public DepthCueingDrawable(Drawable panel, int max_z, int min_z, Color color) {
		this.panel = panel;
		this.max_depth = max_z;
		this.min_depth = min_z;
		this.color = color;
	}

	@Override
	public void fill(int color, double z) {
		for(int i = 0; i < panel.getWidth(); i++) {
			for(int j = 0; j < panel.getHeight(); j++) {
				panel.setPixel(i, j, z, color);
			}
		}
	}

	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		// TODO Auto-generated method stub
		panel.setPixel(x, y, z, argbColor);
	}

	@Override
	public int getPixel(int x, int y) {
		// TODO Auto-generated method stub
		return (int)Math.round(color.asARGB());
	}

	@Override
	public double getZValue(int x, int y) {
		// TODO Auto-generated method stub
		return panel.getZValue(x, y);
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return panel.getWidth();
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return panel.getHeight();
	}
}

	