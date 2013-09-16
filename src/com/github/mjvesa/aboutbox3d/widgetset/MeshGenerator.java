package com.github.mjvesa.aboutbox3d.widgetset;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * 
 * Generates a mesh with normals from a 2d image
 * 
 * @author mattivesa@vaadin.com
 */
public class MeshGenerator {

	private int width,height;
	private int rows,columns;
	
	private boolean[] bitmap;
	private int[] blurMap;
	
	private vertex_d[] vertexGrid;
	private boolean[] inside;
	
	
	private int[] tempFaces;
	private int[] gridToVerticesMap;
	private vertex_i[] tempVertices;
	
	private int faceCount;
	private int vertexCount;

	private int[] faces;
	private int[] vertices;
	private int[] normals;

	/**
	 * This can be used to generate a 3d mesh from a bitmap logo.
	 * 
	 */
	public void generateMesh(String logoFileName, int treshold,
			int columns, int rows, int scrollX, int scrollY, int blurFactor,
			int bumpX, int bumpY) {
		
		this.rows = rows;
		this.columns = columns;

		createBitmap(logoFileName, treshold, scrollX, scrollY);
		createBlurMap(blurFactor);
		createVertexGrid();
		createTempFaceList();
		interpolateEdges();
		cropVerticesAndCreateGridToVerticesMap();
		mirrorShape();
		remapFacelistIndexes();
		generateEdgeFaces();
		createNormalsFromBlurMap(bumpX, bumpY);
	}

	private void createBitmap(String logoFileName, int treshold, int scrollX, int scrollY) {

		try {
			
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			// Obtain a shape bitmap
			BufferedImage bi = ImageIO.read(loader.getResourceAsStream(logoFileName));

			bitmap = new boolean[bi.getWidth() * bi.getHeight()];
			width = bi.getWidth();
			height = bi.getHeight();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = bi.getRGB(x, y);
					int abs = ((rgb & 255) + ((rgb >> 8) & 255) + ((rgb >> 16) & 255)) / 3;
					bitmap[(Math.abs(x + scrollX) % width)
					       + (Math.abs(y + scrollY) % height) * width] = abs > treshold;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void createBlurMap(int blurFactor) {
		// Make a blurred image from the bitmap
		blurMap = new int[bitmap.length];
		for (int i = 0; i < bitmap.length; i++) {
			if (bitmap[i]) {
				blurMap[i] = 1000;
			} else {
				blurMap[i] = 0;
			}
		}

		for (int j = 0; j < blurFactor ; j++) {
			for (int i = 0; i < blurMap.length; i++) {
				blurMap[i] = (blurMap[Math.abs(i - 1) % blurMap.length] + blurMap[Math.abs(i + 1) % blurMap.length]
						+ blurMap[Math.abs(i - width) % blurMap.length] + blurMap[Math.abs(i + width) % blurMap.length]) / 4;
			}
		}
	}

	private void createVertexGrid() {
		// Check if points are inside the shape and generate vertex grid
		vertexGrid = new vertex_d[(rows + 1) * (columns + 1) ];
		inside = new boolean[(rows + 1) * (columns + 1)];
		int i = 0;
		for (int y = 0; y <= rows; y++) {
			for (int x = 0; x <= columns; x++) {
	
				int realX = width * x / columns;
				int realY = height * y / rows;
				int index = (realX + (realY * width)) % (width * height);
				vertexGrid[i] = new vertex_d();
				vertexGrid[i].x = realX;
				vertexGrid[i].y = realY;
				vertexGrid[i].z = blurMap[index] / 12000.0; // 0.07;
				inside[i] = bitmap[index];
				i++;
			}
		}
	}
	

	private void createTempFaceList() {
		// Produce a facelist from the shape
		tempFaces = new int[rows * columns * 3 * 2];
		faceCount = 0;
		int a,b,c;
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns; x++) {
				a = y * (columns + 1) + x;
				b = y * (columns + 1) + (x + 1);
				c = (y + 1) * (columns + 1) + x;
				if (inside[a] || inside[b] || inside[c]) {
					tempFaces[faceCount * 3 + 0] = a;
					tempFaces[faceCount * 3 + 1] = b;
					tempFaces[faceCount * 3 + 2] = c;
					faceCount++;
				}
				a = (y + 1) * (columns + 1) + (x + 1);
				b = (y + 1) * (columns + 1) + x;
				c = y * (columns + 1) + (x + 1);
				if (inside[a] || inside[b] || inside[c]) {
					tempFaces[faceCount * 3 + 0] = a;
					tempFaces[faceCount * 3 + 1] = b;
					tempFaces[faceCount * 3 + 2] = c;
					faceCount++;
				}
			}
		}
	}
	
	private void interpolateEdges() {
		// Interpolate points outside the shape to the edge of the shape.
		for (int i = 0; i < faceCount * 3; i++) {
			int j = tempFaces[i];
			if (!inside[j]) {
	
				double x = vertexGrid[j].x;
				double y = vertexGrid[j].y;
				double z = vertexGrid[j].z;
	
				int vertNum = ((int) (i / 3)) * 3;
				int closest = vertNum;
				double closestD = 10000;
				for (int k = vertNum; k < vertNum + 3; k++) {
					if (inside[tempFaces[k]]) {
						int l = tempFaces[k];
	
						double x2 = vertexGrid[l].x;
						double y2 = vertexGrid[l].y;
						double z2 = vertexGrid[l].z;
	
						double distance = Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2));
						if ((k != i) && (distance < closestD)) {
							closestD = distance;
							closest = l;
						}
					}
				}
	
				// Interpolate here towards the closest point until inside
				// shape
				
				double step =  Math.sqrt(Math.pow(width / columns,2)*Math.pow(height / rows,2)); 
	
				double deltaX = (vertexGrid[closest].x - x) / step;
				double deltaY = (vertexGrid[closest].y - y) / step;
				double deltaZ = (vertexGrid[closest].z - z) / step;
	
				while (!bitmap[Math.abs((((int) x) + ((int) y) * width)) % (width * height)]) {
					x += deltaX;
					y += deltaY;
					z += deltaZ;
				}
	
				vertexGrid[j].x = x;
				vertexGrid[j].y = y;
				vertexGrid[j].z = z;
	
				inside[j] = true;
			}
		}
	}

	@SuppressWarnings("unused")
	private void interpolateEdgesScanning() {
		// Interpolate points outside the shape to the edge of the shape.
		for (int i = 0; i < faceCount * 3; i++) {
			int j = tempFaces[i];
			if (!inside[j]) {
	
				double x = vertexGrid[j].x;
				double y = vertexGrid[j].y;
				double z = vertexGrid[j].z;
	
				int vertNum = ((int) (i / 3)) * 3;
				int closest = vertNum;
				double closestD = 10000;
				for (int k = vertNum; k < vertNum + 3; k++) {
					if (inside[tempFaces[k]]) {
						int l = tempFaces[k];
	
						double x2 = vertexGrid[l].x;
						double y2 = vertexGrid[l].y;
						double z2 = vertexGrid[l].z;
	
						double distance = Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2));
						if ((k != i) && (distance < closestD)) {
							closestD = distance;
							closest = l;
						}
					}
				}
	
				// Interpolate here towards the closest point until inside
				// shape
				
				double step =  Math.sqrt(Math.pow(width / columns,2)*Math.pow(height / rows,2)); 
	
				double deltaX = (vertexGrid[closest].x - x) / step;
				double deltaY = (vertexGrid[closest].y - y) / step;
				double deltaZ = (vertexGrid[closest].z - z) / step;
				
				double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
				double diagX = -deltaY / dist; 
				double diagY = deltaX / dist; 
	
				double a = 0;
				
				boolean done = false;
				
				while (!bitmap[Math.abs((((int) x) + ((int) y) * width)) % (width * height)] && !done) {

					a += 0.5;
					x += deltaX;
					y += deltaY;
					z += deltaZ;
					
					for (double d = -10 ; d < 10 ; d++) {
						double newX = x + diagX * d / 20 * a; 
						double newY = y + diagY * d / 20 * a;
						if (bitmap[Math.abs((((int) newX) + ((int) newY) * width)) % (width * height)]) {
							x = newX;
							y = newY;
							done = true;
							break;
						}
					}
				}
	
				vertexGrid[j].x = x;
				vertexGrid[j].y = y;
				vertexGrid[j].z = z;
	
				inside[j] = true;
			}
		}
	}

	
	@SuppressWarnings("unused")
	private void interpolateEdgesToClosest() {
		// Interpolate points outside the shape to the edge of the shape.
		for (int i = 0; i < faceCount * 3; i++) {
			int j = tempFaces[i];
			if (!inside[j]) {
	
				double x = vertexGrid[j].x;
				double y = vertexGrid[j].y;
				double z = vertexGrid[j].z;
				
				boolean done = false;
				
				for (double r = 0 ; r < 20 ; r+=0.5) {
					for (double ang = 0 ; ang < Math.PI * 2  ; ang+=0.01) {
						double newX = x + Math.cos(ang) * r;
						double newY = y + Math.sin(ang) * r;
						if (bitmap[Math.abs((((int) newX) + ((int) newY) * width)) % (width * height)]) {
							x = newX;
							y = newY;
							done = true;
							break;
						}
					}
					if (done) {
						break;
					}
				}
				
				vertexGrid[j].x = x;
				vertexGrid[j].y = y;
				vertexGrid[j].z = z;
	
				inside[j] = true;
			}
		}
	}

	private void cropVerticesAndCreateGridToVerticesMap() {
	
		// Make proper vertex list and map from the grid
		gridToVerticesMap = new int[vertexGrid.length];
		tempVertices = new vertex_i[vertexGrid.length];
		vertexCount = 0;
	
		for (int i = 0; i < inside.length; i++) {
			if (inside[i]) {
				gridToVerticesMap[i] = vertexCount;
				tempVertices[vertexCount] = new vertex_i();
				tempVertices[vertexCount].x = (int) ((vertexGrid[i].x / width - 0.5) * 2000);
				tempVertices[vertexCount].y = (int) ((vertexGrid[i].y / height - 0.5) * 2000);
				tempVertices[vertexCount].z = (int) ((vertexGrid[i].z) * 2000);
				vertexCount++;
			}
		}
	}
	

	private void mirrorShape() {
		// Mirror the shape to obtain a closed thingamob. Handle normals
		// at the same time
		vertices = new int[vertexCount * 3 * 2];
		normals = new int[vertexCount * 3 * 2];
		for (int i = 0; i < vertexCount * 2; i++) {

			int j = i * 3;
			if (i < vertexCount) {
				vertices[j + 0] = tempVertices[i].x;
				vertices[j + 1] = tempVertices[i].y;
				vertices[j + 2] = tempVertices[i].z;
			} else {
				int k = i - vertexCount;
				vertices[j + 0] = tempVertices[k].x;
				vertices[j + 1] = tempVertices[k].y;
				vertices[j + 2] = -tempVertices[k].z;
			}
		}
	}


	private void createNormalsFromBlurMap(int bumpX,int bumpY) {
		
		// go trough vertices and obtain normals trough the miracle of bumpmapping
		// also take into account the mirrored nature of the surface
		normals = new int[vertices.length];
		for (int i = 0; i < vertexCount; i++) {
			int x = (vertices[i * 3 + 0] + 1000) * width / 2000;
			int y = (vertices[i * 3 + 1] + 1000) * height / 2000;
			int xDelta = blurMap[Math.abs(x - bumpX + y * width) % blurMap.length] - blurMap[Math.abs(x + bumpX + y * width) % blurMap.length];
			int yDelta = blurMap[Math.abs(x + (y - bumpY) * width) % blurMap.length] - blurMap[Math.abs(x + (y + bumpY) * width) % blurMap.length];
	
			// We are inside a sphere of r = 1000, so the Z at each point is the height
			// of the sphere at that point
			int zDelta = (int) Math.sqrt(1000 * 1000 - (xDelta * xDelta + yDelta * yDelta));
			
	
			normals[i * 3 + 0] = xDelta;
			normals[i * 3 + 1] = yDelta;
			normals[i * 3 + 2] = zDelta;
	
			normals[(vertexCount + i) * 3 + 0] = xDelta;
			normals[(vertexCount + i) * 3 + 1] = yDelta;
			normals[(vertexCount + i) * 3 + 2] = -zDelta;
		}
	}

	private void remapFacelistIndexes() {
		faces = new int[faceCount * 3 * 2];
		// Fix facelist indexes. Now we have the full facelist
		for (int i = 0; i < faceCount * 3; i++) {
			faces[i] = gridToVerticesMap[tempFaces[i]];
			faces[i + faceCount * 3] = gridToVerticesMap[tempFaces[i]] + vertexCount;
		}
	}
	
	private void generateEdgeFaces() {
		
		ArrayList<Integer> edgeFaces = new ArrayList<Integer>();
		ArrayList<Double> edgeNormals = new ArrayList<Double>();
	
		// Find edge lines and make the edge polygons
		for (int i = 0; i < faceCount * 3; i += 3) {
			for (int j = 0; j < 3; j++) {
				int a = faces[i + j];
				int b = faces[i + ((j + 1) % 3)];
				int count = 0;
				for (int k = 0; k < faceCount * 3; k += 3) {
					if (((faces[k] == a) || (faces[k + 1] == a) || (faces[k + 2] == a)) && ((faces[k] == b) || (faces[k + 1] == b) || (faces[k + 2] == b))) {
						count++;
					}
				}
				// A line that is at the edge of the shape belongs only to one
				// triagle
				if (count == 1) {
	
					// a and b and vertexCount + a and vertexCount + b are new
					// points. Make two polygons out of that and add them
					// to the facelist
	
					// TODO might not be in clockwise order (if thats important)
					edgeFaces.add(a);
					edgeFaces.add(b);
					edgeFaces.add(a + vertexCount);
					edgeFaces.add(b);
					edgeFaces.add(b + vertexCount);
					edgeFaces.add(a + vertexCount);
	
					// Construct face normals
					double nx = vertices[b + 1] - vertices[a + 1];
					double ny = vertices[a + 0] - vertices[b + 0];
					double nz = 0;
					double d = Math.sqrt(nx * nx + ny * ny);
					nx = nx / d;
					ny = ny / d;
					edgeNormals.add(nx);
					edgeNormals.add(ny);
					edgeNormals.add(nz);
					edgeNormals.add(nx);
					edgeNormals.add(ny);
					edgeNormals.add(nz);
				}
			}
		}
	
		// Add edge polygons to facelist and vertices + normals
		int[] newFaces = new int[faceCount * 3 * 2 + edgeFaces.size()];
		for (int i = 0; i < faces.length; i++) {
			newFaces[i] = faces[i];
		}
	
		for (int i = 0; i < edgeFaces.size(); i++) {
			newFaces[i + faceCount * 3 * 2] = edgeFaces.get(i);
		}
	
		faces = newFaces;
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

	private class vertex_i {
		public int x, y, z;
	}

	private class vertex_d {
		public double x, y, z;
	}
}
