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

import com.projectswg.launcher.core.resources.data.LauncherData;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public enum ProcessExecutor {
	INSTANCE;
	
	@Nullable
	public Process buildProcess(@NotNull UpdateServer server, String executable, String ... args) {
		File swgDirectory;
		{
			swgDirectory = new File(server.getLocalPath());
			if (!swgDirectory.isDirectory()) {
				Log.e("Failed to launch. Invalid SWG directory: %s", swgDirectory);
				reportError("Process", "Invalid SWG directory: " + swgDirectory);
				return null;
			}
		}
		File swg = new File(swgDirectory, executable);
		if (!swg.isFile()) {
			Log.e("Failed to launch. Invalid executable file (%s)", swg);
			reportError("Process", "Invalid executable: " + swg);
			return null;
		}
		String[] commands = isWindows() ? buildWindowsArgs(swg, args) : buildWineArgs(swg, args);
		if (commands == null)
			return null;
		Log.d("Building process with arguments %s", Arrays.asList(commands));
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		pb.directory(swg.getParentFile());
		pb.environment().put("WINEDEBUG", "-all");
		pb.environment().put("mesa_glthread", "true");
		pb.environment().put("__GL_THREADED_OPTIMIZATIONS", "1");
		try {
			Log.i("Starting executable %s in directory %s", Thread.currentThread().getName(), swg.getParentFile());
			return pb.start();
		} catch (IOException e) {
			Log.e("Failed to launch. %s: %s", e.getClass().getName(), e.getMessage());
			reportError("Game - Process", e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}
	
	@Nullable
	private String[] buildWineArgs(@NotNull final File swg, @NotNull final String [] args) {
		File wine = getWine();
		if (wine == null) {
			Log.e("Failed to launch game. Invalid wine configuration");
			reportError("Wine Initialization", "Failed to locate your local wine installation. Please set the correct path in settings");
			return null;
		}
		String[] baseArgs = buildWindowsArgs(swg, args);
		String[] combined = new String[baseArgs.length+1];
		combined[0] = getFileCanonicalIfPossible(wine);
		System.arraycopy(baseArgs, 0, combined, 1, baseArgs.length);
		return combined;
	}
	
	@NotNull
	private String[] buildWindowsArgs(@NotNull final File swg, @NotNull final String [] baseArgs) {
		String[] combined = new String[baseArgs.length+1];
		combined[0] = getFileCanonicalIfPossible(swg);
		System.arraycopy(baseArgs, 0, combined, 1, baseArgs.length);
		return combined;
	}
	
	@NotNull
	private String getFileCanonicalIfPossible(@NotNull final File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			String absolute = file.getAbsolutePath();
			Log.w("Issue when launching game. Could not get canonical path (%s: %s) defaulting to absolute: %s", e.getClass().getName(), e.getMessage(), absolute);
			return absolute;
		}
	}
	
	private boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
	
	private File getWine() {
		{
			String wineStr = LauncherData.INSTANCE.getGeneral().getWine();
			if (wineStr != null) {
				File wine = new File(wineStr);
				if (wine.isFile()) {
					return new File(wineStr);
				} else {
					Log.e("Invalid wine file: " + wineStr);
					reportWarning("Wine Initialization", "Invalid wine setting. Searching for valid path...");
				}
			}
		}
		Log.w("Wine binary is not defined - searching...");
		String pathStr = System.getenv("PATH");
		if (pathStr == null)
			return null;
		
		for (String path : pathStr.split(File.pathSeparator)) {
			Log.t("Testing wine binary at %s", path);
			File test = new File(path, "wine");
			if (test.isFile()) {
				try {
					test = test.getCanonicalFile();
					Log.d("Found wine installation. Location: %s", test);
					return test;
				} catch (IOException e) {
					Log.w("Failed to get canonical file location of possible wine location: %s", test);
				}
			}
		}
		return null;
	}
	
	private void reportWarning(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Game Launch Warning");
			alert.setHeaderText(title);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
	
	private void reportError(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Game Launch Error");
			alert.setHeaderText(title);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
}
