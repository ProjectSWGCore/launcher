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

package com.projectswg.launcher.core.services.data

import com.projectswg.holocore.client.HolocoreSocket
import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.login.LoginData
import com.projectswg.launcher.core.resources.data.login.LoginServer
import com.projectswg.launcher.core.resources.data.update.UpdateData
import com.projectswg.launcher.core.resources.intents.RequestScanIntent
import com.projectswg.launcher.core.resources.pipeline.UpdateServerUpdater
import javafx.beans.InvalidationListener
import me.joshlarson.jlcommon.collections.TransferSet
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool
import me.joshlarson.jlcommon.control.IntentHandler
import me.joshlarson.jlcommon.control.Service
import me.joshlarson.jlcommon.javafx.beans.ConcurrentCollection
import me.joshlarson.jlcommon.log.Log
import tornadofx.FX
import tornadofx.get
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class RemoteDataService : Service() {
	
	private val loginServers: TransferSet<LoginServer, LoginServerUpdater>
	private val executor: ScheduledThreadPool
	
	init {
		this.loginServers = TransferSet( { it.name }, { LoginServerUpdater(it) })
		this.executor = ScheduledThreadPool(2, "remote-data-service")
		
		loginServers.addDestroyCallback { it.terminate() }
		loginData.servers.addCollectionChangedListener(LISTENER_KEY, ConcurrentCollection.ComplexCollectionChangedListener<ConcurrentCollection<Set<LoginServer>, LoginServer>> { loginServers.synchronize(it) })
	}
	
	override fun initialize(): Boolean {
		loginServers.synchronize(loginData.servers)
		return true
	}
	
	override fun start(): Boolean {
		executor.start()
		// Updates the status of the login server (OFFLINE/LOADING/UP/LOCKED)
		executor.executeWithFixedDelay(0, TimeUnit.SECONDS.toMillis(10)) { this.updateLoginServers() }
		// Retrieves the latest file list for each update server
		executor.executeWithFixedDelay(0, TimeUnit.MINUTES.toMillis(30)) { this.updateUpdateServers() }
		return true
	}
	
	override fun stop(): Boolean {
		executor.stop()
		return executor.awaitTermination(1000)
	}
	
	override fun terminate(): Boolean {
		loginServers.synchronize(emptyList())
		return true
	}
	
	@IntentHandler
	private fun handleRequestScanIntent(rsi: RequestScanIntent) {
		UpdateServerUpdater.update(rsi.server)
	}
	
	private fun updateLoginServers() {
		// Allows for parallel networking operations
		loginServers.parallelStream().forEach { it.update() }
	}
	
	private fun updateUpdateServers() {
		updateData.servers.parallelStream().forEach { UpdateServerUpdater.update(it) }
	}
	
	private class LoginServerUpdater(private val server: LoginServer) {
		private var socket: HolocoreSocket? = null
		
		init {
			this.socket = null
			
			server.addressProperty.addListener(InvalidationListener { updateSocket() })
			server.portProperty.addListener(InvalidationListener { updateSocket() })
			updateSocket()
		}
		
		fun terminate() {
			socket?.close()
		}
		
		fun update() {
			// Better luck next time
			val socket = this.socket
			if (socket == null) {
				server.instanceInfo.loginStatus = ""
				return
			}
			if (server.instanceInfo.loginStatus.isNullOrBlank())
				server.instanceInfo.loginStatus = FX.messages["servers.loginStatus.checking"]
			
			for (i in 0..4) {
				val status = socket.getServerStatus(1000)
				if (status != "OFFLINE") {
					server.instanceInfo.loginStatus = status
					return
				}
			}
			server.instanceInfo.loginStatus = "OFFLINE"
		}
		
		private fun updateSocket() {
			try {
				val addr = server.address.trim { it <= ' ' }
				val port = server.port
				if (addr.isEmpty() || port <= 0)
					return
				this.socket = HolocoreSocket(InetAddress.getByName(addr), port)
			} catch (e: UnknownHostException) {
				this.socket = null
				Log.w(e)
			}
			
		}
		
	}
	
	companion object {
		
		private const val LISTENER_KEY = "RDS"
		
		private val loginData: LoginData
			get() = LauncherData.INSTANCE.login
		
		private val updateData: UpdateData
			get() = LauncherData.INSTANCE.update
	}
	
}
