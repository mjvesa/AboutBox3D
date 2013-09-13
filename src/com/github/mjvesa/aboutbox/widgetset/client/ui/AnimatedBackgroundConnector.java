package com.github.mjvesa.aboutbox.widgetset.client.ui;

import com.github.mjvesa.aboutbox.animatedbackground.AnimatedBackground;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;


@Connect(AnimatedBackground.class)
public class AnimatedBackgroundConnector extends LegacyConnector {

	
	@Override
	public VAnimatedBackground getWidget() {
		return (VAnimatedBackground)super.getWidget();
	}
}
