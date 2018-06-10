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

/**
 * Requests a scan of the specified update server's local files, to determine whether or not the files are up to date
 */
public class RequestScanIntent extends Intent {
	
	private final UpdateServer server;
	
	public RequestScanIntent(@NotNull UpdateServer server) {
		this.server = server;
	}
	
	@NotNull
	public UpdateServer getServer() {
		return server;
	}
	
	public static void broadcast(@NotNull UpdateServer server) {
		new RequestScanIntent(server).broadcast();
	}
	
}
