package shading;
// final submission
import polygon.Polygon;
import geometry.*;
import windowing.graphics.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class FlatShading implements FaceShader{
	
	private Polygon flatPolygon;
	// the attenuation constants
	private double attenuation_a[];		// # of light sources
	private double attenuation_b[];		// # of light sources
	private double normal_vector[];
	private double view_vector[];
	// the specular exponent
	private double specular_exp;
	private double distance[];			// # of light sources
	private double specular_reflection;
	// the # of light sources
	private int num_light;
	
	private Vertex3D firstVertex;
	private Vertex3D secondVertex;
	private Vertex3D thirdVertex;
	private Vertex3D central_point;
	
	private Color ambient_light;
	private Color diffuse_reflection;
	
	private ArrayList<Vertex3D> light_location;		// # of light sources
	private ArrayList<Color> light_intensity;		// # of light sources
	// the reflection vector (Ri)
	private ArrayList<double[]> reflection;		// # of light sources
	// the vector to light (Li)
	private ArrayList<double[]> vector_to_light;		// # of light sources
	private ArrayList<Lighting> lightBulb;			// # of light sources
	
	public FlatShading(Polygon polygon, ArrayList<Lighting> lightBulb, double specular_reflection, double specular_exp, 
			Color objectColor) {
		this.flatPolygon = polygon;
		this.ambient_light = polygon.get(0).getColor();
		// initialize lighting attributes
		//this.light_intensity = lightBulb.get(0).light_intensity;
		//this.attenuation_a = lightBulb.get(0).attenuation_a;
		//this.attenuation_b = lightBulb.get(0).attenuation_b;
		//this.light_location = lightBulb.get(0).light_location;
		this.specular_reflection = specular_reflection;
		this.specular_exp = specular_exp;
		this.diffuse_reflection = objectColor;
		this.num_light = lightBulb.size();
		this.lightBulb = lightBulb;
		normal_vector = new double[3];
		view_vector = new double[3];
		//vector_to_light = new double[3];
		//reflection = new double[3];	
		
		distance = new double[num_light];
		light_location = new ArrayList<Vertex3D>();
		light_intensity = new ArrayList<Color>();
		vector_to_light = new ArrayList<double[]>();
		reflection = new ArrayList<double[]>();
		attenuation_a = new double[num_light];
		attenuation_b = new double[num_light];
	}
	@Override
	public Polygon shade(Polygon polygon) {
		Polygon temp_polygon = polygon;
		// get the three vertices of polygon
		firstVertex = polygon.get(1);
		secondVertex = polygon.get(0);
		thirdVertex = polygon.get(2);
		
		// get the middle point of the polygon, the point used in lighting calculation
		central_point = getMiddlePoint(firstVertex, secondVertex, thirdVertex);
		//System.out.println("the central point of surface is: " + central_point.toString());
		
		// get the normal vector of the surface
		normal_vector = getNormalVector(firstVertex, secondVertex, thirdVertex);
		
		// get the view vector 
		view_vector = getViewVector();
		
		// get the L
		vector_to_light = getLvector();
		
		// get the R
		reflection = getRvector();
		
		// get the distance between light source and the surface
		distance = getDistance();
		
		// lighting calculation
		flatPolygon = lightingCal(firstVertex, secondVertex, thirdVertex);
		
		//System.out.println("The shade of surface is: " + flatPolygon.get(0).getColor().toString());
		//System.out.println("The shade of surface is: " + flatPolygon.get(0).getColor().toString());
		//System.out.println("return flatPolygon!");
		return flatPolygon;
	}
	// get the distance between light source and surface
	private double[] getDistance() {
		double all_distance[] = new double[num_light];
		double distance = 0;
		double distant[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		distant[0] = lightBulb.get(i).light_location.getX() - central_point.getX();
		distant[1] = lightBulb.get(i).light_location.getY() - central_point.getY();
		distant[2] = lightBulb.get(i).light_location.getZ() - central_point.getZ();
		distance = Math.sqrt(distant[0]*distant[0] + distant[1]*distant[1] + distant[2]*distant[2]);
		//System.out.println("distance is: " + distance);
		all_distance[i] = distance;
		}
		return all_distance;
	}
	// proceed lighting calculation
	private Polygon lightingCal(Vertex3D v1, Vertex3D v2, Vertex3D v3) {
		
		// initialize the light_intensity
		for(int i = 0; i < num_light; i++) {
			light_intensity.add(lightBulb.get(i).light_intensity);
		}
		// store components of light intensity
		double intensity_r[] = new double[num_light];
		double intensity_g[] = new double[num_light];
		double intensity_b[] = new double[num_light];
		
		for(int k = 0; k < num_light; k++) {
			intensity_r[k] = light_intensity.get(k).getR();
			intensity_g[k] = light_intensity.get(k).getG();
			intensity_b[k] = light_intensity.get(k).getB();
		}
		
		// initialize attenuation constants
		for(int j = 0; j < num_light; j++) {
			attenuation_a[j] = lightBulb.get(j).attenuation_a;
			attenuation_b[j] = lightBulb.get(j).attenuation_b;
		}
		
		Polygon shade_polygon;
		// ambient light(RGB)
		double ambient_r = ambient_light.getR();
		double ambient_g = ambient_light.getG();
		double ambient_b = ambient_light.getB();
		// light intensity(RGB)
		/*
		double intensity_r = light_intensity.getR();
		double intensity_g = light_intensity.getG();
		double intensity_b = light_intensity.getB();
		*/
		// diffuse reflection(RGB)
		double diffuse_r = diffuse_reflection.getR();
		double diffuse_g = diffuse_reflection.getG();
		double diffuse_b = diffuse_reflection.getB();
		
		double light_r = RGBlightingCalculation(ambient_r, intensity_r, diffuse_r);
		double light_g = RGBlightingCalculation(ambient_g, intensity_g, diffuse_g);
		double light_b = RGBlightingCalculation(ambient_b, intensity_b, diffuse_b);
		
		//System.out.println("the color of light_r is: " + light_r);
		Color shade_color = new Color(light_r, light_g, light_b);
		v1 = new Vertex3D(v1.getX(), v1.getY(), v1.getZ(), shade_color);
		v2 = new Vertex3D(v2.getX(), v2.getY(), v2.getZ(), shade_color);
		v3 = new Vertex3D(v3.getX(), v3.getY(), v3.getZ(), shade_color);
		shade_polygon = Polygon.make(v3, v1, v2);
		//System.out.println("the color of the light is: " + shade_polygon.get(0).getColor().getR());
		return shade_polygon;
	}
	// insert RGB component into lighting equation respectively
	private double RGBlightingCalculation(double ambient, double[] intensity, double diffuse) {
		double light_component;
		// first component of lighting calculation
		double first_component = ambient * diffuse;
		//System.out.println("the ambient light is: " + ambient);
		
		
		double sum = 0;
		// second component of lighting calculation
		// N.L (the dot product of normal vector and vector_to_light)
		double v_to_light[] = new double[3];
		double ref[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		
		v_to_light = vector_to_light.get(i);
		ref = reflection.get(i);
		double NL = (-1) * normal_vector[0] * v_to_light[0] - normal_vector[1] * v_to_light[1] - 
				normal_vector[2] * v_to_light[2];
		// VR (the dot product of view vector and reflection vector)
		double VR = view_vector[0] * ref[0] + view_vector[1] * ref[1] + 
				view_vector[2] * ref[2];
		// fatti
		double fatti = 1/(attenuation_a[i] + attenuation_b[i] * distance[i]);
		// sum ---------------------- TO BE CHECK!
		sum += intensity[i] * fatti * (diffuse * NL + specular_reflection * Math.pow(VR, specular_exp));
		}
		light_component = first_component + sum;
		return light_component;
	}
	// calculate the reflection vector
	private ArrayList<double[]> getRvector() {
		ArrayList<double[]> all_R = new ArrayList<double[]>();
		double R[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		// get the dot product of normal vector and incident vector
		double v_to_light[] = vector_to_light.get(i);
		double dot_product = v_to_light[0] * normal_vector[0] + v_to_light[1] * normal_vector[1] + 
				v_to_light[2] * normal_vector[2];
		R[0] = v_to_light[0] - 2 * dot_product * normal_vector[0];
		R[1] = v_to_light[1] - 2 * dot_product * normal_vector[1];
		R[2] = v_to_light[2] - 2 * dot_product * normal_vector[2];
		// get the unit vector
		double R_length = Math.sqrt(R[0]*R[0] + R[1]*R[1] + R[2]*R[2]);
		R[0] = R[0]/R_length;
		R[1] = R[1]/R_length;
		R[2] = R[2]/R_length;
		all_R.add(R);
		}
		return all_R;
	}
	// calculate the vector to light source
	private ArrayList<double[]> getLvector() {
		ArrayList<double[]> all_L = new ArrayList<double[]>();
		double L[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		L[0] = lightBulb.get(i).light_location.getX() - central_point.getX();
		L[1] = lightBulb.get(i).light_location.getY() - central_point.getY();
		L[2] = lightBulb.get(i).light_location.getZ() - central_point.getZ();
		// get the unit vector
		double L_length = Math.sqrt(L[0]*L[0] + L[1]*L[1] + L[2]*L[2]);
		L[0] = L[0]/L_length;
		L[1] = L[1]/L_length;
		L[2] = L[2]/L_length;
		all_L.add(L);
		}
		return all_L;
	}
	// calculate the unit view vector
	private double[] getViewVector() {
		double view[] = new double[3];
		// view point in camera space: (0, 0)
		view[0] = central_point.getX();
		view[1] = central_point.getY();
		view[2] = central_point.getZ();
		double view_length = Math.sqrt(view[0]*view[0] + view[1]*view[1] + view[2]*view[2]);
		// get the unit view vector
		view[0] = view[0]/view_length;
		view[1] = view[1]/view_length;
		view[2] = view[2]/view_length;
		return view;
	}
	// calculate the unit normal vector of surface (polygon)
	private double[] getNormalVector(Vertex3D v1, Vertex3D v2, Vertex3D v3) {
		double normal[] = new double[3];
		double cartesian1[] = new double[3];
		double cartesian2[] = new double[3];
		// v1v2
		cartesian1[0] = v2.getX() - v1.getX();
		cartesian1[1] = v2.getY() - v1.getY();
		cartesian1[2] = v2.getZ() - v1.getZ();
		// v1v3
		cartesian2[0] = v3.getX() - v1.getX();
		cartesian2[1] = v3.getY() - v1.getY();
		cartesian2[2] = v3.getZ() - v1.getZ();
		// get cross product of v1v2 and v1v3
		normal[0] = cartesian1[1] * cartesian2[2] - cartesian1[2] * cartesian2[1];
		normal[1] = cartesian1[2] * cartesian2[0] - cartesian1[0] * cartesian2[2];
		normal[2] = cartesian1[0] * cartesian2[1] - cartesian1[1] * cartesian2[0];
		// get the unit normal vector
		double normal_length = Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
		normal[0] = normal[0]/normal_length;
		normal[1] = normal[1]/normal_length;
		normal[2] = normal[2]/normal_length;
		return normal;
	}
	// calculate the middle point of polygon
	private Vertex3D getMiddlePoint(Vertex3D v1, Vertex3D v2, Vertex3D v3) {
		Vertex3D central_point;
		// get the middle point of each of the side
		double central_x = (v1.getX() + v2.getX() + v3.getX())/3;
		double central_y = (v1.getY() + v2.getY() + v3.getY())/3;
		double central_z = (v1.getZ() + v2.getZ() + v3.getZ())/3;
		central_point = new Vertex3D(central_x, central_y, central_z, v1.getColor());
		return central_point;
	}
}
