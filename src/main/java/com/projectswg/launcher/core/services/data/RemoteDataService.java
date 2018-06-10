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

import com.projectswg.connection.HolocoreSocket;
import com.projectswg.launcher.core.resources.data.LauncherData;
import com.projectswg.launcher.core.resources.data.login.LoginData;
import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateData;
import com.projectswg.launcher.core.resources.intents.RequestScanIntent;
import com.projectswg.launcher.core.resources.pipeline.UpdateServerUpdater;
import me.joshlarson.jlcommon.collections.TransferSet;
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool;
import me.joshlarson.jlcommon.control.IntentHandler;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.log.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RemoteDataService extends Service {
	
	private static final String LISTENER_KEY = "RDS";
	
	private final TransferSet<LoginServer, LoginServerUpdater> loginServers;
	private final ScheduledThreadPool executor;
	
	public RemoteDataService() {
		this.loginServers = new TransferSet<>(LoginServer::getName, LoginServerUpdater::new);
		this.executor = new ScheduledThreadPool(2, "remote-data-service");
		
		loginServers.addDestroyCallback(LoginServerUpdater::terminate);
		getLoginData().getServers().addCollectionChangedListener(LISTENER_KEY, loginServers::synchronize);
	}
	
	@Override
	public boolean start() {
		executor.start();
		// Updates the status of the login server (OFFLINE/LOADING/UP/LOCKED)
		executor.executeWithFixedDelay(0, TimeUnit.SECONDS.toMillis(10), this::updateLoginServers);
		// Retrieves the latest file list for each update server
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30), this::updateUpdateServers);
		return true;
	}
	
	@Override
	public boolean stop() {
		executor.stop();
		executor.awaitTermination(1000);
		return true;
	}
	
	@Override
	public boolean terminate() {
		loginServers.synchronize(Collections.emptyList());
		return true;
	}
	
	@IntentHandler
	private void handleRequestScanIntent(RequestScanIntent rsi) {
		UpdateServerUpdater.update(rsi.getServer());
	}
	
	private void updateLoginServers() {
		// Allows for parallel networking operations
		loginServers.parallelStream().forEach(LoginServerUpdater::update);
	}
	
	private void updateUpdateServers() {
		getUpdateData().getServers().parallelStream().forEach(UpdateServerUpdater::update);
	}
	
	private static LoginData getLoginData() {
		return LauncherData.getInstance().getLogin();
	}
	
	private static UpdateData getUpdateData() {
		return LauncherData.getInstance().getUpdate();
	}
	
	private static class LoginServerUpdater {
		
		private final LoginServer server;
		private HolocoreSocket socket;
		
		public LoginServerUpdater(LoginServer server) {
			this.server = server;
			this.socket = null;
			
			server.getAddressProperty().addListener(LISTENER_KEY, addr -> updateSocket());
			server.getPortProperty().addListener(LISTENER_KEY, port -> updateSocket());
			updateSocket();
		}
		
		public void terminate() {
			HolocoreSocket socket = this.socket;
			if (socket != null)
				socket.terminate();
		}
		
		public void update() {
			HolocoreSocket socket = this.socket;
			if (socket == null)
				return; // Better luck next time
			server.getInstanceInfo().setLoginStatus(socket.getServerStatus(5000));
		}
		
		private void updateSocket() {
			try {
				String addr = server.getAddress().trim();
				int port = server.getPort();
				if (addr.isEmpty() || port <= 0)
					return;
				this.socket = new HolocoreSocket(InetAddress.getByName(addr), port);
			} catch (UnknownHostException e) {
				this.socket = null;
				Log.w(e);
			}
		}
		
	}
	
}
