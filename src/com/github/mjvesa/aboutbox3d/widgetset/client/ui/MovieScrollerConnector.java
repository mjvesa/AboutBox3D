package com.github.mjvesa.aboutbox3d.widgetset.client.ui;

import com.github.mjvesa.aboutbox3d.widgetset.MovieScroller;
import com.github.mjvesa.aboutbox3d.widgetset.client.ui.VMovieScroller;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@Connect(MovieScroller.class)
public class MovieScrollerConnector extends LegacyConnector {

	
	private static final long serialVersionUID = 2806690516431516035L;

	@Override
	public VMovieScroller getWidget() {
		return (VMovieScroller)super.getWidget();
	}
}
