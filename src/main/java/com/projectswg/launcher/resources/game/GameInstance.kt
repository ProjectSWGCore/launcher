/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
 * *
 * This file is part of the ProjectSWG Launcher.                                   *
 * *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 * *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 * *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https:></https:>//www.gnu.org/licenses/>.          *
 * *
 */

package com.projectswg.launcher.resources.game

import com.projectswg.forwarder.Forwarder
import com.projectswg.forwarder.Forwarder.ForwarderData
import com.projectswg.launcher.resources.data.login.LoginServer
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import me.joshlarson.jlcommon.concurrency.BasicThread
import me.joshlarson.jlcommon.concurrency.Delay
import me.joshlarson.jlcommon.log.Log
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class GameInstance(private val server: LoginServer) {
	
	val forwarder = Forwarder()
	
	private val processThread: BasicThread
	private val forwarderThread: BasicThread
	
	init {
		val gameId = GAME_ID.incrementAndGet()
		this.processThread = BasicThread("game-process-$gameId") { this.runProcess() }
		this.forwarderThread = BasicThread("game-forwarder-$gameId") { this.runForwarder() }
	}
	
	fun start() {
		val data = forwarder.data
		data.baseConnectionUri = server.connectionUri
		data.outboundTunerInterval = com.projectswg.launcher.resources.data.LauncherData.INSTANCE.forwarder.sendInterval
		data.outboundTunerMaxSend = com.projectswg.launcher.resources.data.LauncherData.INSTANCE.forwarder.sendMax
		forwarderThread.start()
	}
	
	fun stop() {
		if (processThread.isExecuting) {
			processThread.stop(true)
			processThread.awaitTermination(2000)
		}
	}
	
	private fun runForwarder() {
		processThread.start()
		forwarder.run()
	}
	
	private fun runProcess() {
		try {
			val process = buildProcess(server, forwarder.data) ?: return
			try {
				val crashLog = forwarder.readClientOutput(process.inputStream)
				if (crashLog != null)
					onCrash(crashLog)
				forwarder.data.crashed = true
			} catch (e: InterruptedException) {
				Log.w("Thread %s interrupted", Thread.currentThread().name)
			}
			try {
				process.destroyForcibly().waitFor(1, TimeUnit.SECONDS)
				if (process.isAlive) {
					Log.w("Failed to retrieve proper exit code (still alive) for ${Thread.currentThread().name}")
				} else {
					Log.i("Game thread ${Thread.currentThread().name} terminated with exit code (${process.exitValue()})")
				}
			} catch (i: InterruptedException) {
				Log.w("Failed to retrieve proper exit code (interrupted) for ${Thread.currentThread().name}")
			}
		} finally {
			forwarderThread.stop(true)
			forwarderThread.awaitTermination(500)
		}
	}
	
	private fun onCrash(crashLog: File) {
		Log.w("Crash Detected. ZIP: %s", crashLog)
		reportCrash("A crash was detected. Please report this to the ProjectSWG team with this zip file: $crashLog")
	}
	
	companion object {
		
		private val GAME_ID = AtomicLong(0)
		
		private fun buildProcess(server: LoginServer, data: ForwarderData): Process? {
			Log.t("Waiting for forwarder to initialize...")
			val start = System.nanoTime()
			while (data.loginPort == 0 && System.nanoTime() - start <= 1E9) {
				Delay.sleepMilli(10)
			}
			val loginPort = data.loginPort
			if (loginPort == 0) {
				Log.e("Failed to build process. Forwarder did not initialize.")
				reportError("Connection", "Failed to initialize the PSWG forwarder")
				return null
			}
			var username: String? = data.username
			if (username == null) {
				Log.w("Issue when launching game. Username is null - setting to an empty string")
				username = ""
			}
			val updateServer = server.updateServer
			if (updateServer == null) {
				Log.e("Failed to launch game. No update server defined")
				reportError("Process", "No update server defined")
				return null
			}
			val swgDirectory = updateServer.localPath
			if (!swgDirectory.isDirectory) {
				Log.e("Failed to launch game. Invalid SWG directory: %s", swgDirectory)
				reportError("Process", "Invalid SWG directory: $swgDirectory")
				return null
			}
			Log.d("Building game... (login=%d)", loginPort)
			return ProcessExecutor.INSTANCE.buildProcess(updateServer, "SwgClient_r.exe",
					"--",
					"-s",
					"Station",
					"subscriptionFeatures=1",
					"gameFeatures=34374193",
					"-s",
					"ClientGame",
					"loginServerPort0=$loginPort",
					"loginServerAddress0=127.0.0.1",
					"loginClientID=$username",
					"autoConnectToLoginServer=" + username.isNotEmpty(),
					"logReportFatals=true",
					"logStderr=true",
					"0fd345d9=" + if (com.projectswg.launcher.resources.data.LauncherData.INSTANCE.general.isAdmin) "true" else "false",
					"-s",
					"SharedNetwork",
					"useTcp=false",
					"networkHandlerDispatchThrottle=false",
					"maxRawPacketSize=16384",
					"fragmentSize=16384",
					"maxInstandingPackets=4096",
					"useNetworkThread=false",
					"processOnSend=true",
					"networkHandlerDispatchQueueSize=2000")
		}
		
		private fun reportCrash(message: String) {
			Platform.runLater {
				val alert = Alert(AlertType.WARNING)
				alert.title = "Game Launch Warning"
				alert.headerText = "Crash Detected"
				alert.contentText = message
				alert.showAndWait()
			}
		}
		
		private fun reportError(title: String, message: String) {
			Platform.runLater {
				val alert = Alert(AlertType.ERROR)
				alert.title = "Game Launch Error"
				alert.headerText = title
				alert.contentText = message
				alert.showAndWait()
			}
		}
	}
	
}
