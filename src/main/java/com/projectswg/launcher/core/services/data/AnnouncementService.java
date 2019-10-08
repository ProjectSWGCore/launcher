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

import com.projectswg.common.utilities.LocalUtilities;
import com.projectswg.launcher.core.resources.data.LauncherData;
import com.projectswg.launcher.core.resources.data.announcements.AnnouncementsData;
import com.projectswg.launcher.core.resources.data.announcements.CardData;
import com.projectswg.launcher.core.resources.gui.Card;
import javafx.application.Platform;
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.json.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class AnnouncementService extends Service {
	
	private static final Map<String, String> VARIABLES = new HashMap<>();
	
	static {
		VARIABLES.put("\\$\\{LAUNCHER.VERSION\\}", LauncherData.VERSION);
	}
	
	private final ScheduledThreadPool executor;
	
	public AnnouncementService() {
		this.executor = new ScheduledThreadPool(1, "announcement-service");
	}
	
	@Override
	public boolean start() {
		try (JSONInputStream jsonInputStream = new JSONInputStream(new FileInputStream(new File(LocalUtilities.getApplicationDirectory(), "announcements.json")))) {
			JSONObject announcements = jsonInputStream.readObject();
			update(announcements);
		} catch (IOException | JSONException e) {
			Log.w("Failed to load announcements from disk");
		}
		
		executor.start();
		executor.executeWithFixedDelay(3000, TimeUnit.MINUTES.toMillis(30), this::update);
		return true;
	}
	
	@Override
	public boolean stop() {
		executor.stop();
		executor.awaitTermination(1000);
		return true;
	}
	
	private void update() {
		JSONObject announcements = updateAnnouncements();
		if (announcements == null)
			return;
		update(announcements);
	}
	
	private void update(JSONObject announcements) {
		List<CardData> announcementCards = parseCards(announcements.getArray("announcements")).stream().map(this::downloadImage).collect(Collectors.toList());
		List<CardData> serverCards = parseCards(announcements.getArray("servers")).stream().map(this::downloadImage).collect(Collectors.toList());
		
		Platform.runLater(() -> {
			AnnouncementsData data = LauncherData.INSTANCE.getAnnouncements();
			data.getAnnouncementCards().clear();
			data.getAnnouncementCards().addAll(announcementCards);
			data.getServerListCards().clear();
			data.getServerListCards().addAll(serverCards);
		});
	}
	
	private List<CardData> parseCards(JSONArray descriptor) {
		if (descriptor == null)
			return Collections.emptyList();
		
		return descriptor.stream().filter(JSONObject.class::isInstance).map(JSONObject.class::cast).filter(AnnouncementService::validateCard).map(this::parseCard).collect(Collectors.toList());
	}
	
	private CardData parseCard(JSONObject obj) {
		String imageUrl = obj.getString("image");
		String title = obj.getString("title");
		String description = obj.getString("description");
		String link = obj.getString("link");
		title = title == null ? "" : parseVariables(title);
		description = description == null ? "" : parseVariables(description);
		
		return new CardData(imageUrl, title, description, link);
	}
	
	private CardData downloadImage(CardData card) {
		String url = card.getImageUrl();
		if (url == null)
			return card;
		int lastSlash = url.lastIndexOf('/');
		if (lastSlash == -1)
			return new CardData(null, card.getTitle(), card.getDescription(), card.getLink()); // Invalid url
		
		File cards = new File(LocalUtilities.getApplicationDirectory(), "cards");
		if (!cards.isDirectory() && !cards.mkdir())
			Log.w("Could not create card directory");
		File destination = new File(cards, Integer.toHexString(url.hashCode()));
		download(url, destination);
		return new CardData(destination.getAbsolutePath(), card.getTitle(), card.getDescription(), card.getLink());
	}
	
	private static void download(String url, File destination) {
		if (destination.isFile())
			return;
		Log.d("Downloading image '%s' to '%s'", url, destination);
		try {
			new URL(url).openStream().transferTo(new FileOutputStream(destination));
			Log.t("Completed download of %s", destination);
		} catch (IOException e) {
			Log.e("Failed to download file %s from %s with error: %s: %s", destination, url, e.getClass().getName(), e.getMessage());
		}
	}
	
	private static boolean validateCard(JSONObject obj) {
		JSONObject filter = obj.getObject("filter");
		if (filter == null)
			return true;
		String os = filter.getString("os");
		if (os != null) {
			String currentOs = System.getProperty("os.name").toLowerCase(Locale.US);
			switch (os.toLowerCase(Locale.US)) {
				case "windows":
					if (!currentOs.contains("win"))
						return false;
					break;
				case "mac":
					if (!currentOs.contains("mac"))
						return false;
					break;
				case "linux":
					if (currentOs.contains("win") || currentOs.contains("mac"))
						return false;
					break;
			}
		}
		// Inclusive
		
		return passesVersionCheck(filter.getString("minLauncherVersion"), (cur, b) -> cur >= b, true) && passesVersionCheck(filter.getString("maxLauncherVersion"), (cur, b) -> cur < b, false);
	}
	
	private static String parseVariables(String str) {
		for (Entry<String, String> var : VARIABLES.entrySet()) {
			str = str.replaceAll(var.getKey(), var.getValue());
		}
		return str;
	}
	
	private static boolean passesVersionCheck(String specifiedVersionStr, BiPredicate<Integer, Integer> check, boolean def) {
		if (specifiedVersionStr == null)
			return true;
		String [] currentVersion = LauncherData.VERSION.split("\\.");
		String [] specifiedVersion = specifiedVersionStr.split("\\.");
		for (int i = 0; i < currentVersion.length && i < specifiedVersion.length; i++) {
			int cur = Integer.parseUnsignedInt(currentVersion[i]);
			int spec = Integer.parseUnsignedInt(specifiedVersion[i]);
			if (cur == spec)
				continue;
			return check.test(cur, spec);
		}
		return def;
	}
	
	/**
	 * Stage 1: Download the file list from the update server, or fall back on the local copy. If neither are accessible, fail.
	 */
	private static JSONObject updateAnnouncements() {
		File localFileList = new File(LocalUtilities.getApplicationDirectory(), "announcements.json");
		
		Log.t("Retrieving latest announcements...");
		JSONObject announcements;
		try (JSONInputStream in = new JSONInputStream(new URL("http", LauncherData.UPDATE_ADDRESS, 80, "/launcher/announcements.json").openStream())) {
			announcements = in.readObject();
			try (JSONOutputStream out = new JSONOutputStream(new FileOutputStream(localFileList))) {
				out.writeObject(announcements);
			} catch (IOException e) {
				Log.e("Failed to write updated announcements to disk. %s: %s", e.getClass().getName(), e.getMessage());
			}
		} catch (IOException | JSONException e) {
			Log.w("Failed to retrieve latest announcements. Falling back on local copy...");
			try (JSONInputStream in = new JSONInputStream(new FileInputStream(localFileList))) {
				announcements = in.readObject();
			} catch (JSONException | IOException t) {
				Log.e("Failed to read announcements from disk. %s: %s", t.getClass().getName(), t.getMessage());
				return null;
			}
		}
		return announcements;
	}
	
}
