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
package com.projectswg.launcher.resources.data

import com.projectswg.launcher.resources.data.forwarder.ForwarderData
import com.projectswg.launcher.resources.data.general.GeneralData
import com.projectswg.launcher.resources.data.login.LoginData
import com.projectswg.launcher.resources.data.update.UpdateData
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.FX
import tornadofx.FX.Companion.primaryStage
import java.io.File
import java.util.*

enum class LauncherData {
	INSTANCE;
	
	val general = GeneralData()
	val login = LoginData()
	val update = UpdateData()
	val forwarder = ForwarderData()
	val stage: Stage
		get() = primaryStage
	
	val application: Application
		get() = FX.application
	
	companion object {
		const val VERSION = "2.1.2"
		
		fun getApplicationDataDirectory(): File {
			return when (getOS()) {
				"windows" -> File(System.getenv("APPDATA"), "ProjectSWG")
				"mac" -> File("${System.getProperty("user.home")}/Library/Application Support/ProjectSWG")
				else -> File("${System.getProperty("user.home")}/.config/ProjectSWG")
			}
		}
		
		private fun getOS(): String {
			val currentOs = System.getProperty("os.name").lowercase(Locale.US)
			return when {
				currentOs.contains("win") -> "windows"
				currentOs.contains("mac") -> "mac"
				else -> "linux"
			}
		}
		
	}
}