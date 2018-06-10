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

package com.projectswg.launcher.core.resources.data.login;

import me.joshlarson.jlcommon.concurrency.beans.ConcurrentBoolean;
import me.joshlarson.jlcommon.concurrency.beans.ConcurrentString;
import org.jetbrains.annotations.NotNull;

public class LoginServerInstanceInfo {
	
	private final ConcurrentString loginStatus;
	private final ConcurrentString updateStatus;
	private final ConcurrentBoolean readyToPlay;
	
	public LoginServerInstanceInfo() {
		this.loginStatus = new ConcurrentString("");
		this.updateStatus = new ConcurrentString("");
		this.readyToPlay = new ConcurrentBoolean();
	}
	
	@NotNull
	public ConcurrentString getLoginStatusProperty() {
		return loginStatus;
	}
	
	@NotNull
	public ConcurrentString getUpdateStatusProperty() {
		return updateStatus;
	}
	
	@NotNull
	public ConcurrentBoolean getReadyToPlayProperty() {
		return readyToPlay;
	}
	
	public String getLoginStatus() {
		return loginStatus.get();
	}
	
	public String getUpdateStatus() {
		return updateStatus.get();
	}
	
	public boolean isReadyToPlay() {
		return readyToPlay.get();
	}
	
	public void setLoginStatus(@NotNull String loginStatus) {
		this.loginStatus.set(loginStatus);
	}
	
	public void setUpdateStatus(@NotNull String updateStatus) {
		this.updateStatus.set(updateStatus);
	}
	
	public void setReadyToPlay(boolean readyToPlay) {
		this.readyToPlay.set(readyToPlay);
	}
	
}
