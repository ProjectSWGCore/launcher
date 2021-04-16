open module com.projectswg.launcher {
	requires jdk.crypto.ec;
	
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
	
	requires java.prefs;
	requires tornadofx;
	requires de.jensd.fx.glyphs.commons;
	requires de.jensd.fx.glyphs.fontawesome;
	requires kotlin.stdlib;
	requires kotlin.reflect;
	
	exports com.projectswg.launcher.core.resources.data.forwarder;
}
