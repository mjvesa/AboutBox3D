package com.github.mjvesa.aboutbox.animatedbackground;
/**
 *
 * Represents one point in a mesh including vertex normals. A vertex can be
 * either projected to screen space or not. When projected, the x and y
 * components are valid in screen space.
 *
 * TODO: might be faster when fields are accessed directly.
 */
public class Vertex {

	private double x, y, z;
	private double nx, ny, nz;
	private boolean isProjected = false;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getNx() {
		return nx;
	}

	public void setNx(double nx) {
		this.nx = nx;
	}

	public double getNy() {
		return ny;
	}

	public void setNy(double ny) {
		this.ny = ny;
	}

	public double getNz() {
		return nz;
	}

	public void setNz(double nz) {
		this.nz = nz;
	}

	public boolean isProjected() {
		return isProjected;
	}

	public void setProjected(boolean isProjected) {
		this.isProjected = isProjected;
	}

	public int getScreenX() {
		return (int) x;
	}

	public int getScreenY() {
		return (int) y;
	}

	public int getScreenZ() {
		return (int) z;
	}
}
