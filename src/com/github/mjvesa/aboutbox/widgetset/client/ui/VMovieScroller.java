package com.github.mjvesa.aboutbox.widgetset.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;

/**
 * A scroller that scrolls vertically at a defined speed.
 */
public class VMovieScroller extends Widget implements Paintable, ClickHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-moviescroller";

	public static final String CLICK_EVENT_IDENTIFIER = "click";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	private String scrollMessage = "Hello";
	private int fps = 20;
	private double scrollSpeed = 0.1; 
	private int scrollLength = 500;
	private Timer scrollTimer;
	private double t = 0;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VMovieScroller() {
		setElement(Document.get().createDivElement());
		
		// This method call of the Paintable interface sets the component
		// style name in DOM tree
		setStyleName(CLASSNAME);
		
		// Tell GWT we are interested in receiving click events
		sinkEvents(Event.ONCLICK);
		// Add a handler for the click events (this is similar to FocusWidget.addClickHandler())
		addDomHandler(this, ClickEvent.getType());
		
		scrollTimer = new Timer() {
			public void run() {
			
				t += scrollSpeed;
				if (t > scrollLength) {
					t -= scrollLength;
				}
				getElement().setScrollTop((int)t);
			}
		};
		
		scrollTimer.scheduleRepeating(1000 / fps);
		
		getElement().setInnerHTML("HELLO");
	}

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

		if (uidl.hasAttribute("scrollMessage")) {
			scrollMessage = uidl.getStringAttribute("scrollMessage");
			getElement().setInnerHTML(scrollMessage);
		}
		if (uidl.hasAttribute("fps")) {
			fps = uidl.getIntAttribute("fps");
			scrollTimer.scheduleRepeating(1000 / fps);
		}
		if (uidl.hasAttribute("scrollSpeed")) {
			scrollSpeed = uidl.getDoubleAttribute("scrollSpeed");
		}

		if (uidl.hasAttribute("scrollLength")) {
			scrollLength = uidl.getIntAttribute("scrollLength");
		}
	}

    /**
     * Called when a native click event is fired.
     * 
     * @param event
     *            the {@link ClickEvent} that was fired
     */
     public void onClick(ClickEvent event) {
		// client.updateVariable(paintableId, CLICK_EVENT_IDENTIFIER, button, true);
	}
}
