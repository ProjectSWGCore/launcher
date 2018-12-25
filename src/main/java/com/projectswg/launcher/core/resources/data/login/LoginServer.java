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

import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus;
import me.joshlarson.jlcommon.javafx.beans.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoginServer {
	
	private final String name;
	private final ConcurrentString address;
	private final ConcurrentInteger port;
	private final ConcurrentString username;
	private final ConcurrentString password;
	private final ConcurrentReference<UpdateServer> updateServer;
	private final ConcurrentBoolean verifyServer;
	private final LoginServerInstanceInfo instanceInfo;
	
	public LoginServer(@NotNull String name) {
		this.name = name;
		this.address = new ConcurrentString("");
		this.port = new ConcurrentInteger(0);
		this.username = new ConcurrentString("");
		this.password = new ConcurrentString("");
		this.updateServer = new ConcurrentReference<>(null);
		this.verifyServer = new ConcurrentBoolean(true);
		this.instanceInfo = new LoginServerInstanceInfo();
		
		updateServer.addSimpleListener("login-server-"+name, this::updateServerListener);
		instanceInfo.setUpdateStatus(UpdateServerStatus.UNKNOWN.getFriendlyName());
		updateServerListener(updateServer, null, null);
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
		return username;
	}
	
	@NotNull
	public ConcurrentString getPasswordProperty() {
		return password;
	}
	
	@NotNull
	public ConcurrentBoolean getVerifyServerProperty() {
		return verifyServer;
	}
	
	@NotNull
	public ConcurrentReference<UpdateServer> getUpdateServerProperty() {
		return updateServer;
	}
	
	@NotNull
	public LoginServerInstanceInfo getInstanceInfo() {
		return instanceInfo;
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
	public String getUsername() {
		return username.get();
	}
	
	@NotNull
	public String getPassword() {
		return password.get();
	}
	
	@Nullable
	public UpdateServer getUpdateServer() {
		return updateServer.get();
	}
	
	public boolean isVerifyServer() {
		return verifyServer.get();
	}
	
	public void setAddress(@NotNull String address) {
		this.address.set(address);
	}
	
	public void setPort(int port) {
		this.port.set(port);
	}
	
	public void setUsername(@NotNull String username) {
		this.username.set(username);
	}
	
	public void setPassword(@NotNull String password) {
		this.password.set(password);
	}
	
	public void setUpdateServer(@Nullable UpdateServer server) {
		this.updateServer.set(server);
	}
	
	public void setVerifyServer(boolean verifyServer) {
		this.verifyServer.set(verifyServer);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	private void updateServerListener(ConcurrentBase<UpdateServer> obs, UpdateServer prev, UpdateServer next) {
		String listenerName = "login-server-"+name;
		if (prev != null) {
			prev.getStatusProperty().removeListener(listenerName);
		}
		if (next != null) {
			next.getStatusProperty().addSimpleListener(listenerName, this::onUpdateServerStatusUpdated);
			instanceInfo.setReadyToPlay(calculateReadyToPlay(next.getStatus()));
		} else {
			instanceInfo.setReadyToPlay(false);
		}
	}
	
	private void onUpdateServerStatusUpdated(UpdateServerStatus status) {
		instanceInfo.setReadyToPlay(calculateReadyToPlay(status));
		instanceInfo.setUpdateStatus(status.getFriendlyName());
	}
	
	private boolean calculateReadyToPlay(UpdateServerStatus status) {
		switch (status) {
			case UNKNOWN:
			case READY:
				return true;
			default:
				return false;
		}
	}
	
}
