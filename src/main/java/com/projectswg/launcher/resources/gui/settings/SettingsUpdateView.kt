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

package com.projectswg.launcher.resources.gui.settings

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.gui.createGlyph
import com.projectswg.launcher.resources.gui.style.Style
import com.projectswg.launcher.resources.intents.RequestScanIntent
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.io.File
import java.io.IOException

class SettingsUpdateView: View() {
	
	override val root = vbox {
		label(messages["settings.update.header"]) {
			addClass(Style.settingsHeaderLabel)
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.update.localPath"])
			val localPathTextField = textfield(LauncherData.INSTANCE.update.localPathProperty) {
				isDisable = true
			}
			region {
				prefWidth = 10.0
			}
			
			button("") {
				graphic = FontAwesomeIcon.FOLDER_ALT.createGlyph()
				setOnAction {
					 processLocalPathSelectionButtonAction(localPathTextField)
				}
				
				style {
					prefWidth = 30.px
				}
			}
		}
		
		region {
			prefHeight = 10.0
		}
		
	}
	
	private fun processLocalPathSelectionButtonAction(localPathTextField: TextField) {
		val currentFolder = File(localPathTextField.text)
		val popupFolder = if (currentFolder.exists()) currentFolder else File(System.getProperty("user.home"))
		val selection = chooseLocalInstallationDirectory(popupFolder) ?: return
		try {
			localPathTextField.text = selection.canonicalPath
		} catch (ex: IOException) {
			localPathTextField.text = selection.absolutePath
		}
		
		for (server in LauncherData.INSTANCE.update.servers) {
			RequestScanIntent(server).broadcast()
		}
	}

	private fun chooseLocalInstallationDirectory(currentDirectory: File): File? {
		val directoryChooser = DirectoryChooser()
		directoryChooser.title = "Choose Local Installation Path"
		directoryChooser.initialDirectory = currentDirectory
		val file = directoryChooser.showDialog(com.projectswg.launcher.resources.data.LauncherData.INSTANCE.stage)
		return if (file == null || !file.isDirectory) null else file
	}
	
}
