package com.github.mjvesa.aboutbox3d.widgetset.client.ui;

import com.github.mjvesa.aboutbox3d.widgetset.AnimatedBackground;
import com.github.mjvesa.aboutbox3d.widgetset.client.ui.VAnimatedBackground;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@Connect(AnimatedBackground.class)
public class AnimatedBackgroundConnector extends LegacyConnector {

	
	private static final long serialVersionUID = -8484804541229303415L;

	@Override
	public VAnimatedBackground getWidget() {
		return (VAnimatedBackground)super.getWidget();
	}
}
