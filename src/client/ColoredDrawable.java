package client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;

public class ColoredDrawable extends DrawableDecorator{
	
	private static int color;
	
	public ColoredDrawable(Drawable panel, int color) {
		
		super(panel);
	}
	
	@Override
	public void fill(int color1, double z) {

		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				setPixel(x, y, z, color);
			}
		}
	}
}
