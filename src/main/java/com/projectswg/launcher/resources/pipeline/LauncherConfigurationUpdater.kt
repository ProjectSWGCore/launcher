package com.projectswg.launcher.resources.pipeline

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.gui.events.LauncherNewVersionEvent
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.json.JSONInputStream
import tornadofx.FX
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicReference

object LauncherConfigurationUpdater {
	
	private const val REMOTE_CONFIGURATION_URL = "https://projectswg.com/launcher/launcher.json"
	
	private val downloadUrl = AtomicReference<String>(null)
	
	fun update() {
		try {
			Log.d("Retrieving server configuration from %s", REMOTE_CONFIGURATION_URL)
			JSONInputStream(URL(REMOTE_CONFIGURATION_URL).openStream()).use { input ->
				val configuration = input.readObject()
				Log.t("Remote configuration: %s", configuration)
				val version = configuration["download_version"] as? String ?: return
				@Suppress("UNCHECKED_CAST")
				val download = configuration["download"] as? Map<String, Any?> ?: return
				val downloadUrl = download[getOS()] as? String ?: return
				val needsUpdate = isNewVersionAvailable(version)
				
				Log.d("Server configuration: version=%s  download_url=%s  needs_update=%s", version, downloadUrl, needsUpdate)
				LauncherData.INSTANCE.general.remoteVersion = version
				this.downloadUrl.set(downloadUrl)
				if (needsUpdate)
					FX.eventbus.fire(LauncherNewVersionEvent)
			}
		} catch (e: IOException) {
			Log.e("Failed to retrieve updated server configuration")
		}
	}
	
	fun download() {
		val downloadUrl = this.downloadUrl.get()
		if (downloadUrl == null) {
			Log.w("Cannot update - download url is empty")
			return
		}
		
		Log.i("Launching download URL: %s", downloadUrl)
		LauncherData.INSTANCE.application.hostServices.showDocument(downloadUrl)
	}
	
	private fun isNewVersionAvailable(specifiedVersionStr: String?): Boolean {
		if (specifiedVersionStr == null) return true
		val currentVersion = LauncherData.VERSION.split(".")
		val specifiedVersion = specifiedVersionStr.split(".")
		
		for ((cur, spec) in currentVersion.zip(specifiedVersion)) {
			val curInt = Integer.parseUnsignedInt(cur)
			val specInt = Integer.parseUnsignedInt(spec)
			if (curInt == specInt)
				continue
			
			if (curInt < specInt)
				return true
		}
		
		return false
	}
	
	private fun getOS(): String {
		val os = System.getProperty("os.name").lowercase(Locale.US)
		if (os.contains("win"))
			return "windows"
		if (os.contains("mac"))
			return "mac"
		return "linux"
	}
	
}
