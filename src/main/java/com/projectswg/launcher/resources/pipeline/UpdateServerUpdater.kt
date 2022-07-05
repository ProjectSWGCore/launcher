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
package com.projectswg.launcher.resources.pipeline

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.data.update.UpdateServer
import com.projectswg.launcher.resources.data.update.UpdateServer.RequiredFile
import com.projectswg.launcher.resources.data.update.UpdateServer.UpdateServerStatus
import javafx.application.Platform
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.json.JSONException
import me.joshlarson.json.JSONInputStream
import me.joshlarson.json.JSONOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.stream.Collectors

object UpdateServerUpdater {
	
	fun update(server: UpdateServer) {
		val localPath = LauncherData.INSTANCE.update.localPath
		if (!File(localPath).isDirectory) {
			Log.e("Not a valid local path: %s", localPath)
			return
		}
		val info = UpdateServerDownloaderInfo(server, localPath)
		if (!updateFileList(info))
			return
		filterValidFiles(info)
		updateServerStatus(info)
	}
	
	/**
	 * Stage 1: Download the file list from the update server, or fall back on the local copy. If neither are accessible, fail.
	 */
	private fun updateFileList(info: UpdateServerDownloaderInfo): Boolean {
		Log.t("Retrieving latest file list from %s...", info.url)
		val localFileList = File(info.localPath, "files.json")
		var files: List<Any?>
		try {
			JSONInputStream(createURL(info, "files.json").openConnection().getInputStream()).use { `in` ->
				files = `in`.readArray()
				try {
					JSONOutputStream(FileOutputStream(localFileList)).use { out -> out.writeArray(files) }
				} catch (e: IOException) {
					Log.e("Failed to write updated file list to disk for update server %s (%s: %s)", info.name, e.javaClass.name, e.message)
				}
			}
		} catch (e: IOException) {
			Log.w("Failed to retrieve latest file list for update server %s (%s: %s). Falling back on local copy...", e.javaClass.name, e.message, info.name)
			try {
				JSONInputStream(FileInputStream(localFileList)).use { `in` -> files = `in`.readArray() }
			} catch (t: JSONException) {
				Log.e("Failed to read file list from disk on update server %s with path %s. Aborting update.", info.name, localFileList)
				return false
			} catch (t: IOException) {
				Log.e("Failed to read file list from disk on update server %s with path %s. Aborting update.", info.name, localFileList)
				return false
			}
		} catch (e: JSONException) {
			Log.w("Failed to retrieve latest file list for update server %s (%s: %s). Falling back on local copy...", e.javaClass.name, e.message, info.name)
			try {
				JSONInputStream(FileInputStream(localFileList)).use { `in` -> files = `in`.readArray() }
			} catch (t: JSONException) {
				Log.e("Failed to read file list from disk on update server %s with path %s. Aborting update.", info.name, localFileList)
				return false
			} catch (t: IOException) {
				Log.e("Failed to read file list from disk on update server %s with path %s. Aborting update.", info.name, localFileList)
				return false
			}
		}
		info.files = files.stream()
			.filter { obj: Any? -> MutableMap::class.java.isInstance(obj) }
			.map { obj: Any? -> MutableMap::class.java.cast(obj) }
			.map { obj -> @Suppress("UNCHECKED_CAST") jsonObjectToRequiredFile(info, obj as? Map<String, Any> ?: return@map null ) }
			.filter { it != null }
			.collect(Collectors.toList())
		return true
	}
	
	/**
	 * Stage 2: Scan each file and only keep the ones that need to be downloaded.
	 */
	private fun filterValidFiles(info: UpdateServerDownloaderInfo) {
		val files: MutableList<RequiredFile> = Objects.requireNonNull<MutableList<RequiredFile>?>(info.files, "File list was not read correctly")
		Log.d("%d known files. Scanning...", files.size)
		val total = files.size
		Platform.runLater { info.server.status = UpdateServerStatus.SCANNING }
		files.removeIf { obj: RequiredFile -> isValidFile(obj) }
		val valid = total - files.size
		Log.d("Completed scan of update server %s. %d of %d valid.", info.name, valid, total)
	}
	
	/**
	 * Stage 3: Update the UpdateServer status and the required files.
	 */
	private fun updateServerStatus(info: UpdateServerDownloaderInfo) {
		val serverList: MutableList<RequiredFile> = info.server.requiredFiles
		val updateList: List<RequiredFile> = info.files ?: return
		val updateStatus = if (updateList.isEmpty()) UpdateServerStatus.READY else UpdateServerStatus.REQUIRES_DOWNLOAD
		serverList.clear()
		serverList.addAll(updateList)
		Platform.runLater { info.server.status = updateStatus }
		Log.d("Setting update server '%s' status to %s", info.name, updateStatus)
	}
	
	private fun isValidFile(file: RequiredFile): Boolean {
		val localFile = file.localPath
		val length = localFile.length()
		return localFile.isFile && length == file.length
	}
	
	private fun jsonObjectToRequiredFile(info: UpdateServerDownloaderInfo, obj: Map<String, Any>): RequiredFile {
		val path = obj["path"] as String? ?: throw RuntimeException("no path defined for file")
		return try {
			RequiredFile(File(info.localPath, path), createURL(info, path), obj["length"] as Long, (obj["xxhash"] as String?)!!)
		} catch (e: MalformedURLException) {
			throw RuntimeException(e)
		}
	}
	
	@Throws(MalformedURLException::class)
	private fun createURL(info: UpdateServerDownloaderInfo, path: String): URL {
		var url = info.url
		while (url.endsWith("/"))
			url = url.removeSuffix("/")
		
		return URL(url + "/" + path.removePrefix("/"))
	}
	
	private class UpdateServerDownloaderInfo(val server: UpdateServer, localPath: String) {
		val name: String = server.name
		val url: String = server.url
		val localPath: File = File(localPath, server.gameVersion)
		
		var files: MutableList<RequiredFile>? = null
	}
}