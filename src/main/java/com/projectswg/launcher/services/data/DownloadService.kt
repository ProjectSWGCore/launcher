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

import com.projectswg.launcher.resources.data.update.UpdateServer
import com.projectswg.launcher.resources.data.update.UpdateServer.RequiredFile
import com.projectswg.launcher.resources.data.update.UpdateServer.UpdateServerStatus
import com.projectswg.launcher.resources.intents.CancelDownloadIntent
import com.projectswg.launcher.resources.intents.DownloadPatchIntent
import com.projectswg.launcher.resources.intents.RequestScanIntent
import javafx.application.Platform
import javafx.beans.binding.NumberBinding
import javafx.beans.property.LongProperty
import javafx.beans.property.SimpleLongProperty
import me.joshlarson.jlcommon.concurrency.ThreadPool
import me.joshlarson.jlcommon.control.IntentHandler
import me.joshlarson.jlcommon.control.Service
import me.joshlarson.jlcommon.log.Log
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class DownloadService : Service() {
	
	private val threadPool: ThreadPool = ThreadPool(4, "downloader-%d")
	private val threadDownloaders = ConcurrentHashMap<UpdateServer, Thread>()
	
	override fun start(): Boolean {
		threadPool.start()
		return true
	}
	
	override fun stop(): Boolean {
		threadPool.stop(true)
		threadPool.awaitTermination(1000)
		return true
	}
	
	@IntentHandler
	private fun handleCancelDownloadIntent(cdi: CancelDownloadIntent) {
		val thread = threadDownloaders[cdi.server]
		thread?.interrupt()
	}
	
	@IntentHandler
	private fun handleDownloadPatchIntent(dpi: DownloadPatchIntent) {
		if (threadDownloaders.putIfAbsent(dpi.server, Thread.currentThread()) != null)
			return
		Log.t("Attempting download of patch files from %s", dpi.server)
		val files = ArrayList(dpi.server.requiredFiles)
		if (files.isEmpty())
			return
		val running = AtomicBoolean(true)
		val dataTransferred = SimpleLongProperty(0)
		val fileLockPool = Semaphore(1 - files.size)
		
		// Queue each download in the thread pool
		var dataTransferredBinding: NumberBinding = dataTransferred.add(0L)
		for (file in files) {
			Log.t("Downloading %s -> %s", file.remotePath, file.localPath)
			val downloadAmount = SimpleLongProperty()
			dataTransferredBinding = dataTransferredBinding.add(downloadAmount)
			threadPool.execute { download(file, fileLockPool, downloadAmount, running) }
		}
		val totalDownloadSize = files.stream().mapToLong { it.length }.sum().toDouble()
		Platform.runLater { dpi.server.downloadProgressProperty.bind(dataTransferredBinding.divide(totalDownloadSize)) }
		
		// Waits for all files to complete, then is able to grab the one remaining lock
		try {
			Log.d("Downloading %d files from %s...", files.size, dpi.server)
			Platform.runLater { dpi.server.status = UpdateServerStatus.DOWNLOADING }
			fileLockPool.acquire(1)
			Log.d("Completed all downloads (%d)", files.size)
			RequestScanIntent(dpi.server).broadcast()
		} catch (e: InterruptedException) {
			Log.w("Failed to complete all downloads. %d remaining", 1 - fileLockPool.availablePermits())
			running.set(false)
			RequestScanIntent(dpi.server).broadcast()
		} finally {
			threadDownloaders.remove(dpi.server)
		}
		Platform.runLater { dpi.server.status = UpdateServerStatus.UNKNOWN }
	}
	
	private fun download(file: RequiredFile, fileLockPool: Semaphore, dataTransferred: LongProperty, running: AtomicBoolean) {
		val fileParent = file.localPath.parentFile
		if (fileParent != null && !fileParent.isDirectory && !fileParent.mkdirs())
			Log.w("Failed to create parent directory: %s", fileParent)
		try {
			Channels.newChannel(file.remotePath.openStream()).use { rbc ->
				val downloadPath = File(file.localPath.path+".download")
				FileChannel.open(downloadPath.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE).use { fc ->
					val bb = ByteBuffer.allocateDirect(8 * 1024)
					var downloaded: Long = 0
					val expected = file.length
					while (downloaded < expected && running.get()) {
						bb.clear()
						val n = rbc.read(bb)
						if (n == -1)
							break
						bb.flip()
						downloaded += n
						fc.write(bb)
						Platform.runLater { dataTransferred.set(downloaded) }
					}
					Log.t("Completed download of %s", file.localPath)
				}
				Files.move(downloadPath.toPath(), file.localPath.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
			}
		} catch (e: IOException) {
			Log.e("Failed to download file %s from %s with error: %s: %s", file.localPath, file.remotePath, e.javaClass.name, e.message)
		} finally {
			fileLockPool.release(1)
		}
	}
	
}
