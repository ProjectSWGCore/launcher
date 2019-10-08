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

package com.projectswg.launcher.core.resources.pipeline;

import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.RequiredFile;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus;
import javafx.application.Platform;
import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.json.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UpdateServerUpdater {
	
	private UpdateServerUpdater() {
		
	}
	
	public static void update(UpdateServer server) {
		UpdateServerDownloaderInfo info = new UpdateServerDownloaderInfo(server);
		if (!updateFileList(info))
			return;
		filterValidFiles(info);
		updateServerStatus(info);
	}
	
	/**
	 * Stage 1: Download the file list from the update server, or fall back on the local copy. If neither are accessible, fail.
	 */
	private static boolean updateFileList(UpdateServerDownloaderInfo info) {
		Log.t("Retrieving latest file list from %s...", info.getAddress());
		File localFileList = new File(info.getLocalPath(), "files.json");
		JSONArray files;
		try (JSONInputStream in = new JSONInputStream(createURL(info, "files.json").openConnection().getInputStream())) {
			files = in.readArray();
			try (JSONOutputStream out = new JSONOutputStream(new FileOutputStream(localFileList))) {
				out.writeArray(files);
			} catch (IOException e) {
				Log.e("Failed to write updated file list to disk for update server %s (%s: %s)", info.getName(), e.getClass().getName(), e.getMessage());
			}
		} catch (IOException | JSONException e) {
			Log.w("Failed to retrieve latest file list for update server %s (%s: %s). Falling back on local copy...", e.getClass().getName(), e.getMessage(), info.getName());
			try (JSONInputStream in = new JSONInputStream(new FileInputStream(localFileList))) {
				files = in.readArray();
			} catch (JSONException | IOException t) {
				Log.e("Failed to read file list from disk on update server %s with path %s. Aborting update.", info.getName(), localFileList);
				return false;
			}
		}
		info.setFiles(files.stream().filter(JSONObject.class::isInstance).map(JSONObject.class::cast).map(obj -> jsonObjectToRequiredFile(info, obj)).collect(Collectors.toList()));
		return true;
	}
	
	/**
	 * Stage 2: Scan each file and only keep the ones that need to be downloaded.
	 */
	private static void filterValidFiles(UpdateServerDownloaderInfo info) {
		List<RequiredFile> files = Objects.requireNonNull(info.getFiles(), "File list was not read correctly");
		Log.d("%d known files. Scanning...", files.size());
		int total = files.size();
		Platform.runLater(() -> info.getServer().setStatus(UpdateServerStatus.SCANNING));
		files.removeIf(UpdateServerUpdater::isValidFile);
		int valid = total - files.size();
		Log.d("Completed scan of update server %s. %d of %d valid.", info.getName(), valid, total);
	}
	
	/**
	 * Stage 3: Update the UpdateServer status and the required files.
	 */
	private static void updateServerStatus(UpdateServerDownloaderInfo info) {
		List<RequiredFile> serverList = info.getServer().getRequiredFiles();
		List<RequiredFile> updateList = info.getFiles();
		UpdateServerStatus updateStatus = updateList.isEmpty() ? UpdateServerStatus.READY : UpdateServerStatus.REQUIRES_DOWNLOAD;
		
		serverList.clear();
		serverList.addAll(updateList);
		Platform.runLater(() -> info.getServer().setStatus(updateStatus));
		Log.d("Setting update server '%s' status to %s", info.getName(), updateStatus);
	}
	
	private static boolean isValidFile(RequiredFile file) {
		File localFile = file.getLocalPath();
		long length = localFile.length();
		return localFile.isFile() && length == file.getLength();
	}
	
	private static RequiredFile jsonObjectToRequiredFile(UpdateServerDownloaderInfo info, JSONObject obj) {
		String path = obj.getString("path");
		try {
			return new RequiredFile(new File(info.getLocalPath(), path), createURL(info, path), obj.getLong("length"), obj.getLong("xxhash"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static URL createURL(UpdateServerDownloaderInfo info, String path) throws MalformedURLException {
		String basePath = info.getBasePath();
		while (basePath.endsWith("/"))
			basePath = basePath.substring(0, basePath.length()-1);
		if (!path.startsWith("/"))
			path = "/" + path;
		basePath += path;
		return new URL("http", info.getAddress(), info.getPort(), basePath);
	}
	
	private static class UpdateServerDownloaderInfo {
		
		private final UpdateServer server;
		private final String updateServerName;
		private final String updateServerAddress;
		private final int updateServerPort;
		private final String updateServerBasePath;
		private final File updateServerLocalPath;
		
		private List<RequiredFile> files;
		
		public UpdateServerDownloaderInfo(UpdateServer server) {
			this.server = server;
			this.updateServerName = server.getName();
			this.updateServerAddress = server.getAddress();
			this.updateServerPort = server.getPort();
			this.updateServerBasePath = server.getBasePath();
			this.updateServerLocalPath = new File(server.getLocalPath());
			this.files = null;
		}
		
		public UpdateServer getServer() {
			return server;
		}
		
		public List<RequiredFile> getFiles() {
			return files;
		}
		
		public String getName() {
			return updateServerName;
		}
		
		public String getAddress() {
			return updateServerAddress;
		}
		
		public int getPort() {
			return updateServerPort;
		}
		
		public String getBasePath() {
			return updateServerBasePath;
		}
		
		public File getLocalPath() {
			return updateServerLocalPath;
		}
		
		public void setFiles(List<RequiredFile> files) {
			this.files = files;
		}
	}
	
}
