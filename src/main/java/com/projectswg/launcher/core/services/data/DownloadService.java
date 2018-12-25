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

import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.RequiredFile;
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus;
import com.projectswg.launcher.core.resources.intents.CancelDownloadIntent;
import com.projectswg.launcher.core.resources.intents.DownloadPatchIntent;
import com.projectswg.launcher.core.resources.intents.RequestScanIntent;
import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.control.IntentHandler;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentLong;
import me.joshlarson.jlcommon.log.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DownloadService extends Service {
	
	private final ThreadPool threadPool;
	private final Map<UpdateServer, Thread> threadDownloaders;
	
	public DownloadService() {
		this.threadPool = new ThreadPool(4, "downloader-%d");
		this.threadDownloaders = new ConcurrentHashMap<>();
	}
	
	@Override
	public boolean start() {
		threadPool.start();
		return true;
	}
	
	@Override
	public boolean stop() {
		threadPool.stop(true);
		threadPool.awaitTermination(1000);
		return true;
	}
	
	@IntentHandler
	private void handleCancelDownloadIntent(CancelDownloadIntent cdi) {
		Thread thread = threadDownloaders.get(cdi.getServer());
		if (thread != null)
			thread.interrupt();
	}
	
	@IntentHandler
	private void handleDownloadPatchIntent(DownloadPatchIntent dpi) {
		if (threadDownloaders.putIfAbsent(dpi.getServer(), Thread.currentThread()) != null)
			return;
		Log.t("Attempting download of patch files from %s", dpi.getServer());
		Collection<RequiredFile> files = new ArrayList<>(dpi.getServer().getRequiredFiles());
		if (files.isEmpty())
			return;
		AtomicBoolean running = new AtomicBoolean(true);
		ConcurrentLong dataTransferred = new ConcurrentLong(0);
		Semaphore fileLockPool = new Semaphore(1 - files.size());
		
		// Setup the overall data transfer callback
		Consumer<Double> callback = dpi.getCallback();
		if (callback != null) {
			final double totalTransfer = files.stream().mapToLong(RequiredFile::getLength).sum();
			dataTransferred.addTransformListener(t -> t / totalTransfer, callback);
		}
		
		// Queue each download in the thread pool
		for (RequiredFile file : files) {
			Log.t("Downloading %s -> %s", file.getRemotePath(), file.getLocalPath());
			threadPool.execute(() -> download(file, fileLockPool, dataTransferred, running));
		}
		
		// Waits for all files to complete, then is able to grab the one remaining lock
		try {
			Log.d("Downloading %d files from %s...", files.size(), dpi.getServer());
			dpi.getServer().setStatus(UpdateServerStatus.DOWNLOADING);
			fileLockPool.acquire(1);
			Log.d("Completed all downloads (%d)", files.size());
			RequestScanIntent.broadcast(dpi.getServer());
		} catch (InterruptedException e) {
			Log.w("Failed to complete all downloads. %d remaining", 1 -fileLockPool.availablePermits());
			running.set(false);
			RequestScanIntent.broadcast(dpi.getServer());
		} finally {
			threadDownloaders.remove(dpi.getServer());
		}
		dpi.getServer().setStatus(UpdateServerStatus.UNKNOWN);
	}
	
	private static void download(RequiredFile file, Semaphore fileLockPool, ConcurrentLong dataTransferred, AtomicBoolean running) {
		File fileParent = file.getLocalPath().getParentFile();
		if (fileParent != null && !fileParent.isDirectory() && !fileParent.mkdirs())
			Log.w("Failed to create parent directory: %s", fileParent);
		try (ReadableByteChannel rbc = Channels.newChannel(file.getRemotePath().openStream()); FileChannel fc = FileChannel.open(file.getLocalPath().toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			ByteBuffer bb = ByteBuffer.allocateDirect(8*1024);
			long downloaded = 0;
			long expected = file.getLength();
			while (downloaded < expected && running.get()) {
				bb.clear();
				long n = rbc.read(bb);
				if (n == -1)
					break;
				bb.flip();
				dataTransferred.addAndGet(n);
				downloaded += n;
				fc.write(bb);
			}
			Log.t("Completed download of %s", file.getLocalPath());
		} catch (IOException e) {
			Log.e("Failed to download file %s from %s with error: %s: %s", file.getLocalPath(), file.getRemotePath(), e.getClass().getName(), e.getMessage());
		} finally {
			fileLockPool.release(1);
		}
	}
	
}
