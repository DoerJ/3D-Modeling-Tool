package shading;

import windowing.graphics.*;
import geometry.*;

public class Lighting {
	
	public static Color light_intensity;
	public static double attenuation_a;
	public static double attenuation_b;
	public static Vertex3D light_location;
	
	public Lighting(Color light, double a, double b, Vertex3D light_location) {
		this.light_intensity = light;
		this.attenuation_a = a;
		this.attenuation_b = b;
		this.light_location = light_location;
	}
}
