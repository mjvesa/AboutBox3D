package com.github.mjvesa.aboutbox.widgetset;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;

/**
 * Server side component for the VPolygonRenderer widget.
 */
public class AnimatedBackground extends AbstractComponent implements LegacyComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2978029177990977713L;


	public enum Effect {
		LOGO3D(0), LOGOLIGHTSOURCE(1), LOGO3DMOUSE(2), LOGODISTORT(3);
		
		private int effect;
		
		private Effect(int value) {
			effect = value;
		}
		
		public int getEffect() {
			return effect;
		}
	}

	
	private CloseListener closeListener = null;
	
	private Effect effect = Effect.LOGO3DMOUSE; 
	
	private double verticalSensitivity = 1.0;
	private double horizontalSensitivity = 1.0;
	private double depth = 200;
	private int fps = 20;
	private int width = 400;
	private int height = 400;
	
	private double objectZ = 512;
	private double objectScale = 400;
	
	
	private boolean stop = false;

	// These variables control the mesh generation process
	private String logopath;
	private int treshold;
	private int columns;
	private int rows;
	private int scrollX;
	private int scrollY;
	private int blurFactor;
	private int bumpX, bumpY;
	// pair of xy-coordinates that control how the image is moved
	// when applying temporal anti-aliasing
	private double[] jitters = new double[]{0.25, 0.75, 0.75, 0.25};
	
	private int baseColor = 0x00b4f0;
	
	String imageFileName = null;
	
	private boolean temporalAAEnabled = true;
	private boolean edgeAAEnabled = true;
	
	private int[] faces;
	private int[] vertices;
	private int[] normals;
	
	public AnimatedBackground() {
		resetToDefaults();
	}
	
	public void resetToDefaults() {
		// Somewhat reasonable defaults
		this.treshold = 128;
		this.columns = 10;
		this.rows = 10;
		this.scrollX = 0;
		this.scrollY = 0;
		this.blurFactor = 5;
		this.bumpX = 5;
		this.bumpY = 5;
	}
	
	public void setEffect(Effect effect) {
		this.effect = effect;
		requestRepaint();
	}
	
	/**
	 * This can be used to generate a 3d mesh from a bitmap logo.
	 * 
	 */
	public void setLogo(String logopath,int treshold,int columns,int rows,
			int scrollX, int scrollY, int blurFactor, int bumpX, int bumpY) {
		
		this.logopath = logopath;
		this.treshold = treshold;
		this.columns = columns;
		this.rows = rows;
		this.scrollX = scrollX;
		this.scrollY = scrollY;
		this.blurFactor = blurFactor;
		
	}
	
	public void setBlurFactor(int blurFactor) {
		this.blurFactor = blurFactor;
	}
	
	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getScrollX() {
		return scrollX;
	}

	public void setScrollX(int scrollX) {
		this.scrollX = scrollX;
	}

	public int getScrollY() {
		return scrollY;
	}

	public void setScrollY(int scrollY) {
		this.scrollY = scrollY;
	}

	public int getBumpX() {
		return bumpX;
	}

	public void setBumpX(int bumpX) {
		this.bumpX = bumpX;
	}

	public int getBumpY() {
		return bumpY;
	}

	public void setBumpY(int bumpY) {
		this.bumpY = bumpY;
	}

	public double getDepth() {
		return depth;
	}

	public int getBlurFactor() {
		return blurFactor;
	}
	
	

	public boolean isTemporalAAEnabled() {
		return temporalAAEnabled;
	}

	public void setTemporalAAEnabled(boolean temporalAAEnabled) {
		this.temporalAAEnabled = temporalAAEnabled;
		requestRepaint();
	}

	public boolean isEdgeAAEnabled() {
		return edgeAAEnabled;
	}

	public void setEdgeAAEnabled(boolean edgeAAEnabled) {
		this.edgeAAEnabled = edgeAAEnabled;
		requestRepaint();
	}

	public void generateMesh() {
		MeshGenerator gen = new MeshGenerator();
		gen.generateMesh(logopath, treshold, columns, rows, scrollX, scrollY,
				blurFactor, bumpX, bumpY);

		faces = gen.getFaces();
		vertices = gen.getVertices();
		normals = gen.getNormals();
	}
	
	public void setLogo(String logoFileName) {
		logopath = logoFileName;
	}
	
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	
	public double[] getJitters() {
		return jitters;
	}

	public void setJitters(double[] jitters) {
		this.jitters = jitters;
		requestRepaint();
	}
	
	/*
	 * Only 24 bits of each integer is used, which means each integer is four chars.
	 * 
	 */
	private final String base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-";
	
	private String intArrayToBase64(int[] data) {
		StringBuffer sb = new StringBuffer();
		
		for (int a : data) {
			int b = a + 8388608;
		    sb.append(base64Chars.charAt(b&63));
		    sb.append(base64Chars.charAt((b>>6)&63));
		    sb.append(base64Chars.charAt((b>>12)&63));
		    sb.append(base64Chars.charAt((b>>18)&63));
		}
		return sb.toString();
	}
	
	
	private String imageToBase64(String imageFileName) {
		
		try {
			StringBuffer sb = new StringBuffer();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			BufferedImage bi;
			bi = ImageIO.read(loader.getResourceAsStream(imageFileName));

			int width = bi.getWidth();
			int height = bi.getHeight();
	
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = bi.getRGB(x, y);
				    sb.append(base64Chars.charAt(rgb&63));
				    sb.append(base64Chars.charAt((rgb>>6)&63));
				    sb.append(base64Chars.charAt((rgb>>12)&63));
				    sb.append(base64Chars.charAt((rgb>>18)&63));
				}
			}
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param depth
	 */
	public void setDepth(double depth) {
		this.depth = depth;
		requestRepaint();
	}
	
	
	/**
	 * 
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
		requestRepaint();
	}
	
	/**
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
		requestRepaint();
	}
	
	
	
	public int getBaseColor() {
		return baseColor;
	}

	public void setBaseColor(int baseColor) {
		this.baseColor = baseColor;
	}
	

	public double getObjectZ() {
		return objectZ;
	}

	public void setObjectZ(double objectZ) {
		this.objectZ = objectZ;
		requestRepaint();
	}

	public double getObjectScale() {
		return objectScale;
	}

	public void setObjectScale(double objectScale) {
		this.objectScale = objectScale;
		requestRepaint();
	}

	/**
	 * 
	 * @param fps
	 */
	public void setFramerate(int fps) {
		this.fps = fps;
		requestRepaint();
	}
	
	public void stop() {
		this.stop = true;
		requestRepaint();
	}
	
	/**
	 * 
	 * @param horizontalSensitivity
	 * @param verticalSensitivity
	 */
	public void setSensitivity(double horizontalSensitivity,
				double verticalSensitivity) {
		this.verticalSensitivity = verticalSensitivity;
		this.horizontalSensitivity = horizontalSensitivity;
	}
	
	
	public void loadWavefrontObject(String filename) {

		ObjLoader loader = new ObjLoader();
		loader.loadWavefrontObject(filename);
		normals = loader.getNormals();
		vertices = loader.getVertices();
		faces = loader.getFaces();
	}
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		//super.paintContent(target);
		
		target.addAttribute("effect", effect.ordinal());
		target.addAttribute("horizontalSensitivity", horizontalSensitivity);
		target.addAttribute("verticalSensitivity", verticalSensitivity);
		target.addAttribute("depth", depth);
		target.addAttribute("fps", fps);
		target.addAttribute("baseColor", baseColor);
		target.addAttribute("temporalAAEnabled", temporalAAEnabled);
		target.addAttribute("edgeAAEnabled", edgeAAEnabled);
		target.addAttribute("canvas_width", width);
		target.addAttribute("canvas_height", height);
		target.addAttribute("faces", intArrayToBase64(faces));
		target.addAttribute("vertices", intArrayToBase64(vertices));
		target.addAttribute("normals", intArrayToBase64(normals));
		target.addAttribute("objectZ", objectZ);
		target.addAttribute("objectScale", objectScale);
		
		for (int i = 0 ; i < 4; i++) {
			target.addAttribute("jitters" + i, jitters[i]);
		}
		
		if (imageFileName != null) {
			target.addAttribute("texture", imageToBase64(imageFileName));
		}
		target.addAttribute("stop", stop);
	}
	
	public CloseListener getCloseListener() {
		return closeListener;
	}

	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	public interface CloseListener {
		public void animatedBackgroundClosed();
	}
	
	
    /**
     * Receive and handle events and other variable changes from the client.
     * 
     * {@inheritDoc}
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        //super.changeVariables(source, variables);

        if (variables.containsKey("stopped") &&
        		Boolean.TRUE.equals(variables.get("stopped"))) {
        		if (closeListener!=null) {
        			closeListener.animatedBackgroundClosed();
        		}
        	
        }
    }

	
}
