package com.github.mjvesa.aboutbox.animatedbackground;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ObjLoader {
	
	private int faces[];
	private int normals[];
	private int vertices[];
	
	/** 
     * Loads an object in Wavefront obj format. The object
     * should consist of a single (normalized) object made up of
     * triangles. There should be vertex normals. Other data,
     * such as texture coordinates, are ignored.
 	 * 
	 * @param filename Filename of the object to be loaded
 	 */
	public void loadWavefrontObject(String filename) {

		ArrayList<Integer> vertices = new ArrayList<Integer>();
		ArrayList<Integer> normals = new ArrayList<Integer>();
		ArrayList<Integer> faces = new ArrayList<Integer>();
		ArrayList<Integer> facesNormals = new ArrayList<Integer>();

		try {
			
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream is = loader.getResourceAsStream(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line = null;
			while (true) {
				line = reader.readLine();

				if (line == null) {
					break;
				}

				String [] tokens = line.split(" ");
				if (tokens[0].equals("v")) {
					vertices.add((int)(Double.parseDouble(tokens[1]) * 1000));
					vertices.add((int)(Double.parseDouble(tokens[2]) * 1000));
					vertices.add((int)(Double.parseDouble(tokens[3]) * 1000));
				}

				if (tokens[0].equals("f")) {
					String[] tmp;
					tmp = tokens[1].split("/");
					faces.add(Integer.parseInt(tmp[0]));
					facesNormals.add(Integer.parseInt(tmp[2]));
					tmp = tokens[2].split("/");
					faces.add(Integer.parseInt(tmp[0]));
					facesNormals.add(Integer.parseInt(tmp[2]));
					tmp = tokens[3].split("/");
					faces.add(Integer.parseInt(tmp[0]));
					facesNormals.add(Integer.parseInt(tmp[2]));
				}

				if (tokens[0].equals("vn")) {
					normals.add((int)(Double.parseDouble(tokens[1]) * 1000));
					normals.add((int)(Double.parseDouble(tokens[2]) * 1000));
					normals.add((int)(Double.parseDouble(tokens[3]) * 1000));
				}

			}
			
			this.normals = new int[vertices.size()];
			for (int i = 0 ; i < facesNormals.size() ; i++) {
				int a = (facesNormals.get(i) - 1) * 3;
				int b = (faces.get(i) - 1) * 3;
				this.normals[b] =  normals.get(a);
				this.normals[b + 1] = normals.get(a + 1);
				this.normals[b + 2] = normals.get(a + 2);
			}
			
			this.vertices = new int[vertices.size()];
			for (int i = 0 ; i < vertices.size() ; i++) {
				this.vertices[i] = vertices.get(i);				
			}
			
			
			this.faces = new int[faces.size()];
			for (int i = 0 ; i < faces.size() ; i++) {
				this.faces[i] = faces.get(i) - 1;				
			}

			System.out.println("Faces: " + faces.size() / 3);
			System.out.println("Vertices: " + vertices.size());
		} catch (Exception e) {
			System.out.println("Loading of " + filename + " failed.");
			e.printStackTrace();
		}
	}

	public int[] getFaces() {
		return faces;
	}

	public int[] getNormals() {
		return normals;
	}

	public int[] getVertices() {
		return vertices;
	}
	

}
