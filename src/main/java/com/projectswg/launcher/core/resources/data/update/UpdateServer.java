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

package com.projectswg.launcher.core.resources.data.update;

import me.joshlarson.jlcommon.javafx.beans.ConcurrentInteger;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentList;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentReference;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentString;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public class UpdateServer {
	
	private final String name;
	private final ConcurrentString address;
	private final ConcurrentInteger port;
	private final ConcurrentString basePath;
	private final ConcurrentString localPath;
	private final ConcurrentList<RequiredFile> requiredFiles;
	private final ConcurrentReference<UpdateServerStatus> status;
	private final ConcurrentString gameVersion;
	
	public UpdateServer(@NotNull String name) {
		this.name = name;
		this.address = new ConcurrentString("");
		this.port = new ConcurrentInteger(0);
		this.basePath = new ConcurrentString("");
		this.localPath = new ConcurrentString("");
		this.requiredFiles = new ConcurrentList<>();
		this.status = new ConcurrentReference<>(UpdateServerStatus.UNKNOWN);
		this.gameVersion = new ConcurrentString("");
	}
	
	@NotNull
	public ConcurrentString getAddressProperty() {
		return address;
	}
	
	@NotNull
	public ConcurrentInteger getPortProperty() {
		return port;
	}
	
	@NotNull
	public ConcurrentString getUsernameProperty() {
		return basePath;
	}
	
	@NotNull
	public ConcurrentString getPasswordProperty() {
		return localPath;
	}
	
	@NotNull
	public ConcurrentList<RequiredFile> getRequiredFiles() {
		return requiredFiles;
	}
	
	@NotNull
	public ConcurrentReference<UpdateServerStatus> getStatusProperty() {
		return status;
	}
	
	@NotNull
	public String getGameVersion() {
		return gameVersion.get();
	}
	
	@NotNull
	public String getName() {
		return name;
	}
	
	@NotNull
	public String getAddress() {
		return address.get();
	}
	
	public int getPort() {
		return port.getValue();
	}
	
	@NotNull
	public String getBasePath() {
		return basePath.get();
	}
	
	@NotNull
	public String getLocalPath() {
		return localPath.get();
	}
	
	
	@NotNull
	public UpdateServerStatus getStatus() {
		return status.get();
	}
	
	public void setAddress(@NotNull String address) {
		this.address.set(address);
	}
	
	public void setPort(int port) {
		this.port.set(port);
	}
	
	public void setBasePath(@NotNull String basePath) {
		this.basePath.set(basePath);
	}
	
	public void setLocalPath(@NotNull String localPath) {
		this.localPath.set(localPath);
	}
	
	public void setStatus(@NotNull UpdateServerStatus status) {
		this.status.set(status);
	}
	
	public void setGameVersion(@NotNull String gameVersion) {
		this.gameVersion.set(gameVersion);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static class RequiredFile {
		
		private final File localPath;
		private final URL remotePath;
		private final long length;
		private final long hash;
		
		public RequiredFile(@NotNull File localPath, @NotNull URL remotePath, long length, long hash) {
			this.localPath = localPath;
			this.remotePath = remotePath;
			this.length = length;
			this.hash = hash;
		}
		
		@NotNull
		public File getLocalPath() {
			return localPath;
		}
		
		@NotNull
		public URL getRemotePath() {
			return remotePath;
		}
		
		public long getLength() {
			return length;
		}
		
		public long getHash() {
			return hash;
		}
		
	}
	
	public enum UpdateServerStatus {
		UNKNOWN				("servers.status.unknown"),
		SCANNING			("servers.status.scanning"),
		REQUIRES_DOWNLOAD	("servers.status.requires_download"),
		DOWNLOADING			("servers.status.downloading"),
		READY				("servers.status.ready");
		
		private final String friendlyName;
		
		UpdateServerStatus(String friendlyName) {
			this.friendlyName = friendlyName;
		}
		
		public String getFriendlyName() {
			return friendlyName;
		}
	}
	
}
