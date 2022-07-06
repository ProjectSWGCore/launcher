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

package com.projectswg.launcher.services.data

import com.projectswg.common.data.encodables.galaxy.Galaxy
import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.intents.DownloadLauncherIntent
import com.projectswg.launcher.resources.intents.RequestScanIntent
import com.projectswg.launcher.resources.pipeline.LauncherConfigurationUpdater
import com.projectswg.launcher.resources.pipeline.UpdateServerUpdater
import javafx.application.Platform
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool
import me.joshlarson.jlcommon.control.IntentHandler
import me.joshlarson.jlcommon.control.Service
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.json.JSONException
import me.joshlarson.json.JSONInputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

class RemoteDataService : Service() {
	
	private val executor: ScheduledThreadPool = ScheduledThreadPool(3, "remote-data-service")
	
	override fun start(): Boolean {
		executor.start()
		// Retrieves the latest file list for each update server
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30)) { this.updateUpdateServers() }
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30)) { this.updateRemoteVersion() }
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(1)) { this.updateLoginStatus() }
		return true
	}
	
	override fun stop(): Boolean {
		executor.stop()
		return executor.awaitTermination(1000)
	}
	
	@IntentHandler
	private fun handleRequestScanIntent(rsi: RequestScanIntent) {
		UpdateServerUpdater.update(rsi.server)
	}
	
	@IntentHandler
	private fun handleDownloadLauncherIntent(dli: DownloadLauncherIntent) {
		LauncherConfigurationUpdater.download()
	}
	
	private fun updateUpdateServers() {
		updateData.servers.parallelStream().forEach { UpdateServerUpdater.update(it) }
	}
	
	private fun updateRemoteVersion() {
		LauncherConfigurationUpdater.update()
	}
	
	private fun updateLoginStatus() {
		for (server in loginData.servers) {
			val serverUri = URI(server.connectionUri)
			val protocol = if (serverUri.scheme == "ws") "http" else "https"
			val port = if (serverUri.port < 0) 443 else serverUri.port
			val statsUrl = URL(protocol, serverUri.host ?: continue, port, "/stats")
			Log.t("Requesting server stats from %s", statsUrl.toExternalForm())
			
			lateinit var loginName: String
			lateinit var loginStatus: String
			try {
				JSONInputStream(statsUrl.openStream()).use {
					val serverInfo = it.readObject()
					val galaxyName = serverInfo["name"] as? String ?: return@use
					val status = serverInfo["status"] as? String ?: return@use
					
					loginName = galaxyName
					loginStatus = status
				}
			} catch (e: JSONException) {
				Platform.runLater {
					loginName = ""
					loginStatus = "INVALID"
				}
			} catch (e: IOException) {
				loginName = ""
				loginStatus = Galaxy.GalaxyStatus.DOWN.name
			}
			
			Platform.runLater {
				server.instanceInfo.loginName = loginName
				server.instanceInfo.loginStatus = loginStatus
			}
		}
	}
	
	companion object {
		
		private val updateData = LauncherData.INSTANCE.update
		private val loginData = LauncherData.INSTANCE.login
	}
	
}
