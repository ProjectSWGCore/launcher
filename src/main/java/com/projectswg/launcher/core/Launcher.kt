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

package com.projectswg.launcher.core

import com.projectswg.common.utilities.LocalUtilities
import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.gui.NavigationView
import com.projectswg.launcher.core.resources.gui.style.Style
import com.projectswg.launcher.core.services.data.DataManager
import com.projectswg.launcher.core.services.launcher.LauncherManager
import javafx.scene.image.Image
import javafx.stage.Stage
import me.joshlarson.jlcommon.control.IntentManager
import me.joshlarson.jlcommon.control.Manager
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.jlcommon.log.log_wrapper.ConsoleLogWrapper
import me.joshlarson.jlcommon.log.log_wrapper.FileLogWrapper
import tornadofx.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	LocalUtilities.setApplicationName(".projectswg/launcher")
	Log.addWrapper(ConsoleLogWrapper())
	Log.addWrapper(FileLogWrapper(File(LocalUtilities.getApplicationDirectory(), "log.txt")))
	
	try {
		launch<LauncherApp>(args)
	} finally {
		exitProcess(0)
	}
}

class Launcher

class LauncherApp: App(NavigationView::class, Style::class) {
	
	private val intentManager = IntentManager(Runtime.getRuntime().availableProcessors())
	private val services = listOf(DataManager(), LauncherManager())
	
	init {
		IntentManager.setInstance(intentManager)
		FX.localeProperty().bind(LauncherData.INSTANCE.general.localeProperty)
		FX.messages = ResourceBundle.getBundle("strings.strings", LauncherData.INSTANCE.general.locale)
		LauncherData.INSTANCE.general.localeProperty.addListener {_, _, locale ->
			FX.messages = ResourceBundle.getBundle("strings.strings", locale)
			FX.primaryStage.scene.reloadStylesheets()
			FX.primaryStage.scene.findUIComponents().forEach {
				FX.replaceComponent(it)
			}
		}
	}
	
	override fun start(stage: Stage) {
		services.forEach { it.setIntentManager(intentManager) }
		Manager.start(services)
		super.start(stage)
		stage.isResizable = false
		stage.icons.add(Image(resources["/graphics/ProjectSWG.png"]))
	}
	
	override fun stop() {
		services.forEach { it.setIntentManager(null) }
		intentManager.close()
		Manager.stop(services.reversed())
		super.stop()
	}
}
