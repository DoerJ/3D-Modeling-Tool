package client.interpreter;

import java.util.ArrayList;
import java.util.List;
import shading.*;
import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.BlerpPolygonRenderer;
import polygon.Polygon;
import polygon.wireframeRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import client.interpreter.*;
import client.RendererTrio;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;
	private BlerpPolygonRenderer phongRenderer;
	
	private ArrayList<Lighting> lightBulb;

	private class ObjVertex {
		// TODO: fill this class in.  Store indices for a vertex, a texture, and a normal.  Have getters for them.
		private int vertex_index;
		
		private ObjVertex(int index) {
			this.vertex_index = index;
		}
		// the getter method
		private int getVertex() {
			return this.vertex_index;
		}
	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}	
	private LineBasedReader reader;
	
	// array list can grow dynamically
	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;
	// store the camera space vertices to be shaded
	private List<Vertex3D> shaded_vertices;
	
	private FlatShading flat;
	private GouraudShading gouraud;
	
	private double specular_coefficient;
	private double specular_exp;
	
	private String shading_style;
	
	private Color defaultColor;
	// ambient color (color of object)
	private Color objectColor;
	// constructor
	public ObjReader(String filename, Color defaultColor, ArrayList<Lighting> lightBulb, String shading_style, 
			double specular_coefficient, double specular_exp, Color objectColor) {
		// TODO: Initialize an instance of this class.
		objVertices = new ArrayList<Vertex3D>();
		transformedVertices = new ArrayList<Vertex3D>();
		objNormals = new ArrayList<Point3DH>();
		shaded_vertices = new ArrayList<Vertex3D>();
		objFaces = new ArrayList<ObjFace>();
		this.defaultColor = defaultColor;
		this.lightBulb = lightBulb;
		this.shading_style = shading_style;
		this.specular_coefficient = specular_coefficient;
		this.specular_exp = specular_exp;
		this.objectColor = objectColor;
		this.phongRenderer = new BlerpPolygonRenderer();
		// reader has been initialized
		reader = new LineBasedReader(filename);
	}

	public void render(Drawable drawable) {
		// TODO: Implement.  All of the vertices, normals, and faces have been defined.
		// First, transform all of the vertices.		
		// Then, go through each face, break into triangles if necessary, and send each triangle to the renderer.
		// You may need to add arguments to this function, and/or change the visibility of functions in SimpInterpreter.
		for(int i = 0; i < objFaces.size(); i++) {
			// check if a face needs to be broken down to pieces
			ObjFace face = objFaces.get(i);
			if(face.size() > 3) {
				// use filled renderer
				if(SimpInterpreter.render_style == true) {
					for(int j = 1; j < face.size() - 1; j ++) {
						int first_index = face.get(0).getVertex();
						int second_index = face.get(j).getVertex();
						int third_index = face.get(j + 1).getVertex();
						
						// shade surface
						Polygon shaded_polygon = shadeSurface(first_index, second_index, third_index);
						Color shade_color_v1 = shaded_polygon.get(0).getColor();
						Color shade_color_v2 = shaded_polygon.get(1).getColor();
						Color shade_color_v3 = shaded_polygon.get(2).getColor();
						
						Vertex3D first_vertex = new Vertex3D(transformedVertices.get(first_index).getX(), 
								transformedVertices.get(first_index).getY(), transformedVertices.get(first_index).getZ(), shade_color_v1);
						
						Vertex3D second_vertex = new Vertex3D(transformedVertices.get(second_index).getX(), 
								transformedVertices.get(second_index).getY(), transformedVertices.get(second_index).getZ(), shade_color_v2);
						
						Vertex3D third_vertex = new Vertex3D(transformedVertices.get(third_index).getX(), 
								transformedVertices.get(third_index).getY(), transformedVertices.get(third_index).getZ(), shade_color_v3);
						
						Polygon dummy_face = Polygon.make(first_vertex, second_vertex, third_vertex);
						
						if(!shading_style.equals("flat")) {

							phongRenderer.drawPolygon(dummy_face, drawable);
						}
						else {
							SimpInterpreter.filledRenderer.drawPolygon(dummy_face, drawable);
						}
					}
				}
				// use wireframe renderer
				else {
					for(int j = 0; j < face.size() - 1; j ++) {
						int first_index = face.get(0).getVertex();
						int second_index = face.get(j).getVertex();
						int third_index = face.get(j + 1).getVertex();
						
						Vertex3D first_vertex = new Vertex3D(transformedVertices.get(first_index).getX(), 
								transformedVertices.get(first_index).getY(), transformedVertices.get(first_index).getZ(), Color.WHITE);
						
						Vertex3D second_vertex = new Vertex3D(transformedVertices.get(second_index).getX(), 
								transformedVertices.get(second_index).getY(), transformedVertices.get(second_index).getZ(), Color.WHITE);
						
						Vertex3D third_vertex = new Vertex3D(transformedVertices.get(third_index).getX(), 
								transformedVertices.get(third_index).getY(), transformedVertices.get(third_index).getZ(), Color.WHITE);
						
						Polygon dummy_face = Polygon.make(first_vertex, second_vertex, third_vertex);
						SimpInterpreter.wireframeRenderer.drawPolygon(dummy_face, drawable);
					}
				}
			}
			// no need to break down the face
			else{
				int first_index = face.get(0).getVertex();
				int second_index = face.get(1).getVertex();
				int third_index = face.get(2).getVertex();
				// -------------------------------- CHECK
//				System.out.println("the first vertex is: (" + transformedVertices.get(first_index).getX() + 
//						", " + transformedVertices.get(first_index).getY() + ", " + transformedVertices.get(first_index).getZ() + ")");
//				
//				System.out.println("the second vertex is: (" + transformedVertices.get(second_index).getX() + 
//						", " + transformedVertices.get(second_index).getY() + ", " + transformedVertices.get(second_index).getZ() + ")");
//				
//				System.out.println("the third vertex is: (" + transformedVertices.get(third_index).getX() + 
//						", " + transformedVertices.get(third_index).getY() + ", " + transformedVertices.get(third_index).getZ() + ")");
				
				if(SimpInterpreter.render_style == true) {
					
					// shade surface
					Polygon shaded_polygon = shadeSurface(first_index, second_index, third_index);
					Color shade_color = shaded_polygon.get(0).getColor();
					
					Vertex3D first_vertex = new Vertex3D(transformedVertices.get(first_index).getX(), 
							transformedVertices.get(first_index).getY(), transformedVertices.get(first_index).getZ(), shade_color);
					
					Vertex3D second_vertex = new Vertex3D(transformedVertices.get(second_index).getX(), 
							transformedVertices.get(second_index).getY(), transformedVertices.get(second_index).getZ(), shade_color);
					
					Vertex3D third_vertex = new Vertex3D(transformedVertices.get(third_index).getX(), 
							transformedVertices.get(third_index).getY(), transformedVertices.get(third_index).getZ(), shade_color);
					
					Polygon dummy_face = Polygon.make(first_vertex, second_vertex, third_vertex);
					if(!shading_style.equals("flat")) {
						
						phongRenderer.drawPolygon(dummy_face, drawable);
					}
					else{
						SimpInterpreter.filledRenderer.drawPolygon(dummy_face, drawable);
					}
				}
				else {
					
					Vertex3D first_vertex = new Vertex3D(transformedVertices.get(first_index).getX(), 
							transformedVertices.get(first_index).getY(), transformedVertices.get(first_index).getZ(), Color.WHITE);
					
					Vertex3D second_vertex = new Vertex3D(transformedVertices.get(second_index).getX(), 
							transformedVertices.get(second_index).getY(), transformedVertices.get(second_index).getZ(), Color.WHITE);
					
					Vertex3D third_vertex = new Vertex3D(transformedVertices.get(third_index).getX(), 
							transformedVertices.get(third_index).getY(), transformedVertices.get(third_index).getZ(), Color.WHITE);
					
					Polygon dummy_face = Polygon.make(first_vertex, 
							second_vertex, third_vertex);
					
					SimpInterpreter.wireframeRenderer.drawPolygon(dummy_face, drawable);
				}
			}
		}
	}
	
//	private Polygon polygonForFace(ObjFace face) {
//		// TODO: This function might be used in render() above.  Implement it if you find it handy.
//	}
	
	// shade surface
	private Polygon shadeSurface(int first_index, int second_index, int third_index) {
		
		Vertex3D first_vertex = new Vertex3D(shaded_vertices.get(first_index).getX(), 
				shaded_vertices.get(first_index).getY(), shaded_vertices.get(first_index).getZ(), defaultColor);
		
		Vertex3D second_vertex = new Vertex3D(shaded_vertices.get(second_index).getX(), 
				shaded_vertices.get(second_index).getY(), shaded_vertices.get(second_index).getZ(), defaultColor);
		
		Vertex3D third_vertex = new Vertex3D(shaded_vertices.get(third_index).getX(), 
				shaded_vertices.get(third_index).getY(), shaded_vertices.get(third_index).getZ(), defaultColor);
		Polygon shaded_polygon = Polygon.make(first_vertex, second_vertex, third_vertex);
		// if flat shading
		if(shading_style.equals("flat")) {
			Polygon plain_polygon = Polygon.make(first_vertex, second_vertex, third_vertex);
			flat = new FlatShading(plain_polygon, lightBulb, specular_coefficient, specular_exp, objectColor);
			shaded_polygon = flat.shade(plain_polygon);
		}
		// if gouraud shading
		else {
			Polygon plain_polygon = Polygon.make(first_vertex, second_vertex, third_vertex);
			gouraud = new GouraudShading(plain_polygon, lightBulb, specular_coefficient, specular_exp, objectColor);
			first_vertex = gouraud.shade(plain_polygon, first_vertex);
			second_vertex = gouraud.shade(plain_polygon, second_vertex);
			third_vertex = gouraud.shade(plain_polygon, third_vertex);
			shaded_polygon = Polygon.make(first_vertex, second_vertex, third_vertex);
		}
		return shaded_polygon;
	}
	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			// split by whitespace
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}
	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();
		// object face format: f 84/96/109 83/95/107 46/67/108 45/65/110
		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			// subtokens: 84 96 109
			String[] subtokens = token.split("/");
			// objVerteices.size() == the length of list for object vertex
			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			// TODO: fill in action to take here.
			ObjVertex face_vertex = new ObjVertex(vertexIndex);
			face.add(face_vertex);
		}
		// now we have a face of polygon
		// TODO: fill in action to take here.
		objFaces.add(face);
		// object face finished
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		// TODO: write this.  subtokens[tokenIndex], if it exists, holds a string for an index.
		// use Integer.parseInt() to get the integer value of the index.
		// Be sure to handle both positive and negative indices.
		int index = 0;
		// check if string is null
		if(!subtokens[tokenIndex].equals(null)) {
			index = Integer.parseInt(subtokens[tokenIndex]) - 1;
			// check whether the index is negative
			if(index < 0) {
				index = baseForNegativeIndices - 1 - (Math.abs(index) - 1);
			}
		}
		return index;
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);				
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		// TODO: fill in action to take here.
		// store the normal vertex into the list
		objNormals.add(normal);
		// normal vertex finished
	}
	private void interpretObjVertex(String[] tokens) {
		// check the format of object vertex to see if w or (r, g, b) come with it
		int numArgs = tokens.length - 1;
		// get object vertex in Point3DH
		// current point has been applied by CTM
		Point3DH point = objVertexPoint(tokens, numArgs);
		// get the color of the current object vertex
		Color color = objVertexColor(tokens, numArgs);
		
		// TODO: fill in action to take here.
		// convert Point3DH into Vertex3D
		Vertex3D obj_vertex = new Vertex3D(point, color);
		// store the object vertex into list
		objVertices.add(obj_vertex);
		
		// object vertex finished
		Point3DH trans_vertex = obj_vertex.getPoint3D();
		
		// transform to camera_space
		trans_vertex = SimpInterpreter.transformToCameraspace(trans_vertex);
		
		// add the CAMERA SPACE vertices into a arrayList
		// transform point3DH to Vertex3D
		Vertex3D shade_vertex = new Vertex3D(trans_vertex.getX(), trans_vertex.getY(), trans_vertex.getZ(), color);
		shaded_vertices.add(shade_vertex);
		
		// applied by perspective matrix
		trans_vertex = SimpInterpreter.transformToPerspective(trans_vertex);
		
		// transform to view space
		trans_vertex = SimpInterpreter.transformtoView(trans_vertex);
		
		Vertex3D transformed_vertex = new Vertex3D(trans_vertex, color);
		
		// store the transformed vertex into list
		transformedVertices.add(transformed_vertex);
//		System.out.println("----------------------");
//		System.out.println("The transformed vertex is: (" + transformed_vertex.getX()
//		 + ", " + transformed_vertex.getY() + ")");
//		System.out.println("----------------------");
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		// object vertex format: (x, y, z) or (x, y, z, r, g, b)
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		// object vertex format: (x, y, z, w) or (x, y, z, w, r, g, b)
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}