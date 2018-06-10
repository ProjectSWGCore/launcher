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

package com.projectswg.launcher.core.resources.gui.servers;

import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.RequiredFile;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus;
import javafx.application.Platform;
import javafx.scene.control.Label;
import me.joshlarson.jlcommon.concurrency.beans.ConcurrentDouble;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class ServerPlayLabel extends Label {
	
	private static final String[] SIZE_SUFFIX = new String[] { "B", "kB", "MB", "GB" };
	
	private final ResourceBundle bundle;
	private final AtomicReference<UpdateServer> server;
	private final ConcurrentDouble progressBar;
	
	public ServerPlayLabel(@NotNull ResourceBundle bundle, @NotNull ConcurrentDouble progressBar) {
		Objects.requireNonNull(bundle, "bundle");
		this.bundle = bundle;
		this.server = new AtomicReference<>(null);
		this.progressBar = progressBar;
	}
	
	public void setUpdateServer(UpdateServer server) {
		UpdateServer prev = this.server.getAndSet(server);
		teardown(prev);
		setup(server);
	}
	
	private void setup(UpdateServer server) {
		if (server == null)
			return;
		server.getStatusProperty().addListener(this, status -> update(server, status));
	}
	
	private void teardown(UpdateServer server) {
		if (server == null)
			return;
		server.getStatusProperty().removeListener(this);
	}
	
	private void update(UpdateServer server, UpdateServerStatus status) {
		progressBar.removeListener("server-play-label");
		switch (status) {
			case UNKNOWN:
			case READY:
			case SCANNING:
				progressBar.setValue(-1);
				internalSetText(bundle.getString("servers.action_info.empty"));
				break;
			case REQUIRES_DOWNLOAD:
				progressBar.setValue(-1);
				internalSetText(calculateDownloadSize(server.getRequiredFiles()) + " " + bundle.getString("servers.action_info.required"));
				break;
			case DOWNLOADING:
				progressBar.addListener("server-play-label", p -> internalSetText(String.format("%.2f%% %s", p*100, bundle.getString("servers.action_info.progress"))));
				if (progressBar.get() == -1)
					internalSetText(bundle.getString("servers.action_info.downloading"));
				else
					internalSetText(String.format("%.2f%% %s", progressBar.get(), bundle.getString("servers.action_info.progress")));
				break;
		}
	}
	
	private void internalSetText(String text) {
		Platform.runLater(() -> setText(text));
	}
	
	private static String calculateDownloadSize(Collection<RequiredFile> files) {
		double totalSize = files.stream().mapToLong(RequiredFile::getLength).sum();
		for (int i = 0; i < SIZE_SUFFIX.length; i++) {
			if (i != 0)
				totalSize /= 1024;
			if (totalSize < 1024)
				return String.format("%.2f%s", totalSize, SIZE_SUFFIX[i]);
		}
		return String.format("%.2f%s", totalSize, SIZE_SUFFIX[SIZE_SUFFIX.length - 1]);
	}
	
}
