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
	
	private final LauncherData data;
	private final Preferences preferences;
	private final ScheduledThreadPool executor;
	
	public PreferencesDataService() {
		this.data = LauncherData.getInstance();
		this.preferences = data.getPreferences();
		this.executor = new ScheduledThreadPool(1, 3, "data-executor-%d");
	}
	
	@Override
	public boolean initialize() {
		loadPreferences();
		createDefaults();
		executor.start();
		executor.executeWithFixedDelay(5*60000, 5*60000, this::savePreferences);
		return true;
	}
	
	@Override
	public boolean terminate() {
		executor.stop();
		executor.awaitTermination(1000);
		savePreferences();
		return true;
	}
	
	private void createDefaults() {
		createPSWG();
		createTeamSWG();

		if (data.getGeneral().getWine() == null || data.getGeneral().getWine().isEmpty())
			data.getGeneral().setWine(getWinePath());
	}
	
	private void createPSWG() {
		UpdateServer pswgUpdateServer = data.getUpdate().getServers().stream().filter(s -> s.getName().equals("ProjectSWG")).findFirst().orElse(null);
		if (pswgUpdateServer == null) {
			pswgUpdateServer = new UpdateServer("ProjectSWG");
			pswgUpdateServer.setAddress("login1.projectswg.com");
			pswgUpdateServer.setPort(80);
			pswgUpdateServer.setBasePath("/launcher/patch");
			pswgUpdateServer.setGameVersion("NGE");
			data.getUpdate().addServer(pswgUpdateServer);
		} else if (pswgUpdateServer.getGameVersion().isEmpty()) {
			// Migrate existing persisted update servers
			pswgUpdateServer.setGameVersion("NGE");
		}
		if (data.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("ProjectSWG"))) {
			LoginServer defaultLive = new LoginServer("ProjectSWG");
			defaultLive.setAddress("login1.projectswg.com");
			defaultLive.setPort(44453);
			defaultLive.setUpdateServer(pswgUpdateServer);
			data.getLogin().addServer(defaultLive);
		}
		
		if (data.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("localhost"))) {
			LoginServer defaultLocalhost = new LoginServer("localhost");
			defaultLocalhost.setAddress("localhost");
			defaultLocalhost.setPort(44463);
			defaultLocalhost.setUpdateServer(pswgUpdateServer);
			data.getLogin().addServer(defaultLocalhost);
		}
	}
	
	private void createTeamSWG() {
		UpdateServer teamswgUpdateServer = data.getUpdate().getServers().stream().filter(s -> s.getName().equals("TeamSWG")).findFirst().orElse(null);
		
		if (teamswgUpdateServer == null) {
			teamswgUpdateServer = new UpdateServer("TeamSWG");
			teamswgUpdateServer.setAddress("patch.teamswg.com");
			teamswgUpdateServer.setPort(80);
			teamswgUpdateServer.setBasePath("/launcher/patch");
			teamswgUpdateServer.setGameVersion("CU");
			data.getUpdate().addServer(teamswgUpdateServer);
		}
		
		if (data.getLogin().getServers().stream().noneMatch(s -> s.getName().equals("Constrictor"))) {
			LoginServer constrictor = new LoginServer("Constrictor");
			constrictor.setAddress("game.teamswg.com");
			constrictor.setPort(44463);
			constrictor.setUpdateServer(teamswgUpdateServer);
			data.getLogin().addServer(constrictor);
		}
	}
	
	private void loadPreferences() {
		try {
			loadGeneralPreferences(data.getGeneral());
			loadUpdatePreferences(data.getUpdate());
			loadLoginPreferences(data.getLogin());
			loadForwarderPreferences(data.getForwarderData());
		} catch (BackingStoreException e) {
			Log.w(e);
		}
	}
	
	private void savePreferences() {
		try {
			saveGeneralPreferences(data.getGeneral());
			saveUpdatePreferences(data.getUpdate());
			saveLoginPreferences(data.getLogin());
			saveForwarderPreferences(data.getForwarderData());
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
	}
	
	private void saveGeneralPreferences(GeneralData generalData) {
		Preferences generalPreferences = preferences.node("general");
		generalPreferences.putBoolean("sound", generalData.isSound());
		generalPreferences.put("theme", generalData.getTheme().getTag());
		generalPreferences.put("locale", generalData.getLocale().toLanguageTag());
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
			ifPresent(loginServerPreferences, "updateServer", name -> server.setUpdateServer(data.getUpdate().getServers().stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null)));
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
		Preferences generalPreferences = preferences.node("forwarder");
		ifPresent(generalPreferences, "sendInterval", Integer::valueOf, forwarderData::setSendInterval);
		ifPresent(generalPreferences, "sendMax", Integer::valueOf, forwarderData::setSendMax);
	}
	
	private void saveForwarderPreferences(ForwarderData forwarderData) {
		Preferences generalPreferences = preferences.node("forwarder");
		generalPreferences.putInt("sendInterval", forwarderData.getSendInterval());
		generalPreferences.putInt("sendMax", forwarderData.getSendMax());
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
