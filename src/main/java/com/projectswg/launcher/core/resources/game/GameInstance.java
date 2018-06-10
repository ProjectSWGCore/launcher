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

package com.projectswg.launcher.core.resources.game;

import com.projectswg.forwarder.Forwarder;
import com.projectswg.forwarder.Forwarder.ForwarderData;
import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import me.joshlarson.jlcommon.concurrency.BasicThread;
import me.joshlarson.jlcommon.concurrency.Delay;
import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GameInstance {
	
	private static final AtomicLong GAME_ID = new AtomicLong(0);
	
	private final BasicThread processThread;
	private final BasicThread forwarderThread;
	private final LoginServer server;
	private Forwarder forwarder;
	
	public GameInstance(LoginServer server) {
		this.server = server;
		long gameId = GAME_ID.incrementAndGet();
		this.processThread = new BasicThread("game-process-"+gameId, this::runProcess);
		this.forwarderThread = new BasicThread("game-forwarder-"+gameId, this::runForwarder);
		this.forwarder = new Forwarder();
	}
	
	public void start() {
		if (forwarder == null)
			return;
		ForwarderData data = forwarder.getData();
		data.setAddress(new InetSocketAddress(server.getAddress(), server.getPort()));
		data.setUsername(server.getUsername());
		data.setPassword(server.getPassword());
		forwarderThread.start();
	}
	
	public void stop() {
		if (processThread.isExecuting()) {
			processThread.stop(true);
			processThread.awaitTermination(2000);
		}
	}
	
	private void runForwarder() {
		processThread.start();
		forwarder.run();
		forwarder = null;
	}
	
	private void runProcess() {
		try {
			Process process = buildProcess(server, forwarder.getData());
			if (process == null)
				return;
			int ret;
			try {
				File crashLog = forwarder.readClientOutput(process.getInputStream());
				if (crashLog != null)
					onCrash(crashLog);
				ret = process.waitFor();
			} catch (InterruptedException e) {
				Log.w("Thread %s interrupted", Thread.currentThread().getName());
				try {
					process.destroyForcibly().waitFor(1, TimeUnit.SECONDS);
					if (process.isAlive())
						ret = Integer.MIN_VALUE;
					else
						ret = process.exitValue();
				} catch (InterruptedException i) {
					// Suppressed - no need to report interruption twice
					ret = Integer.MIN_VALUE;
				}
			}
			if (ret == Integer.MIN_VALUE)
				Log.w("Failed to retrieve proper exit code - defaulting to MIN_VALUE");
			Log.i("Game thread %s terminated with exit code (%d)", Thread.currentThread().getName(), ret);
		} finally {
			forwarderThread.stop(true);
			forwarderThread.awaitTermination(500);
		}
	}
	
	private void onCrash(File crashLog) {
		Log.w("Crash Detected. ZIP: %s", crashLog);
		reportWarning("Crash Detected", "A crash was detected. Please report this to the ProjectSWG team with this zip file: " + crashLog);
	}
	
	@Nullable
	private static Process buildProcess(@NotNull LoginServer server, @NotNull final ForwarderData data) {
		Log.t("Waiting for forwarder to initialize...");
		long start = System.nanoTime();
		while (data.getLoginPort() == 0 && System.nanoTime() - start <= 1E9) {
			Delay.sleepMilli(10);
		}
		int loginPort = data.getLoginPort();
		if (loginPort == 0) {
			Log.e("Failed to build process. Forwarder did not initialize.");
			reportError("Connection", "Failed to initialize the PSWG forwarder");
			return null;
		}
		String username = data.getUsername();
		if (username == null) {
			Log.w("Issue when launching game. Username is null - setting to an empty string");
			username = "";
		}
		File swgDirectory = null;
		UpdateServer updateServer = server.getUpdateServer();
		if (updateServer != null) {
			swgDirectory = new File(updateServer.getLocalPath());
			if (!swgDirectory.isDirectory()) {
				Log.e("Failed to launch game. Invalid SWG directory: %s", swgDirectory);
				reportError("Process", "Invalid SWG directory: " + swgDirectory);
				return null;
			}
		}
		if (swgDirectory == null) {
			Log.e("Failed to launch game. No SWG directory defined");
			reportError("Process", "No SWG directory defined");
			return null;
		}
		Log.d("Building game... (login=%d)", loginPort);
		return ProcessExecutor.INSTANCE.buildProcess(updateServer, "SwgClient_r.exe", 
				"--",
				"-s",
				"Station",
				"subscriptionFeatures=1",
				"gameFeatures=34374193",
				"-s",
				"ClientGame",
				"loginServerPort0=" + loginPort,
				"loginServerAddress0=127.0.0.1",
				"loginClientID=" + username,
				"autoConnectToLoginServer=" + !username.isEmpty(),
				"logReportFatals=true",
				"logStderr=true",
				"0fd345d9=true");
	}
	
	private static void reportWarning(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Game Launch Warning");
			alert.setHeaderText(title);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
	
	private static void reportError(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Game Launch Error");
			alert.setHeaderText(title);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
	
}
