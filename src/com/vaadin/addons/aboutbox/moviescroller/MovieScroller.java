package com.vaadin.addons.aboutbox.moviescroller;

import java.util.Map;

import com.example.aboutbox.widgetset.client.ui.MovieScrollerConnector;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.shared.ui.Connect;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;

/**
 * Server side component for the VMovieScoller widget.
 */
public class MovieScroller extends AbstractComponent implements LegacyComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -803926010131093393L;
	private String scrollMessage = "";
	private int fps = 20;
	private double scrollSpeed = 1;
	private int scrollLength = 500;
	
	public String getScrollMessage() {
		return scrollMessage;
	}

	public void setScrollMessage(String scrollMessage) {
		this.scrollMessage = scrollMessage;
		requestRepaint();
	}

	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
		requestRepaint();
	}

	public double getScrollSpeed() {
		return scrollSpeed;
	}

	public void setScrollSpeed(double scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
		requestRepaint();
	}
	
	public int getScrollLength() {
		return scrollLength;
	}

	public void setScrollLength(int scrollLength) {
		this.scrollLength = scrollLength;
		requestRepaint();
	}

	public void paintContent(PaintTarget target) throws PaintException {
//		super.paintContent(target);

		target.addAttribute("scrollMessage", scrollMessage);
		target.addAttribute("fps", fps);
		target.addAttribute("scrollSpeed", scrollSpeed);
		target.addAttribute("scrollLength", scrollLength);
		
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	//@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		//super.changeVariables(source, variables);
	}

}
