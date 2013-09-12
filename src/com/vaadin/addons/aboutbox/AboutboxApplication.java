package com.vaadin.addons.aboutbox;

import com.gargoylesoftware.htmlunit.Page;
import com.vaadin.addons.aboutbox.animatedbackground.AnimatedBackground.Effect;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("aboutbox")
public class AboutboxApplication extends UI {

	private static final long serialVersionUID = 5853529673557124301L;

	@Override
	protected void init(VaadinRequest request) {
		getPage().setTitle("AboutBox demo");
		
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(new Button("Open AboutBox", new Button.ClickListener() {
			
			private static final long serialVersionUID = 5405789614881134772L;

			@Override
			public void buttonClick(ClickEvent event) {
				AboutBox ab = new AboutBox();
				ab.setWidth("400px");
				ab.setHeight("400px");
				ab.setLogo(new ThemeResource("img/vaadin.png"));
				ab.setCreditsHTML("<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>" +
						"<br/><br/><br/><br/><br/><h1>Credits</h1><p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum <a href=\"http://vaadin.com\" target=\"_blank\">w0rd.</a><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>");
				ab.setScrollLength(400);
				ab.setFooterHTML("<h3>Footer</h3>");
				ab.setEffect(Effect.LOGO3D);
				getUI().addWindow(ab);
				ab.setTessellationAccuracy(30, 30, 0.6, 0.50);
				ab.getAnimatedBackground().setEdgeAAEnabled(true);
				ab.getAnimatedBackground().setTemporalAAEnabled(false);
				ab.center();
				
			}
		}));
		setContent(vl);
		
	}
}
