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

import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus;
import com.projectswg.launcher.core.resources.intents.CancelDownloadIntent;
import com.projectswg.launcher.core.resources.intents.DownloadPatchIntent;
import com.projectswg.launcher.core.resources.intents.LaunchGameIntent;
import javafx.application.Platform;
import javafx.scene.control.Button;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentDouble;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles the updating of the server play button between each state of the update server
 */
public class ServerPlayButton extends Button {
	
	private final ResourceBundle bundle;
	private final AtomicReference<LoginServer> loginServer;
	private final AtomicReference<UpdateServer> updateServer;
	private final ConcurrentDouble progressBar;
	
	public ServerPlayButton(@NotNull ResourceBundle bundle, @NotNull ConcurrentDouble progressBar) {
		Objects.requireNonNull(bundle, "bundle");
		this.bundle = bundle;
		this.loginServer = new AtomicReference<>(null);
		this.updateServer = new AtomicReference<>(null);
		this.progressBar = progressBar;
		
		setOnAction(e -> act());
	}
	
	public void setLoginServer(LoginServer server) {
		this.loginServer.set(server);
	}
	
	public void setUpdateServer(UpdateServer server) {
		UpdateServer prev = this.updateServer.getAndSet(server);
		teardown(prev);
		setup(server);
	}
	
	private void setup(UpdateServer server) {
		if (server == null)
			return;
		server.getStatusProperty().addSimpleListener(this, this::update);
		update(server.getStatus());
	}
	
	private void teardown(UpdateServer server) {
		if (server == null)
			return;
		server.getStatusProperty().removeListener(this);
		setDisable(true);
	}
	
	private void update(UpdateServerStatus status) {
		setDisable(status == UpdateServerStatus.SCANNING);
		switch (status) {
			case SCANNING:
			case UNKNOWN:
			case READY:
				internalSetText("servers.play.play");
				break;
			case REQUIRES_DOWNLOAD:
				internalSetText("servers.play.update");
				break;
			case DOWNLOADING:
				internalSetText("servers.play.cancel");
				break;
		}
	}
	
	private void act() {
		LoginServer loginServer = this.loginServer.get();
		UpdateServer updateServer = this.updateServer.get();
		
		if (loginServer == null || updateServer == null)
			return;
		
		switch (updateServer.getStatus()) {
			case SCANNING:
				break;
			case UNKNOWN:
			case READY:
				LaunchGameIntent.broadcast(loginServer);
				break;
			case REQUIRES_DOWNLOAD:
				DownloadPatchIntent.broadcastWithCallback(updateServer, progressBar::set);
				break;
			case DOWNLOADING:
				CancelDownloadIntent.broadcast(updateServer);
				break;
		}
	}
	
	private void internalSetText(String key) {
		Platform.runLater(() -> setText(bundle.getString(key)));
	}
	
}
