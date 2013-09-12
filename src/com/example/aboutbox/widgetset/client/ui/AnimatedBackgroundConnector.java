package com.example.aboutbox.widgetset.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addons.aboutbox.animatedbackground.AnimatedBackground;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;


@Connect(AnimatedBackground.class)
public class AnimatedBackgroundConnector extends LegacyConnector {

	
	@Override
	public VAnimatedBackground getWidget() {
		return (VAnimatedBackground)super.getWidget();
	}
}
