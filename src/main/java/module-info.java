module com.projectswg.launcher {
	requires javafx.controls;
	requires javafx.fxml;
	requires me.joshlarson.jlcommon;
	requires me.joshlarson.jlcommon.javafx;
	requires fast.json;
	requires zero.allocation.hashing;
	requires de.jensd.fx.glyphs.fontawesome;
	requires org.bouncycastle.provider;
	
	requires com.projectswg.common;
	requires com.projectswg.forwarder;
	requires com.projectswg.holocore.client;
	
	requires java.prefs;
	
	opens strings;
	opens css;
	opens graphics;
	opens graphics.headers;
	opens fxml;
	opens com.projectswg.launcher.core.resources.gui;
	opens com.projectswg.launcher.core.resources.gui.servers;
	opens com.projectswg.launcher.core.resources.gui.settings;
	opens com.projectswg.launcher.core.services.data;
	opens com.projectswg.launcher.core.services.launcher;
}
