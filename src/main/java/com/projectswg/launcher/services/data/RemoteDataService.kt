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

import com.projectswg.launcher.resources.intents.DownloadLauncherIntent
import com.projectswg.launcher.resources.intents.RequestScanIntent
import com.projectswg.launcher.resources.pipeline.LauncherConfigurationUpdater
import com.projectswg.launcher.resources.pipeline.UpdateServerUpdater
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool
import me.joshlarson.jlcommon.control.IntentHandler
import me.joshlarson.jlcommon.control.Service
import java.util.concurrent.TimeUnit

class RemoteDataService : Service() {
	
	private val executor: ScheduledThreadPool = ScheduledThreadPool(2, "remote-data-service")
	
	override fun start(): Boolean {
		executor.start()
		// Retrieves the latest file list for each update server
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30)) { this.updateUpdateServers() }
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30)) { this.updateRemoteVersion() }
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
	
	companion object {
		
		private val updateData: com.projectswg.launcher.resources.data.update.UpdateData
			get() = com.projectswg.launcher.resources.data.LauncherData.INSTANCE.update
	}
	
}
