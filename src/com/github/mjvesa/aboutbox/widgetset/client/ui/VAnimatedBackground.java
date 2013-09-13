package com.github.mjvesa.aboutbox.widgetset.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;

/**
 * Draws a real-time rendered 3d object consisting of a tringular
 * mesh. Features temporal and edge antialiazed, textured polygons.
 * 
 */
public class VAnimatedBackground extends Composite implements Paintable 
{
	
	private static final int LOGO3D = 0;
	private static final int LOGOLIGHTSOURCE = 1;
	private static final int LOGO3DMOUSE = 2;
	private static final int LOGODISTORT = 3;
	
	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-animatedbackground";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	private double verticalSensitivity = 1.0;
	private double horizontalSensitivity = 1.0;
	private double depth = 200;
	private int fps = 20;
	private int width = 400;
	private int height = 400; 
	
	private double objectZ = 512;
	private double objectScale = 400;
	
	private int light[];

	private int effect = LOGOLIGHTSOURCE;
	
	private double mouseX,mouseY;
	private double textureX,textureY;
	
	private Panel panel;
	
	private int  baseColor = 0x00b4f0; 
	private int[] previous = new int[width * height * 4];
	private boolean useVML;
	
	private int[] faces;
	private double[] jitters = new double[]{0.25, 0.75, 0.75, 0.25};
	private double[] vertices;
	private double[] normals;
	
	private boolean temporalAAEnabled = true;
	private boolean edgeAAEnabled = true;
	
	private int frameCount = 0;

	private Timer timer;
	private HandlerRegistration previewHandlerReg;
	
	private CanvasElement canvas;
	
	private double angleX,angleY;
	
	// private Date startDate;
	
	/**
	 * Constructs the widget. Calculates a simple light texture and
	 * sets up a timer for rendering.
	 */
	public VAnimatedBackground() {
	
		super();
		
		
		// startDate = new Date();
		panel = new FlowPanel();
		panel.setStylePrimaryName(CLASSNAME);

		canvas = new CanvasElement();
		panel.add(canvas);
		
		if (BrowserInfo.get().isIE() && !BrowserInfo.get().isIE9()) {
			calculateSmoothLight();
		} else {
			calculateSpotLight();
		}
		
		previewHandlerReg = Event.addNativePreviewHandler(
			new NativePreviewHandler() {
			  public void onPreviewNativeEvent(final NativePreviewEvent event) {
				    final int eventType = event.getTypeInt();
				    switch (eventType) {
				      case Event.ONMOUSEMOVE:
				    	  mouseX = event.getNativeEvent().getClientX();
				    	  mouseY = event.getNativeEvent().getClientY();
				        break;
				      default:
				    }
				  }
				});
		angleX = 0;
		angleY = 0;
		createTimer();
		initWidget(panel);
	}
	
	
	/*
	 * Creates a timer which calls the rendering routine
	 */
	private void createTimer() { 
		
		timer = new Timer() {
			private double t = 0;
			public void run() {

			textureX = 0;
			textureY = 0;
			
			// log("timer. " + startDate);
			
			if ((mouseX >= canvas.getAbsoluteLeft())
				&& (mouseX <= canvas.getAbsoluteLeft() + width)
				&& (mouseY >= canvas.getAbsoluteTop())
				&& (mouseY <= canvas.getAbsoluteTop() + height)) {
					angleX = mouseX - canvas.getAbsoluteLeft();
					angleY = mouseY - canvas.getAbsoluteTop();
				}

			frameCount++;
			
					
			if ((faces != null) && (faces.length>0) 
				&& (vertices != null) && (vertices.length>0)
				&& (normals != null) && (normals.length>0)) {
				
				
					if (effect == LOGO3D) {
						t+=0.05;
						drawObject(0,Math.sin(t)/2);			
					} else if (effect == LOGO3DMOUSE) {
						drawObject((height / 2 - angleY)*horizontalSensitivity / (height / 2),
								 (angleX - width / 2) * verticalSensitivity / (width / 2));
					} else if (effect == LOGOLIGHTSOURCE) {
						t+=0.05;
						textureY = (width / 2 - angleX)*verticalSensitivity;
						textureX = (height / 2 - angleY)*horizontalSensitivity;
						drawObject(0,Math.sin(t)/2);
					}
				}
			}
		 };
		 timer.scheduleRepeating(1000 / fps);
	}
	
	/*
	 * Calculates a spotlight texture which has a single
	 * gray circular spot of light on it.
	 */
	private void calculateSpotLight() {
		
		light = new int[256*256];
		int i = 0;
		for (int y=-128;y<128;y++) {
			for (int x=-128;x<128;x++) {
				int color =100000 / ((x*x+y*y)+1);
				if (color<0) color = 0; 
				if (color>255) color=255;
				int r = color;
				int g = color;
				int b = color;
				light[i&65535]= r * 65536 + g * 256 + b;  
				i++;
			}
		}
	}

	/*
	 * Calculates a smooth spotlight texture
	 */
	private void calculateSmoothLight() {
		
		light = new int[256*256];
		int i = 0;
		for (int y=-128;y<128;y++) {
			for (int x=-128;x<128;x++) {
				int color =255 - ((x*x+y*y) / 32);
				if (color<0) color = 0; 
				if (color>255) color=255;
				int r = color;
				int g = color;
				int b = color;
				light[i&65535]= r * 65536 + g * 256 + b;  
				i++;
			}
		}
	}

	/*
	 * This is where the meat of the drawing routine resides.
	 * 
	 */
	private native void drawObject(double x,double y) /*-{


	    var temporalAAEnabled = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::temporalAAEnabled;
	    var edgeAAEnabled = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::edgeAAEnabled;

	    var useVML = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::useVML;

	    var previous = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::previous;

	    var texture = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::light;

	    var frameCount = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::frameCount;

   	    var textureX = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::textureX;
	    var textureY = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::textureY;
   	    
   	    var width = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::width;
	    var height = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::height;
   	    
   	    var objectZ = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::objectZ;
	    var objectScale = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::objectScale;

	    var jitters = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::jitters;

	    var faces = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::faces;
	    var vertices = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::vertices;
	    var normals = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::normals;
	    
	    var baseColor = this.@com.example.aboutbox.widgetset.client.ui.VAnimatedBackground::baseColor;

		var colorR = (baseColor >> 16) & 255; 
		var colorG = (baseColor >> 8) & 255; 
		var colorB = baseColor & 255; 

		var hline;
		
		var edgeLeft = [];
		var edgeRight = [];
		
		for (var i = 0 ; i < height ; i++) {
			edgeLeft[i] = width - 1;
			edgeRight[i] = 0;
		} 

		//Textured polygon//////////////////////////////////////////////////////
		
   		texturedHLineAA = function(left, right, z, y, isFirst, isLast, canvasData) {
		
			if (right.x < left.x) {
				var temp = right;
				right = left;
				left = temp;
			}
			
			if (temporalAAEnabled) {
				var i = Math.floor(y);
				if (left.x < edgeLeft[i]) {
					if (left.x >= 0) {
						edgeLeft[i] = left.x;
					} else {
						edgeLeft[i] = 0;
					}
				}
				if (right.x > edgeRight[i]) {
					if (right.x < width) {
						edgeRight[i] = right.x;
					} else {
						edgeRight[i] = width - 1;
					}
				}
			}
			var leftX = Math.floor(left.x);
			var rightX = Math.floor(right.x);

			var tx = left.tx;
			var ty = left.ty;
			
			var leftAALength = Math.abs(left.xDelta);
			var rightAALength = Math.abs(right.xDelta);

			var txDelta = (right.tx - left.tx) / (rightX - leftX + 1);
			var tyDelta = (right.ty - left.ty) / (rightX - leftX + 1);
			
			var i	= Math.floor(leftX) + Math.floor(y) * width;
			var ptr = i * 4;
			
   			var color = texture[(Math.floor(tx) & 255) + (Math.floor(ty ) & 255) * 256];
    		
    		var r = colorR + ((color >> 16) & 255);
     		var g = colorG + ((color >> 8) & 255);
    		var b = colorB + (color & 255);

     		if (r > 255) {
    			r = 255;
    		}
    		
    		if (g > 255) {
    			g = 255;
    		}
    		
    		if (b > 255) {
    			b = 255;
    		}

			if (Math.abs(left.xDelta) > 1) {
			
				if (!(((left.xDelta > 0) && isFirst) ||
					((left.xDelta <= 0) && isLast))) {
					var aDelta = 1 / leftAALength;
					var a = aDelta;
					
					if (Math.abs(left.xDelta) < 2)  {
						a = 0.5;
					}
					var j = (Math.floor(leftX - leftAALength) + Math.floor(y) * width) * 4;
					for (var x = 0 ; x < leftAALength ; x++) {
					
						if (pixels[j + 3] !== 255) {
			            	pixels[j + 0] = r;
				            pixels[j + 1] = g;
				            pixels[j + 2] = b;
				            pixels[j + 3] = 250 * a;
			            }
			            j = j + 4;
			            a = a + aDelta;
					}
				}
			} else {
			
				if (pixels[ptr - 4 + 3] !== 255) {
					var a = left.count / left.totalSteps;
					if (left.xDelta > 0) {
						a = 1 - a;
					} 
					
					if (left.totalSteps === 1) {
						a = 0.5;
					}
					
             		pixels[ptr - 4 + 0] = r;
	            	pixels[ptr - 4 + 1] = g;
	            	pixels[ptr - 4 + 2] = b;
	            	pixels[ptr - 4 + 3] = 250 * a;
	            }
			}
			
			for (var x = leftX ; x <= rightX ; x++) {
			
				color = texture[(Math.floor(tx) & 255) + (Math.floor(ty ) & 255) * 256];
        		
	    		r = colorR + ((color >> 16) & 255);
	     		g = colorG + ((color >> 8) & 255);
	    		b = colorB + (color & 255);

        		if (r > 255) {
        			r = 255;
        		}
        		
        		if (g > 255) {
        			g = 255;
        		}
        		
        		if (b > 255) {
        			b = 255;
        		}
        		
	            pixels[ptr + 0] = r;
	            pixels[ptr + 1] = g;
	            pixels[ptr + 2] = b;
	            pixels[ptr + 3] = 255;

				tx = tx + txDelta;
				ty = ty + tyDelta;
	            ptr = ptr + 4;
	       	}
	       	
	       color = texture[(Math.floor(tx) & 255) + (Math.floor(ty) & 255) * 256];
        		
    		r = colorR + ((color >> 16) & 255);
     		g = colorG + ((color >> 8) & 255);
    		b = colorB + (color & 255);

    		if (r > 255) {
    			r = 255;
    		}
    		
    		if (g > 255) {
    			g = 255;
    		}
    		
    		if (b > 255) {
    			b = 255;
    		}	
	       	
      		if (Math.abs(right.xDelta) > 1) {
    		
				if (!(((right.xDelta > 0) && isLast) ||
				((right.xDelta <= 0) && isFirst))) {
					var aDelta = 1 / rightAALength;
					var a = 1 - aDelta;
					
					if (Math.abs(right.xDelta) < 2)  {
						a = 0.5;
					}
	
					for (var x = 0 ; x < rightAALength ; x++) {
					
						if (pixels[ptr + 3] !== 255) {
			            	pixels[ptr + 0] = r;
				            pixels[ptr + 1] = g;
				            pixels[ptr + 2] = b;
				            pixels[ptr + 3] = 250 * a;
			            }
			            ptr = ptr + 4;
			            a = a - aDelta;
					}
				}
			} else {
				if (pixels[ptr + 3] !== 255) {
					
					var a = right.count  / right.totalSteps;
	 				if (right.xDelta < 0) {
						a = 1 - a;
					}

					if (right.totalSteps === 1) {
						a = 0.5;
					}

             		pixels[ptr + 0] = r;
	            	pixels[ptr + 1] = g;
	            	pixels[ptr + 2] = b;
	            	pixels[ptr + 3] = 250 * a;
	            }
			}
		}

   		texturedHLine = function(left, right, z, y, isFirst, isLast, canvasData) {
		
			if (right.x < left.x) {
				var temp = right;
				right = left;
				left = temp;
			}

			if (temporalAAEnabled) {
				var i = Math.floor(y);
				if (left.x < edgeLeft[i]) {
					if (left.x >= 0) {
						edgeLeft[i] = left.x;
					} else {
						edgeLeft[i] = 0;
					}
				}
				if (right.x > edgeRight[i]) {
					if (right.x < width) {
						edgeRight[i] = right.x;
					} else {
						edgeRight[i] = width - 1;
					}
				}
			}

			var leftX = Math.floor(left.x);
			var rightX = Math.floor(right.x);

			var tx = left.tx;
			var ty = left.ty;
			
			var txDelta = (right.tx - left.tx) / (rightX - leftX + 1);
			var tyDelta = (right.ty - left.ty) / (rightX - leftX + 1);
			
			var i	= Math.floor(leftX) + Math.floor(y) * width;
			var ptr = i * 4;
			
			
			for (var x = leftX ; x <= rightX ; x++) {
			
				color = texture[(Math.floor(tx) & 255) + (Math.floor(ty ) & 255) * 256];
        		
	    		r = colorR + ((color >> 16) & 255);
	     		g = colorG + ((color >> 8) & 255);
	    		b = colorB + (color & 255);

        		if (r > 255) {
        			r = 255;
        		}
        		
        		if (g > 255) {
        			g = 255;
        		}
        		
        		if (b > 255) {
        			b = 255;
        		}
        		
	            pixels[ptr + 0] = r;
	            pixels[ptr + 1] = g;
	            pixels[ptr + 2] = b;
	            pixels[ptr + 3] = 255;

				tx = tx + txDelta;
				ty = ty + tyDelta;
	            ptr = ptr + 4;
	       	}
	       	
		}

		drawTexturedPoly = function(a,b,c,canvasData) {

			var temp;
			
			if (a.y > b.y) {
				temp = a;
				a = b;
				b = temp;
			}
			
			if (b.y > c.y) {
				temp = b;
				b = c;
				c = temp;
			}

			if (a.y > b.y) {
				temp = a;
				a = b;
				b = temp;
			}

			var left = {};
			var right = {};
			
			left.xDelta 	= (b.x - a.x) 	/ (b.y - a.y + 1);
			right.xDelta 	= (c.x - a.x) 	/ (c.y - a.y + 1);
			left.txDelta 	= (b.tx - a.tx)	/ (b.y - a.y + 1);
			right.txDelta 	= (c.tx - a.tx)	/ (c.y - a.y + 1);
			left.tyDelta 	= (b.ty - a.ty)	/ (b.y - a.y + 1);
			right.tyDelta 	= (c.ty - a.ty)	/ (c.y - a.y + 1);
			
			left.x		= a.x;
			right.x		= a.x;
			left.tx		= a.tx;
			right.tx	= a.tx;
			left.ty		= a.ty;		
			right.ty	= a.ty;	            	

			var z = (a.z + b.z + c.z) / 3;
			
			left.count = 1;
			right.count = 1;
			left.prevX = Math.floor(left.x);
			right.prevX = Math.floor(right.x);
			
			left.totalSteps = 1 / Math.abs(left.xDelta);
			var steps = Math.floor(b.y) - Math.floor(a.y); 
			if (2 * steps < left.totalSteps) {
				left.totalSteps = steps;
			}

			right.totalSteps = 1 / Math.abs(right.xDelta);
			steps = Math.floor(c.y) - Math.floor(a.y); 
			if (2 * steps < right.totalSteps) {
				right.totalSteps = steps;
			} 
			
			var isFirst = true;
			var isLast = false;
			
			for (var sy = Math.floor(a.y) ; sy < Math.floor(b.y) ; sy++) {

				if (!(sy + 1 < Math.floor(b.y))) {
					isLast = true;
				}

				hline(left, right, z, sy, isFirst, isLast, canvasData);

				left.x		= left.x   + left.xDelta;
				right.x		= right.x  + right.xDelta;
				left.tx		= left.tx  + left.txDelta;
				right.tx	= right.tx + right.txDelta;
				left.ty		= left.ty  + left.tyDelta;
				right.ty	= right.ty + right.tyDelta;
				
				isFirst = false;

				left.count = left.count + 1;
				right.count = right.count + 1;
				if (left.prevX !== Math.floor(left.x)) {
					left.count = 1;
				}
				if (right.prevX !== Math.floor(right.x)) {
					right.count = 1;
				}
				left.prevX = Math.floor(left.x);
				right.prevX = Math.floor(right.x);
			}

			left.xDelta = (c.x-b.x) / (c.y-b.y + 1);
			left.txDelta = (c.tx-b.tx) / (c.y-b.y + 1);
			left.tyDelta = (c.ty-b.ty) / (c.y-b.y + 1);
			left.x = b.x;
			left.tx = b.tx;
			left.ty = b.ty;
			
			left.count = 1;
			left.prevX = Math.floor(left.x);
			
			left.totalSteps = 1 / Math.abs(left.xDelta);
			steps = Math.floor(c.y) - Math.floor(b.y); 
			if ( 2 * steps < left.totalSteps) {
				left.totalSteps = steps;
			}

			isFirst = true;
			isLast = false;
			
			for (var sy = Math.floor(b.y) ; sy < Math.floor(c.y) ; sy++) {

				if (!(sy + 1 < Math.floor(c.y))) {
					isLast = true;
				}

				hline(left, right, z, sy, isFirst, isLast, canvasData);
				
				left.x		= left.x	+ left.xDelta;
				right.x		= right.x	+ right.xDelta;
				left.tx		= left.tx	+ left.txDelta;
				right.tx	= right.tx	+ right.txDelta;
				left.ty		= left.ty	+ left.tyDelta;
				right.ty	= right.ty	+ right.tyDelta;

				isFirst = false;
				
				left.count = left.count + 1;
				right.count = right.count +1;
				if (left.prevX !== Math.floor(left.x)) {
					left.count = 1;
				}
				if (right.prevX !== Math.floor(right.x)) {
					right.count = 1;
				}
				left.prevX = Math.floor(left.x);
				right.prevX = Math.floor(right.x);
			}
		}

		if (edgeAAEnabled) {
			hline = texturedHLineAA;
		} else {
			hline = texturedHLine;
		}
		
		var pixels = [];
		
		//Rotation and rendering////////////////////////////////////////////////
	  	if (useVML !== true) {
		  	var canvas = $doc.getElementById('rendercanvas');
		    var ctx = canvas.getContext('2d');
		    var canvasData = ctx.createImageData(canvas.width, canvas.height);
		    pixels = canvasData.data;
	    }
	    

	    var points = [];
		
		var jitters = [0.25, 0.75, 0.75, 0.25];
		var jitterX = 0;
		var jitterY = 0;
		if (temporalAAEnabled) {
			jitterX = jitters[(frameCount & 1) * 2];
			jitterY = jitters[(frameCount & 1) * 2 + 1];
		}
		
		var cx = Math.cos(x);
		var sx = Math.sin(x);

		var cy = Math.cos(y);
		var sy = Math.sin(y);

		for (var i = 0 ; i < vertices.length ; i+=3) {
			
			var pi = 3.14159265358979323846264338327950288419716939937510;

			var x1 = vertices[i + 0] * objectScale;
			var y1 = vertices[i + 1] * objectScale;
			var z1 = vertices[i + 2] * objectScale;

			var nx1 = normals[i + 0];
			var ny1 = normals[i + 1];
			var nz1 = normals[i + 2];

			
			var y2 = cx * y1 + sx * z1;
			var z2 = sx * y1 - cx * z1;  

			var x2 = cy * x1 + sy * z2;
			var z3 = sy * x1 - cy * z2;

			var ny2 = cx * ny1+ sx * nz1;
			var nz2 = sx * ny1- cx * nz1;  

			var nx2 = cy * nx1 + sy * nz2;
			var nz3 = sy * nx1 - cy * nz2;
			
			var j = i / 3;
			points[j] = {};
			
			points[j].x = width / 2 + (x2 * 256 / (z3 + objectZ)) + jitterX;
			points[j].y = height / 2 + (y2 * 256 / (z3 + objectZ)) + jitterY;
			points[j].z = 128 + z3;
			
			if (useVML === true) {
			
				points[j].vmlString = points[j].x.toFixed() + ' ' + points[j].y.toFixed();
			}

			points[j].nx = nx2 * 128 + 128;
			points[j].ny = ny2 * 128 + 128;
			points[j].nz = nz3 * 128 + 128;
		}
	
		//Obtain Z for each face and construct a sortable facelist 
		facelist = [];
		for (var i = 0 ; i < faces.length ; i+=3) {
			var a = faces[i+0];
			var b = faces[i+1];
			var c = faces[i+2];
			var j = i / 3;
			facelist[j] = {};
			facelist[j].a = a;
			facelist[j].b = b;
			facelist[j].c = c;
			facelist[j].z = (points[a].z + points[b].z + points[c].z) / 3;
		}

		// Sort faces 
		sortfunction = function(a, b) {
			if (a.z > b.z) return -1;
			if (a.z < b.z) return 1;
			return 0;
		}
		
		facelist.sort(sortfunction);
		
		var vmlStrings = new Array(facelist.length);
		 
		// Draw sorted faces
		for (var i = 0 ; i < facelist.length ; i++) {
			
			var a = facelist[i].a;
			var b = facelist[i].b;
			var c = facelist[i].c;
			
			
			var nz = Math.min(Math.min(points[a].nz, points[b].nz), points[c].nz);
			
			if (nz < 128) {
				
				points[a].tx = points[a].nx + textureX;
				points[a].ty = points[a].ny + textureY;
				points[b].tx = points[b].nx + textureX;
				points[b].ty = points[b].ny + textureY;
				points[c].tx = points[c].nx + textureX;
				points[c].ty = points[c].ny + textureY;
				
				if (useVML === true) {

					var nx = Math.floor((points[a].nx + points[b].nx + points[c].nx) / 3); 
					var ny = Math.floor((points[a].ny + points[b].ny + points[c].ny) / 3);

					var indexColor = texture[(nx + ny * 256) & 65535]; 
					var indexR = indexColor & 255;
					var indexG = indexColor / 256 & 255;
					var indexB = indexColor / 65536  & 255;

					var cr = colorR + indexR;
					if (cr > 255) {
						cr = 255;
					}
					var cg = colorG + indexG;
					if (cg > 255) { 
						cg = 255;
					}
					var cb = colorB + indexB;
					if (cb > 255) { 
						cb = 255;
					}
					var color = 'rgb(' + cr.toFixed() + ',' + cg.toFixed() + ',' + cb.toFixed() + ')';

				 	vmlStrings[i] = '<v:polyline class="v" points="' 
				 			+ points[a].vmlString + ' '
				 			+ points[b].vmlString + ' '
				 			+ points[c].vmlString + ' '
				 			+ points[a].vmlString + '"'
				 			+ 'filled="true" stroke="true" fillcolor="'+color+'" strokeweight="2" strokecolor="'+color+'">'
				 			+ '</v:polyline>';
				} else {
	
					// console.log('drawing tha polygone');
					drawTexturedPoly(points[a], points[b], points[c], canvasData);
				}
			}
		}
	
	if (useVML === true) {
		$doc.getElementById("rendercanvas").innerHTML = vmlStrings.join("");
		
	} else {
	
		if (temporalAAEnabled) {

			for (var i = 0 ; i < previous.length ; i+=4) {
			
				var	 a = canvasData.data[i + 0];
				var	 b = canvasData.data[i + 1];
				var	 c = canvasData.data[i + 2];
				var	 d = canvasData.data[i + 3];
				
				canvasData.data[i + 0] = Math.max(canvasData.data[i], previous[i]);
				canvasData.data[i + 1] = Math.max(canvasData.data[i + 1], previous[i + 1]);
				canvasData.data[i + 2] = Math.max(canvasData.data[i + 2], previous[i + 2]);
				canvasData.data[i + 3] =(canvasData.data[i + 3] + previous[i + 3]) /2;
	
				previous[i + 0] = a;   
				previous[i + 1] = b;   
				previous[i + 2] = c;   
				previous[i + 3] = d;   
			}

		}
		ctx.putImageData(canvasData, 0, 0);
	}
	
}-*/;
	
	
    /**
     * Called whenever an update is received from the server 
     */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first. 
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, true)) {
		    // If client.updateComponent returns true there has been no changes and we
		    // do not need to update anything.
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();
		
		if (uidl.hasAttribute("effect")) {
			effect = uidl.getIntAttribute("effect");
		}

		if (uidl.hasAttribute("stop")) {
			boolean stop = uidl.getBooleanAttribute("stop");
			if (stop) {
				previewHandlerReg.removeHandler();
				timer.cancel();
				client.updateVariable(paintableId, "stopped", true, true);
			}
		}

		if (uidl.hasAttribute("fps")) {
			fps = uidl.getIntAttribute("fps");
		}

		if (uidl.hasAttribute("baseColor")) {
			baseColor = uidl.getIntAttribute("baseColor");
		}
		if (uidl.hasAttribute("temporalAAEnabled")) {
			temporalAAEnabled = uidl.getBooleanAttribute("temporalAAEnabled");
		}

		if (uidl.hasAttribute("edgeAAEnabled")) {
			edgeAAEnabled = uidl.getBooleanAttribute("edgeAAEnabled");
		}

		if (uidl.hasAttribute("canvas_width")) {
			width = uidl.getIntAttribute("canvas_width");
			canvas.setWidth(width);
		}

		if (uidl.hasAttribute("canvas_height")) {
			height = uidl.getIntAttribute("canvas_height");
			canvas.setHeight(height);
		}
		
		if (uidl.hasAttribute("objectZ")) {
			objectZ = uidl.getIntAttribute("objectZ");
		}
		
		
		if (uidl.hasAttribute("objectScale")) {
			objectScale = uidl.getIntAttribute("objectScale");
		}

		for (int i = 0; i < 4 ; i++) {
			if (uidl.hasAttribute("jitters"+i)) {
				jitters[i] = uidl.getIntAttribute("jitters"+i);
			}
		}

		if (uidl.hasAttribute("texture")) {
			light = base64toIntArray(uidl.getStringAttribute("texture"), false);
		}

		if (uidl.hasAttribute("verticalSensitivity")) {
			verticalSensitivity = uidl.getIntAttribute("verticalSensitivity");
		}

		if (uidl.hasAttribute("horizontalSensitivity")) {
			horizontalSensitivity = uidl.getIntAttribute("horizontalSensitivity");
		}
		
		if (uidl.hasAttribute("vertices")) {
			
			int[] verts = base64toIntArray(uidl.getStringAttribute("vertices"), true);
			vertices = new double[verts.length];
			for (int i = 0; i < vertices.length ; i++) {
				vertices[i] = verts[i] / 1000.0; //Fixed point madness
			}
		}
		
		if (uidl.hasAttribute("normals")) {
			
			int[] norms = base64toIntArray(uidl.getStringAttribute("normals"), true);
			normals = new double[norms.length];
			for (int i = 0; i < normals.length ; i++) {
				normals[i] = norms[i] / 1000.0; //Fixed point madness
			}
		}
		
		if (uidl.hasAttribute("faces")) {
			faces =  base64toIntArray(uidl.getStringAttribute("faces"), true);
		}
	}

	private final String base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-";
	
	private int[] base64toIntArray(String s, boolean isSigned) {
		
		int[] ints = new int[s.length() / 4];
		for (int i = 0 ; i < s.length() ; i+=4) {
			
			int a = base64Chars.indexOf(s.charAt(i))
				   +(base64Chars.indexOf(s.charAt(i+1)) << 6)
				   +(base64Chars.indexOf(s.charAt(i+2)) << 12)
				   +(base64Chars.indexOf(s.charAt(i+3)) << 18);
			if (isSigned) {
				a = a  - 8388608;
			}				   
			ints[i / 4] = a;
		}
		
		return ints;
	}

	/**
	 * 
	 * 
	 * This contains our render and editor canvases
	 * 
	 * @author mjvesa
	 *
	 */
	private class CanvasElement extends Widget {
		
		private Element renderCanvas;
		
		public CanvasElement() {
			Element root = Document.get().createDivElement();
			Element render = Document.get().createDivElement();
			render.setClassName("v-rendercanvas");
			root.appendChild(render);
			
			if (BrowserInfo.get().isIE() && !BrowserInfo.get().isIE9()) {
				addNamespaceAndStyle("v","v");
				renderCanvas = render;
				useVML = true;
			} else {
				renderCanvas = Document.get().createElement("canvas");
				render.appendChild(renderCanvas);
				useVML = false;
			}

			renderCanvas.setId("rendercanvas");
			renderCanvas.setAttribute("width", "" + width);
			renderCanvas.setAttribute("height", "" +  height);

			setElement(root);
		}
		
		public void setWidth(int width) {
			renderCanvas.setAttribute("width", "" + width);
			resetPrevious();
		}

		public void setHeight(int height) {
			renderCanvas.setAttribute("height", "" + height);
			resetPrevious();
		}
		
		public void resetPrevious() {
			previous = new int[width * height * 4];
		}
		
		// Lifted from GWT Graphics
	    private native void addNamespaceAndStyle(String ns, String classname) /*-{
	         if (!$doc.namespaces[ns]) {
	            $doc.namespaces.add(ns, "urn:schemas-microsoft-com:vml");
	            // IE8's standards mode doesn't support * selector
	            $doc.createStyleSheet().cssText = "." + classname + "{behavior:url(#default#VML);}";
	         }
	    }-*/;

	}
}
