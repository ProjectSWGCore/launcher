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

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.data.login.AuthenticationData
import com.projectswg.launcher.resources.data.login.LoginServer
import com.projectswg.launcher.resources.data.update.UpdateServer
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool
import me.joshlarson.jlcommon.control.Service
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.json.JSONInputStream
import me.joshlarson.json.JSONOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*

class PreferencesDataService : Service() {
	
	private val executor: ScheduledThreadPool = ScheduledThreadPool(1, 3, "data-executor-%d")
	
	init {
		loadPreferences()
	}
	
	override fun start(): Boolean {
		createDefaults()
		executor.start()
		executor.executeWithFixedDelay((5 * 60000).toLong(), (5 * 60000).toLong()) { savePreferences() }
		return true
	}
	
	override fun stop(): Boolean {
		savePreferences()
		executor.stop()
		return executor.awaitTermination(1000)
	}
	
	private fun createDefaults() {
		if (LauncherData.INSTANCE.general.wine == null || LauncherData.INSTANCE.general.wine!!.isEmpty())
			LauncherData.INSTANCE.general.wine = winePath
	}
	
	@Synchronized
	private fun loadPreferences() {
		val applicationDataDirectory = LauncherData.getApplicationDataDirectory()
		if (!applicationDataDirectory.exists())
			applicationDataDirectory.mkdirs()
		else if (!applicationDataDirectory.isDirectory) {
			applicationDataDirectory.deleteRecursively()
			applicationDataDirectory.mkdirs()
		}
		
		lateinit var lastSelectedServerName: String
		lateinit var localServerConfiguration: Map<String, Any?>
		
		try {
			JSONInputStream(FileInputStream(File(LauncherData.getApplicationDataDirectory(), "settings.json"))).use {
				val settings = it.readObject()
				loadSettingsGeneral(settings.getMap("general"))
				loadSettingsUpdate(settings.getMap("update"))
				loadSettingsLogin(settings.getMap("login"))
				loadSettingsForwarder(settings.getMap("forwarder"))
				
				lastSelectedServerName = settings.getMap("login").getString("last_selected_server", "")
				localServerConfiguration = settings.getMap("login").getMap("local_server")
			}
		} catch (fnf: FileNotFoundException) {
			loadSettingsGeneral(mapOf())
			loadSettingsLogin(mapOf())
			loadSettingsForwarder(mapOf())
			lastSelectedServerName = ""
			localServerConfiguration = mapOf()
		}
		
		loadServers()
		
		// Add the local server last so that it's always last in the menu
		loadLocalServer(localServerConfiguration)
		
		val lastSelectedServer = LauncherData.INSTANCE.login.getServerByName(lastSelectedServerName)
		if (lastSelectedServer != null)
			LauncherData.INSTANCE.login.activeServer = lastSelectedServer
	}
	
	private fun loadSettingsGeneral(settings: Map<String, Any?>) {
		LauncherData.INSTANCE.general.locale = Locale.forLanguageTag(settings.getString("locale", Locale.US.toLanguageTag()))
		LauncherData.INSTANCE.general.wine = settings.getString("wine", "")
		LauncherData.INSTANCE.general.isAdmin = settings.getBool("admin", false)
	}
	
	private fun loadSettingsUpdate(settings: Map<String, Any?>) {
		LauncherData.INSTANCE.update.localPath = settings.getString("local_path", "")
	}
	
	private fun loadSettingsLogin(settings: Map<String, Any?>) {
		for (e in settings.getMap("authentications")) {
			val data = AuthenticationData(e.key)
			data.username = castMap(e.value).getString("username", "")
			data.password = castMap(e.value).getString("password", "")
			LauncherData.INSTANCE.login.authenticationData.add(data)
		}
	}
	
	private fun loadLocalServer(localServerSettings: Map<String, Any?>) {
		val localAuthenticationSource = AuthenticationData(LOCAL_AUTHENTICATION_NAME)
		if (LauncherData.INSTANCE.login.getAuthenticationData(LOCAL_AUTHENTICATION_NAME) == null)
			LauncherData.INSTANCE.login.authenticationData.add(localAuthenticationSource)
		
		val defaultLocalServer = LoginServer("local")
		defaultLocalServer.connectionUri = "ws://127.0.0.1:44463/game"
		defaultLocalServer.updateServer = LauncherData.INSTANCE.update.serversProperty.getOrNull(0)
		defaultLocalServer.authentication = localAuthenticationSource
		
		val localServer = loadLoginServer(localServerSettings) ?: defaultLocalServer
		LauncherData.INSTANCE.login.localServer = localServer
		LauncherData.INSTANCE.login.servers.add(LauncherData.INSTANCE.login.localServer)
	}
	
	private fun loadSettingsForwarder(settings: Map<String, Any?>) {
		LauncherData.INSTANCE.forwarder.sendInterval = settings.getInt("send_interval", 1000)
		LauncherData.INSTANCE.forwarder.sendMax = settings.getInt("send_max", 400)
	}
	
	private fun loadServers() {
		try {
			loadRemoteServers()
			JSONInputStream(FileInputStream(File(LauncherData.getApplicationDataDirectory(), "servers.json"))).use {
				val serverConfigurations = it.readObject()
				loadUpdateServers(serverConfigurations.getListOfMap("update_servers"))
				loadLoginServers(serverConfigurations.getListOfMap("login_servers"))
			}
		} catch (fnf: FileNotFoundException) {
			loadSettingsGeneral(mapOf())
			loadSettingsLogin(mapOf())
			loadSettingsForwarder(mapOf())
		}
	}
	
	private fun loadRemoteServers() {
		try {
			Log.d("Retrieving server configuration from %s", REMOTE_SERVER_CONFIGURATION_URL)
			JSONInputStream(URL(REMOTE_SERVER_CONFIGURATION_URL).openStream()).use { input ->
				JSONOutputStream(FileOutputStream(File(LauncherData.getApplicationDataDirectory(), "servers.json"))).use { output ->
					output.writeObject(input.readObject())
				}
			}
		} catch (e: IOException) {
			Log.e("Failed to retrieve updated server configuration")
		}
	}
	
	private fun loadUpdateServers(serverConfigurations: List<Map<String, Any?>>) {
		for (updateServerConfiguration in serverConfigurations) {
			val updateServer = UpdateServer(updateServerConfiguration["name"] as? String ?: continue)
			updateServer.url = updateServerConfiguration["url"] as? String ?: continue
			updateServer.gameVersion = updateServerConfiguration["game_version"] as? String ?: continue
			updateServer.friendlyName = updateServerConfiguration["friendly_name"] as? String ?: continue
			LauncherData.INSTANCE.update.servers.add(updateServer)
		}
	}
	
	private fun loadLoginServers(serverConfigurations: List<Map<String, Any?>>) {
		val unusedAuthenticationServers = HashSet(LauncherData.INSTANCE.login.authenticationData)
		for (loginServerConfiguration in serverConfigurations) {
			val loginServer = loadLoginServer(loginServerConfiguration) ?: continue
			
			unusedAuthenticationServers.remove(loginServer.authentication)
			
			LauncherData.INSTANCE.login.servers.add(loginServer)
		}
		
		// Don't want to perpetually store credentials
		for (unusedAuthentication in unusedAuthenticationServers) {
			if (unusedAuthentication.name == LOCAL_AUTHENTICATION_NAME)
				continue // This will be used later
			LauncherData.INSTANCE.login.authenticationData.remove(unusedAuthentication)
		}
	}
	
	private fun loadLoginServer(loginServerConfiguration: Map<String, Any?>): LoginServer? {
		val loginServer = LoginServer(loginServerConfiguration["name"] as? String ?: return null)
		loginServer.connectionUri = loginServerConfiguration["url"] as? String ?: return null
		loginServer.updateServer = LauncherData.INSTANCE.update.getServer(loginServerConfiguration["update_server"] as? String ?: return null) ?: return null
		val authenticationSource = loginServerConfiguration["authentication_source"] as? String ?: return null
		var authentication = LauncherData.INSTANCE.login.getAuthenticationData(authenticationSource)
		if (authentication == null) {
			authentication = AuthenticationData(loginServerConfiguration["authentication_source"] as? String ?: return null)
			LauncherData.INSTANCE.login.authenticationData.add(authentication)
		}
		loginServer.authentication = authentication
		return loginServer
	}
	
	private fun castMap(obj: Any?): Map<String, Any?> {
		@Suppress("UNCHECKED_CAST")
		return obj as? Map<String, Any?> ?: mapOf()
	}
	
	private fun castListOfMap(obj: Any?): List<Map<String, Any?>> {
		@Suppress("UNCHECKED_CAST")
		return obj as? List<Map<String, Any?>> ?: listOf()
	}
	
	@Synchronized
	private fun savePreferences() {
		JSONOutputStream(FileOutputStream(File(LauncherData.getApplicationDataDirectory(), "settings.json"))).use {
			it.writeObject(jsonSettings())
		}
	}
	
	private fun jsonSettings(): Map<String, Any?> {
		return mapOf(
			"general" to jsonSettingsGeneral(),
			"update" to jsonSettingsUpdate(),
			"login" to jsonSettingsLogin(),
			"forwarder" to jsonSettingsForwarder()
		)
	}
	
	private fun jsonSettingsGeneral(): Map<String, Any?> {
		return mapOf(
			"locale" to LauncherData.INSTANCE.general.locale.toLanguageTag(),
			"wine" to LauncherData.INSTANCE.general.wine,
			"admin" to LauncherData.INSTANCE.general.isAdmin
		)
	}
	
	private fun jsonSettingsUpdate(): Map<String, Any?> {
		return mapOf(
			"local_path" to LauncherData.INSTANCE.update.localPath
		)
	}
	
	private fun jsonSettingsLogin(): Map<String, Any?> {
		val localServer = LauncherData.INSTANCE.login.localServer
		
		return mapOf(
			"last_selected_server" to LauncherData.INSTANCE.login.lastSelectedServer,
			"authentications" to jsonSettingsLoginAuthentication(),
			"local_server" to mapOf(
				"name" to localServer.name,
				"url" to localServer.connectionUri,
				"update_server" to (localServer.updateServer?.name ?: ""),
				"authentication_source" to localServer.authentication.name
			)
		)
	}
	
	private fun jsonSettingsLoginAuthentication(): Map<String, Any?> {
		val authentications = HashMap<String, Any?>()
		
		for (auth in LauncherData.INSTANCE.login.authenticationData) {
			authentications[auth.name] = mapOf(
				"username" to auth.username,
				"password" to auth.password
			)
		}
		
		return authentications
	}
	
	private fun jsonSettingsForwarder(): Map<String, Any?> {
		return mapOf(
			"send_interval" to LauncherData.INSTANCE.forwarder.sendInterval,
			"send_max" to LauncherData.INSTANCE.forwarder.sendMax
		)
	}
	
	private fun Map<String, Any?>?.getMap(key: String): Map<String, Any?> {
		return castMap(this?.getOrDefault(key, mapOf<String, Any?>()))
	}
	
	private fun Map<String, Any?>?.getListOfMap(key: String): List<Map<String, Any?>> {
		return castListOfMap(this?.getOrDefault(key, listOf<Map<String, Any?>>()))
	}
	
	private fun Map<String, Any?>?.getString(key: String, defaultValue: String): String {
		return (this?.getOrDefault(key, defaultValue) as? String?) ?: defaultValue
	}
	
	private fun Map<String, Any?>?.getInt(key: String, defaultValue: Int): Int {
		return ((this?.getOrDefault(key, defaultValue) as? Number?) ?: defaultValue).toInt()
	}
	
	private fun Map<String, Any?>?.getBool(key: String, defaultValue: Boolean): Boolean {
		return (this?.getOrDefault(key, defaultValue) as? Boolean?) ?: defaultValue
	}
	
	companion object {
		
		private const val LOCAL_AUTHENTICATION_NAME = "local"
		private const val REMOTE_SERVER_CONFIGURATION_URL = "https://projectswg.com/launcher/servers.json"
		
		private val winePath: String?
			get() {
				val pathStr = System.getenv("PATH") ?: return null
				for (path in pathStr.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
					Log.t("Testing wine binary at %s", path)
					var test = File(path, "wine")
					if (test.isFile) {
						try {
							test = test.canonicalFile
							Log.d("Found wine installation. Location: %s", test)
							return test.absolutePath
						} catch (e: IOException) {
							Log.w("Failed to get canonical file location of possible wine location: %s", test)
						}
					}
				}
				return null
			}
	}
}