package shading;

import polygon.Polygon;
import geometry.*;
import windowing.graphics.*;
import java.lang.Math;
import java.util.ArrayList;
public class GouraudShading implements VertexShader{
	
	Polygon gouraudPolygon;
	
	private double normal_vector[];
	private double view_vector[];
	// the specular exponent
	private double specular_exp;
	private double specular_reflection;
	private int num_light;		// # of light sources
	
	private Vertex3D firstVertex;
	private Vertex3D secondVertex;
	private Vertex3D thirdVertex;
	private Vertex3D central_point;
	private Vertex3D gouraudVertex;
	
	private Color ambient_light;
	private Color diffuse_reflection;
	
	private ArrayList<Vertex3D> light_location;		// # of light sources
	private ArrayList<Color> light_i;		// # of light sources
	// the reflection vector (Ri)
	private ArrayList<double[]> reflection;		// # of light sources
	// the vector to light (Li)
	private ArrayList<double[]> vector_to_light;		// # of light sources
	private ArrayList<Lighting> lightBulb;			// # of light sources
	private double distance[];				// # of light sources
	// the attenuation constants
	private double attenuation_a[];		// # of light sources
	private double attenuation_b[];		// # of light sources
	
	public GouraudShading(Polygon polygon, ArrayList<Lighting> lightBulb, double specular_reflection, double specular_exp, 
			Color objectColor) {
		this.gouraudPolygon = polygon;
		//this.attenuation_a = lightBulb.attenuation_a;
		//this.attenuation_b = lightBulb.attenuation_b;
		//this.light_intensity = lightBulb.light_intensity;
		//this.light_location = lightBulb.light_location;
		this.specular_reflection = specular_reflection;
		this.specular_exp = specular_exp;
		this.diffuse_reflection = objectColor;
		this.lightBulb = lightBulb;
		this.num_light = lightBulb.size();
		normal_vector = new double[3];
		view_vector = new double[3];
		
		light_location = new ArrayList<Vertex3D>();
		light_i = new ArrayList<Color>();
		reflection = new ArrayList<double[]>();
		vector_to_light = new ArrayList<double[]>();
		distance = new double[num_light];
		attenuation_a = new double[num_light];
		attenuation_b = new double[num_light];	
		distance = new double[num_light];
	}

	@Override
	public Vertex3D shade(Polygon polygon, Vertex3D vertex) {
		// TODO Auto-generated method stub
		// initialize gouraudVertex
		gouraudVertex = vertex;
		// initialize the ambient light of vertex
		ambient_light = vertex.getColor();
		// get the three vertices of polygon
		firstVertex = polygon.get(1);
		secondVertex = polygon.get(0);
		thirdVertex = polygon.get(2);
		
		// get the distance between light source and vertex to be illuminated
		distance = getDistance(vertex);
		
		// get the unit normal vector
		normal_vector = getNormalVector(firstVertex, secondVertex, thirdVertex);
		
		// get the view vector
		view_vector = getViewVector(vertex);
		
		// get L
		vector_to_light = getLvector(vertex);
		
		// get R
		reflection = getRvector();
		
		// proceed lighting calculation
		gouraudVertex = lightingCal(vertex);
		
		return gouraudVertex;
	}
	// proceed lighting calculation
	private Vertex3D lightingCal(Vertex3D v) {
		
		// initialize light_intensity
		for(int i = 0; i < num_light; i++) {
			light_i.add(lightBulb.get(i).light_intensity);
		}
		double intensity_r[] = new double[num_light];
		double intensity_g[] = new double[num_light];
		double intensity_b[] = new double[num_light];
		
		for(int j = 0; j < num_light; j++) {
			// light intensity(RGB)
			intensity_r[j] = light_i.get(j).getR();
			intensity_g[j] = light_i.get(j).getG();
			intensity_b[j] = light_i.get(j).getB();
		}
		
		// initialize attenuation constants
		for(int k = 0; k < num_light; k++) {
			attenuation_a[k] = lightBulb.get(k).attenuation_a;
			attenuation_b[k] = lightBulb.get(k).attenuation_b;
		}
		
		Vertex3D shaded_vertex = v;
		double ambient_r = ambient_light.getR();
		double ambient_g = ambient_light.getG();
		double ambient_b = ambient_light.getB();
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
		
		Color shade_color = new Color(light_r, light_g, light_b);
		shaded_vertex = new Vertex3D(v.getX(), v.getY(), v.getZ(), shade_color);
		return shaded_vertex;
	}
	private double RGBlightingCalculation(double ambient, double[] intensity, double diffuse) {
		double light_component;
		// first component of lighting calculation
		double first_component = ambient * diffuse;
		//System.out.println("the ambient light is: " + ambient);
		
		double sum = 0;
		double v_to_light[] = new double[3];
		double ref[] = new double[3];
		// second component of lighting calculation
		// N.L (the dot product of normal vector and vector_to_light)
		for(int k = 0; k < num_light; k++) {
		v_to_light = vector_to_light.get(k);
		ref = reflection.get(k);
		double NL = (-1) * normal_vector[0] * v_to_light[0] - normal_vector[1] * v_to_light[1] - 
				normal_vector[2] * v_to_light[2];
		// VR (the dot product of view vector and reflection vector)
		double VR = view_vector[0] * ref[0] + view_vector[1] * ref[1] + 
				view_vector[2] * ref[2];
		// fatti
		double fatti = 1/(attenuation_a[k] + attenuation_b[k] * distance[k]);
		// sum ---------------------- TO BE CHECK!
		sum += intensity[k] * fatti * (diffuse * NL + specular_reflection * Math.pow(VR, specular_exp));
		}
		light_component = first_component + sum;
		return light_component;
	}
	// calculate the R vector
	private ArrayList<double[]> getRvector() {
		ArrayList<double[]> all_R = new ArrayList<double[]>();
		double R[] = new double[3];
		// get the dot product of normal vector and incident vector
		for(int i = 0; i < num_light; i++) {
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
	// calculate the L vector
	private ArrayList<double[]> getLvector(Vertex3D v) {
		ArrayList<double[]> all_L = new ArrayList<double[]>();
		double L[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		L[0] = lightBulb.get(i).light_location.getX() - v.getX();
		L[1] = lightBulb.get(i).light_location.getY() - v.getY();
		L[2] = lightBulb.get(i).light_location.getZ() - v.getZ();
		double L_length = Math.sqrt(L[0]*L[0] + L[1]*L[1] + L[2]*L[2]);
		L[0] = L[0]/L_length;
		L[1] = L[1]/L_length;
		L[2] = L[2]/L_length;
		all_L.add(L);
		}
		return all_L;
	}
	// calculate the view vector
	private double[] getViewVector(Vertex3D v) {
		double view[] = new double[3];
		view[0] = v.getX();
		view[1] = v.getY();
		view[2] = v.getZ();
		double view_length = Math.sqrt(view[0]*view[0] + view[1]*view[1] + view[2]*view[2]);
		view[0] = view[0]/view_length;
		view[1] = view[1]/view_length;
		view[2] = view[2]/view_length;
		return view;
	}
	// calculate the normal vector of surface
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
	// calculate the distance
	private double[] getDistance(Vertex3D v) {
		double all_distance[] = new double[num_light];
		double light_distance = 0;
		double distant[] = new double[3];
		for(int i = 0; i < num_light; i++) {
		distant[0] = lightBulb.get(i).light_location.getX() - v.getX();
		distant[1] = lightBulb.get(i).light_location.getY() - v.getY();
		distant[2] = lightBulb.get(i).light_location.getZ() - v.getZ();
		light_distance = Math.sqrt(distant[0]*distant[0] + distant[1]*distant[1] + distant[2]*distant[2]);
		all_distance[i] = light_distance;
		}
		return all_distance;
	}
}
