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

package com.projectswg.launcher.core.resources.intents;

import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import me.joshlarson.jlcommon.control.Intent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Queues a download for all required files on the specified update server, with an optional callback for the current download progress
 */
public class DownloadPatchIntent extends Intent {
	
	private final UpdateServer server;
	private final Consumer<Double> callback;
	
	public DownloadPatchIntent(@NotNull UpdateServer server, @Nullable Consumer<Double> callback) {
		Objects.requireNonNull(server, "server");
		this.server = server;
		this.callback = callback;
	}
	
	@NotNull
	public UpdateServer getServer() {
		return server;
	}
	
	@Nullable
	public Consumer<Double> getCallback() {
		return callback;
	}
	
	public static void broadcast(@NotNull UpdateServer server) {
		new DownloadPatchIntent(server, null).broadcast();
	}
	
	public static void broadcastWithCallback(@NotNull UpdateServer server, @NotNull Consumer<Double> callback) {
		Objects.requireNonNull(callback, "callback");
		new DownloadPatchIntent(server, callback).broadcast();
	}
}
