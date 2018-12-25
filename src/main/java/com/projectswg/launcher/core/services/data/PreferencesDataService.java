/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * This file is part of the ProjectSWG Launcher.                                   *
 *                                                                                 *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 *                                                                                 *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.          *
 *                                                                                 *
 ***********************************************************************************/

package com.projectswg.launcher.core.services.data;

import com.projectswg.launcher.core.resources.data.LauncherData;
import com.projectswg.launcher.core.resources.data.forwarder.ForwarderData;
import com.projectswg.launcher.core.resources.data.general.GeneralData;
import com.projectswg.launcher.core.resources.data.general.LauncherTheme;
import com.projectswg.launcher.core.resources.data.login.LoginData;
import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateData;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesDataService extends Service {
	
	private final Preferences preferences;
	private final ScheduledThreadPool executor;
	
	public PreferencesDataService() {
		this.preferences = LauncherData.INSTANCE.getPreferences();
		this.executor = new ScheduledThreadPool(1, 3, "data-executor-%d");
		loadPreferences();
	}
	
	@Override
	public boolean start() {
		createDefaults();
		executor.start();
		executor.executeWithFixedDelay(5*60000, 5*60000, this::savePreferences);
		return true;
	}
	
	@Override
	public boolean stop() {
		savePreferences();
		executor.stop();
		return executor.awaitTermination(1000);
	}
	
	private void createDefaults() {
		createPSWG();
		createTeamSWG();

		if (LauncherData.INSTANCE.getGeneral().getWine() == null || LauncherData.INSTANCE.getGeneral().getWine().isEmpty())
			LauncherData.INSTANCE.getGeneral().setWine(getWinePath());
	}
	
	private void createPSWG() {
		UpdateServer pswgUpdateServer = LauncherData.INSTANCE.getUpdate().getServers().stream().filter(s -> s.getName().equals("ProjectSWG")).findFirst().orElse(null);
		if (pswgUpdateServer == null) {
			pswgUpdateServer = new UpdateServer("ProjectSWG");
			pswgUpdateServer.setAddress("login1.projectswg.com");
			pswgUpdateServer.setPort(80);
			pswgUpdateServer.setBasePath("/launcher/patch");
			pswgUpdateServer.setGameVersion("NGE");
			LauncherData.INSTANCE.getUpdate().addServer(pswgUpdateServer);
		} else if (pswgUpdateServer.getGameVersion().isEmpty()) {
			// Migrate existing persisted update servers
			pswgUpdateServer.setGameVersion("NGE");
		}
		if (LauncherData.INSTANCE.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("ProjectSWG"))) {
			LoginServer defaultLive = new LoginServer("ProjectSWG");
			defaultLive.setAddress("login1.projectswg.com");
			defaultLive.setPort(44453);
			defaultLive.setUpdateServer(pswgUpdateServer);
			LauncherData.INSTANCE.getLogin().addServer(defaultLive);
		}
		
		if (LauncherData.INSTANCE.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("localhost"))) {
			LoginServer defaultLocalhost = new LoginServer("localhost");
			defaultLocalhost.setAddress("localhost");
			defaultLocalhost.setPort(44463);
			defaultLocalhost.setUpdateServer(pswgUpdateServer);
			LauncherData.INSTANCE.getLogin().addServer(defaultLocalhost);
		}
	}
	
	private void createTeamSWG() {
		UpdateServer teamswgUpdateServer = LauncherData.INSTANCE.getUpdate().getServers().stream().filter(s -> s.getName().equals("TeamSWG")).findFirst().orElse(null);
		
		if (teamswgUpdateServer == null) {
			teamswgUpdateServer = new UpdateServer("TeamSWG");
			teamswgUpdateServer.setAddress("patch.teamswg.com");
			teamswgUpdateServer.setPort(80);
			teamswgUpdateServer.setBasePath("/launcher/patch");
			teamswgUpdateServer.setGameVersion("CU");
			LauncherData.INSTANCE.getUpdate().addServer(teamswgUpdateServer);
		}
		
		if (LauncherData.INSTANCE.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("Constrictor"))) {
			LoginServer constrictor = new LoginServer("Constrictor");
			constrictor.setAddress("game.teamswg.com");
			constrictor.setPort(44463);
			constrictor.setUpdateServer(teamswgUpdateServer);
			LauncherData.INSTANCE.getLogin().addServer(constrictor);
		}
	}
	
	private synchronized void loadPreferences() {
		try {
			loadGeneralPreferences(LauncherData.INSTANCE.getGeneral());
			loadUpdatePreferences(LauncherData.INSTANCE.getUpdate());
			loadLoginPreferences(LauncherData.INSTANCE.getLogin());
			loadForwarderPreferences(LauncherData.INSTANCE.getForwarderData());
		} catch (BackingStoreException e) {
			Log.w(e);
		}
	}
	
	private synchronized void savePreferences() {
		try {
			saveGeneralPreferences(LauncherData.INSTANCE.getGeneral());
			saveUpdatePreferences(LauncherData.INSTANCE.getUpdate());
			saveLoginPreferences(LauncherData.INSTANCE.getLogin());
			saveForwarderPreferences(LauncherData.INSTANCE.getForwarderData());
			preferences.flush();
		} catch (BackingStoreException e) {
			Log.w(e);
		}
	}
	
	private void loadGeneralPreferences(GeneralData generalData) {
		Preferences generalPreferences = preferences.node("general");
		ifPresent(generalPreferences, "sound", Boolean::valueOf, generalData::setSound);
		ifPresent(generalPreferences, "theme", LauncherTheme::forThemeTag, generalData::setTheme);
		ifPresent(generalPreferences, "locale", Locale::forLanguageTag, generalData::setLocale);
		ifPresent(generalPreferences, "wine", generalData::setWine);
		ifPresent(generalPreferences, "admin", Boolean::valueOf, generalData::setAdmin);
	}
	
	private void saveGeneralPreferences(GeneralData generalData) {
		Preferences generalPreferences = preferences.node("general");
		generalPreferences.putBoolean("sound", generalData.isSound());
		generalPreferences.put("theme", generalData.getTheme().getTag());
		generalPreferences.put("locale", generalData.getLocale().toLanguageTag());
		generalPreferences.putBoolean("admin", generalData.isAdmin());
		String wine = generalData.getWine();
		if (wine != null)
			generalPreferences.put("wine", wine);
	}
	
	private void loadLoginPreferences(LoginData loginData) throws BackingStoreException {
		Preferences loginPreferences = preferences.node("login");
		for (String childNodeName : loginPreferences.childrenNames()) {
			Preferences loginServerPreferences = loginPreferences.node(childNodeName);
			LoginServer server = new LoginServer(childNodeName);
			ifPresent(loginServerPreferences, "address", server::setAddress);
			ifPresent(loginServerPreferences, "port", Integer::parseInt, server::setPort);
			ifPresent(loginServerPreferences, "username", server::setUsername);
			ifPresent(loginServerPreferences, "password", server::setPassword);
			ifPresent(loginServerPreferences, "updateServer", name -> server.setUpdateServer(LauncherData.INSTANCE.getUpdate().getServers().stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null)));
			ifPresent(loginServerPreferences, "verifyServer", Boolean::parseBoolean, server::setVerifyServer);
			loginData.getServers().add(server);
		}
	}
	
	private void saveLoginPreferences(LoginData loginData) throws BackingStoreException {
		preferences.node("login").removeNode();
		Preferences loginPreferences = preferences.node("login");
		for (LoginServer server : loginData.getServers()) {
			Preferences loginServerPreferences = loginPreferences.node(server.getName());
			loginServerPreferences.put("address", server.getAddress());
			loginServerPreferences.putInt("port", server.getPort());
			loginServerPreferences.put("username", server.getUsername());
			loginServerPreferences.put("password", server.getPassword());
			loginServerPreferences.putBoolean("verifyServer", server.isVerifyServer());
			UpdateServer updateServer = server.getUpdateServer();
			if (updateServer != null)
				loginServerPreferences.put("updateServer", updateServer.getName());
		}
	}
	
	private void loadUpdatePreferences(UpdateData updateData) throws BackingStoreException {
		Preferences updatePreferences = preferences.node("update");
		for (String childNodeName : updatePreferences.childrenNames()) {
			Preferences updateServerPreferences = updatePreferences.node(childNodeName);
			UpdateServer server = new UpdateServer(childNodeName);
			ifPresent(updateServerPreferences, "address", server::setAddress);
			ifPresent(updateServerPreferences, "port", Integer::parseInt, server::setPort);
			ifPresent(updateServerPreferences, "basePath", server::setBasePath);
			ifPresent(updateServerPreferences, "localPath", server::setLocalPath);
			ifPresent(updateServerPreferences, "gameVersion", server::setGameVersion);
			updateData.getServers().add(server);
		}
	}
	
	private void saveUpdatePreferences(UpdateData updateData) throws BackingStoreException {
		preferences.node("update").removeNode();
		Preferences updatePreferences = preferences.node("update");
		for (UpdateServer server : updateData.getServers()) {
			Preferences updateServerPreferences = updatePreferences.node(server.getName());
			updateServerPreferences.put("address", server.getAddress());
			updateServerPreferences.putInt("port", server.getPort());
			updateServerPreferences.put("basePath", server.getBasePath());
			updateServerPreferences.put("localPath", server.getLocalPath());
			updateServerPreferences.put("gameVersion", server.getGameVersion());
		}
	}
	
	private void loadForwarderPreferences(ForwarderData forwarderData) {
		Preferences forwarderPreferences = preferences.node("forwarder");
		int version = forwarderPreferences.getInt("version", 0);
		if (version < 1) {
			forwarderData.setSendInterval(ForwarderData.DEFAULT_SEND_INTERVAL);
			forwarderData.setSendMax(ForwarderData.DEFAULT_SEND_MAX);
		} else {
			ifPresent(forwarderPreferences, "sendInterval", Integer::valueOf, forwarderData::setSendInterval);
			ifPresent(forwarderPreferences, "sendMax", Integer::valueOf, forwarderData::setSendMax);
		}
	}
	
	private void saveForwarderPreferences(ForwarderData forwarderData) {
		Preferences forwarderPreferences = preferences.node("forwarder");
		forwarderPreferences.putInt("sendInterval", forwarderData.getSendInterval());
		forwarderPreferences.putInt("sendMax", forwarderData.getSendMax());
		forwarderPreferences.putInt("version", 1);
	}
	
	private static <T> void ifPresent(Preferences p, String key, Function<String, T> transform, Consumer<T> setter) {
		String val = p.get(key, null);
		if (val != null)
			setter.accept(transform.apply(val));
	}
	
	private static void ifPresent(Preferences p, String key, Consumer<String> setter) {
		String val = p.get(key, null);
		if (val != null)
			setter.accept(val);
	}
	
	private static String getWinePath() {
		String pathStr = System.getenv("PATH");
		if (pathStr == null)
			return null;
		
		for (String path : pathStr.split(File.pathSeparator)) {
			Log.t("Testing wine binary at %s", path);
			File test = new File(path, "wine");
			if (test.isFile()) {
				try {
					test = test.getCanonicalFile();
					Log.d("Found wine installation. Location: %s", test);
					return test.getAbsolutePath();
				} catch (IOException e) {
					Log.w("Failed to get canonical file location of possible wine location: %s", test);
				}
			}
		}
		return null;
	}
	
}
