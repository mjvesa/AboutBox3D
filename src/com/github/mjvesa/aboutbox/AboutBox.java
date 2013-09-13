package com.github.mjvesa.aboutbox;

import com.github.mjvesa.aboutbox.animatedbackground.AnimatedBackground;
import com.github.mjvesa.aboutbox.animatedbackground.AnimatedBackground.Effect;
import com.github.mjvesa.aboutbox.moviescroller.MovieScroller;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.Resource;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AboutBox 
	extends Window
	implements ClickListener, AnimatedBackground.CloseListener {
	
	private static final long serialVersionUID = -7822829984485730681L;

	private String footerHTML;
	
	private String creditsHTML; 
	
	private String applicationName;

	private MovieScroller creditsScroller;
	
	private Label footerLabel;
	
	private AbsoluteLayout layout;
	
	AnimatedBackground animatedBackground;
	
	private int slowMeshColumns;
	private int slowMeshRows;
	private int mediumMeshColumns;
	private int mediumMeshRows;
	private int fastMeshColumns;
	private int fastMeshRows;

	/**
	 * Constructs an AboutBox with sane defaults and a Vaadin logo
	 */
	public AboutBox() {
		
		addStyleName("aboutbox");
		
		layout = new AbsoluteLayout();

		animatedBackground = new AnimatedBackground();		
		animatedBackground.setLogo("vaadin_logo.png", 128, 5, 5, 0, 0, 20, 10, 10);
		animatedBackground.generateMesh();
		animatedBackground.setObjectScale(200);
		animatedBackground.setObjectZ(400);
		animatedBackground.setCloseListener(this);
		layout.addComponent(animatedBackground, "top: 0px; left:0px;");
		
		creditsScroller = new MovieScroller();
		creditsScroller.setFps(20);
		creditsScroller.setScrollSpeed(0.5);
		creditsScroller.setScrollLength(300);
		creditsScroller.addStyleName("creditslabel");
		layout.addComponent(creditsScroller, "top: 30%; left:0px;");
		
		footerLabel = new Label("footer");
		footerLabel.addStyleName("footerlabel");
		layout.addComponent(footerLabel,"bottom: 0px; left:0px;");
		
		setContent(layout);
		addClickListener((ClickListener)this);
	}
	
	/**
	 * This can be used to get the background effect and modify its
	 * properties.
	 * 
	 * @return AnimatedBackground instance that is shown
	 */
	public AnimatedBackground getAnimatedBackground() {
		return animatedBackground;
	}
	
	
	/**
	 * Obtain the credits scrollers shown on the about box.
	 * 
	 * @return MovieScroller the credits scolder
	 */
	public MovieScroller getCreditsScroller() {
		return creditsScroller;
	}

	/**
	 * 
	 * Sets the accuracy at which the 2d image is to be tessellated
	 * to a 3d mesh. This controls the density of the grid that is laid
	 * upon the 2d image and which is then cut and distorted to match 
	 * the 2d shape. The more there are squares in the grid, the closer it will
	 * resemble the shape.
	 * 
	 * This routine also derives the other tessallation accuracies for
	 * medium speed and slow browsers.
	 * 
	 * @param horizontal How many horizontal rectangles
	 * @param vertical How many vertical rectangles
	 */
	public void setTessellationAccuracy(int horizontal, int vertical, double medium, double slow) {
		
		slowMeshColumns = (int) (horizontal * slow);
		slowMeshRows = (int) (vertical * slow);
		mediumMeshColumns = (int) (horizontal * medium);
		mediumMeshRows = (int) (vertical * medium);
		fastMeshColumns = horizontal;
		fastMeshRows = vertical;
		
		setTessellationAccuracy();
	}
	
	
	/*
	 * This performs the actual job of setting the tessellation accuracy. 
	 * It determines the browser that runs this widget and depending on
	 * that it deduces how many polygons the browser can handle. 
	 */
	private void setTessellationAccuracy() {
		
		WebBrowser wb = getUI().getPage().getWebBrowser();
		
		if (wb.isIE()) {
			animatedBackground.setColumns(slowMeshColumns);
			animatedBackground.setRows(slowMeshRows);
		} else if (wb.isChrome() || wb.isSafari()) {
			animatedBackground.setColumns(fastMeshColumns);
			animatedBackground.setRows(fastMeshRows);
		} else if (wb.isOpera()) {
			animatedBackground.setColumns(fastMeshColumns);
			animatedBackground.setRows(fastMeshRows);
		} else if (wb.isFirefox()) {

				if (wb.getBrowserMajorVersion() >= 4) {
					animatedBackground.setColumns(fastMeshColumns);
					animatedBackground.setRows(fastMeshRows);
				} else {
					animatedBackground.setColumns(mediumMeshColumns);
					animatedBackground.setRows(mediumMeshRows);
				}
		} else {
			animatedBackground.setColumns(fastMeshColumns);
			animatedBackground.setRows(fastMeshRows);
		}
		
		animatedBackground.generateMesh();
		
	}
	
	/* Setters and getters for various levels of mesh accuracy*/
	
	public int getSlowMeshColumns() {
		return slowMeshColumns;
	}

	public void setSlowMeshColumns(int slowMeshColumns) {
		this.slowMeshColumns = slowMeshColumns;
	}

	public int getSlowMeshRows() {
		return slowMeshRows;
	}

	public void setSlowMeshRows(int slowMeshRows) {
		this.slowMeshRows = slowMeshRows;
	}

	public int getMediumMeshColumns() {
		return mediumMeshColumns;
	}

	public void setMediumMeshColumns(int mediumMeshColumns) {
		this.mediumMeshColumns = mediumMeshColumns;
	}

	public int getMediumMeshRows() {
		return mediumMeshRows;
	}

	public void setMediumMeshRows(int mediumMeshRows) {
		this.mediumMeshRows = mediumMeshRows;
	}

	public int getFastMeshColumns() {
		return fastMeshColumns;
	}

	public void setFastMeshColumns(int fastMeshColumns) {
		this.fastMeshColumns = fastMeshColumns;
	}

	public int getFastMeshRows() {
		return fastMeshRows;
	}

	public void setFastMeshRows(int fastMeshRows) {
		this.fastMeshRows = fastMeshRows;
	}
	
	/**
	 * Runs the mesh generation routine again. This should be
	 * called when any of the variables that affect mesh generation
	 * are changed. This is done so that the object reflects
	 * those changes.
	 */
	public void regenerateMesh() {
		animatedBackground.generateMesh();
	}

	/**
	 * Sets the effect of the background 3d object
	 * @param effect Effect to be displayed
	 */
	public void setEffect(Effect effect) {
		animatedBackground.setEffect(effect);
	}

	public String getFooterHTML() {
		return footerHTML;
	}
	
	/**
	 * Sets the html content of the footer part.
	 * 
	 * @param footerHTML
	 */
	public void setFooterHTML(String footerHTML) {
		Label newLabel = new Label(footerHTML,Label.CONTENT_XHTML);
		layout.replaceComponent(footerLabel, newLabel);
		footerLabel = newLabel;
		footerLabel.addStyleName("footerlabel");
		this.footerHTML = footerHTML;
	}

	public void setLogo(Resource res) {
		Embedded logo = new Embedded("", res);
		layout.addComponent(logo, "top: 0px; left:0px;");
	}

	public Resource getLogo() {
		return null;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getCreditsHTML() {
		return creditsHTML;
	}
	
	/**
	 * Sets the HTML that is shown scrolling
	 * 
	 * @param creditsHTML
	 */
	public void setCreditsHTML(String creditsHTML) {
		
		creditsScroller.setScrollMessage(creditsHTML);
		this.creditsHTML = creditsHTML;
	}
	
	/**
	 * Sets the amount of pixels after which 
	 * @param length
	 */
	public void setScrollLength(int length) {
		creditsScroller.setScrollLength(length);
	}
	
	public void setVaadinLogoVisible(boolean isVisible) {
		
	}

	public void click(ClickEvent event) {
		animatedBackground.stop();
	}
	
	

	/**
	 * Removes this window from the UI. This is called by the animated background when it has stopped
	 * the timer it has. When this is called it is safe to remove this
	 * window from the UI. 
	 */
	public void animatedBackgroundClosed() {
		getUI().removeWindow(this);
	}
	
	
	
}
