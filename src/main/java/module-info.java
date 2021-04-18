open module com.projectswg.launcher {
	requires javafx.controls;
	requires javafx.fxml;
	requires me.joshlarson.jlcommon;
	requires me.joshlarson.jlcommon.javafx;
	requires fast.json;
	requires net.openhft.hashing;
	requires org.bouncycastle.provider;
	requires org.jetbrains.annotations;
	
	requires com.projectswg.common;
	requires com.projectswg.forwarder;
	requires com.projectswg.holocore.client;
	
	requires jdk.crypto.ec;
	requires java.prefs;
	requires java.net.http;
	requires java.xml;
	
	requires tornadofx;
	requires de.jensd.fx.glyphs.commons;
	requires de.jensd.fx.glyphs.fontawesome;
	requires com.rometools.rome;
	requires org.apache.commons.text;
	
	requires kotlin.stdlib;
	requires kotlin.reflect;
	
	exports com.projectswg.launcher.core.resources.data.forwarder;
}
