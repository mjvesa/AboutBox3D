package com.github.mjvesa.aboutbox.widgetset.client.ui;

import com.github.mjvesa.aboutbox.widgetset.client.ui.VAnimatedBackground;
import com.github.mjvesa.aboutbox.widgetset.AnimatedBackground;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@StyleSheet("vaadin://addons/aboutbox/styles.css")
@Connect(AnimatedBackground.class)
public class AnimatedBackgroundConnector extends LegacyConnector {

	
	private static final long serialVersionUID = -8484804541229303415L;

	@Override
	public VAnimatedBackground getWidget() {
		return (VAnimatedBackground)super.getWidget();
	}
}
