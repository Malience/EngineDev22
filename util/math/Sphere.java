package math;

import java.nio.IntBuffer;

import api.vulkan.Vertex;
import util.Util;

public class Sphere {
	public static Vertex[] generateSphere(float radius, int vdiv, int hdiv, IntBuffer indices) {
		int numIndices = 6 * vdiv * hdiv;
		int total = vdiv * hdiv + 2;
		Vertex top = new Vertex(0, radius, 0, 1, 0, 0);
		Vertex bot = new Vertex(0, -radius, 0, 0, 0, 1);
		Vertex[] vertices = new Vertex[total];
		//Stepping around the y-axis
		float hstep = 360f / (vdiv) * Constants.RADIAN;
		//Stepping around the x-axis
		float vstep = (180f / (hdiv + 1)) * Constants.RADIAN; //Fix, should subtract after calc not before
		vertices[0] = top;
		vertices[total - 1] = bot;
		
		for(int i = 0; i < vdiv; i++) {
			for(int j = 1; j < hdiv + 1; j++) {
				float x = radius * (float) (Math.cos(i * hstep) * Math.sin(j * vstep));
				//Swapped Y and Z axes
				float z = radius * (float) (Math.sin(i * hstep) * Math.sin(j * vstep));
				float y = radius * (float) (Math.cos(j * vstep));
				vertices[j + i * hdiv] = new Vertex(x, y, z, 0, 1, 0);
			}
		}
		for(int i = 0; i < vdiv; i++) {
			int x1 = 1 + i * hdiv, y1 = i != vdiv - 1 ? 1 + i * hdiv + hdiv : 1;
			indices.put(0).put(x1).put(y1); //Top triangle
			for(int j = 0; j < hdiv - 1; j++) {
				indices.put(x1).put(x1 + 1).put(y1 + 1);
				indices.put(x1).put(y1 + 1).put(y1);
				x1++; y1++;
			}
			indices.put(x1).put(total - 1).put(y1); //Bot Triangle
		}
		indices.flip();
		
		Util.printIntBuffer(indices);
		return vertices;
	}
}
