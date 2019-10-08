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

package com.projectswg.launcher.core.resources.gui.settings

import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.update.UpdateServer
import com.projectswg.launcher.core.resources.game.ProcessExecutor
import com.projectswg.launcher.core.resources.gui.createGlyph
import com.projectswg.launcher.core.resources.intents.RequestScanIntent
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import javafx.util.converter.NumberStringConverter
import tornadofx.View
import tornadofx.select
import java.io.File
import java.io.IOException

class SettingsUpdateView: View() {
	
	override val root: Parent by fxml()
	private val nameComboBox: ComboBox<UpdateServer> by fxid()
	private val addressTextField: TextField by fxid()
	private val portTextField: TextField by fxid()
	private val basePathTextField: TextField by fxid()
	private val localPathTextField: TextField by fxid()
	private val scanButton: Button by fxid()
	private val clientOptionsButton: Button by fxid()
	private val localPathSelectionButton: Button by fxid()
	
	private val currentDirectory: File
		get() {
			val server = nameComboBox.value ?: return File(".")
			val localPathString = server.localPath
			if (localPathString.isEmpty())
				return File(".")
			val localPath = File(localPathString)
			return if (!localPath.isDirectory) File(".") else localPath
		}
	
	init {
		localPathSelectionButton.graphic = FontAwesomeIcon.FOLDER_ALT.createGlyph()
		
		// TODO: Add/remove login servers
		scanButton.onAction = EventHandler { this.processScanButtonAction() }
		clientOptionsButton.onAction = EventHandler { this.processClientOptionsButtonAction() }
		
		addressTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.addressProperty })
		portTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.portProperty }, NumberStringConverter("#"))
		basePathTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.basePathProperty })
		localPathTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.localPathProperty })
		localPathSelectionButton.onAction = EventHandler { this.processLocalPathSelectionButtonAction() }
		
		nameComboBox.items = FXCollections.observableArrayList(LauncherData.INSTANCE.update.servers)
		nameComboBox.value = nameComboBox.items.getOrNull(0)
	}
	
	private fun processScanButtonAction() {
		val server = nameComboBox.value ?: return
		RequestScanIntent(server).broadcast()
	}
	
	private fun processClientOptionsButtonAction() {
		val server = nameComboBox.value ?: return
		ProcessExecutor.INSTANCE.buildProcess(server, "SwgClientSetup_r.exe")
	}
	
	private fun processLocalPathSelectionButtonAction() {
		val selection = chooseOpenDirectory("Choose Local Installation Path", currentDirectory) ?: return
		try {
			localPathTextField.text = selection.canonicalPath
		} catch (ex: IOException) {
			localPathTextField.text = selection.absolutePath
		}
		
		val server = nameComboBox.value ?: return
		RequestScanIntent(server).broadcast()
	}
	
	private fun chooseOpenDirectory(title: String, currentDirectory: File): File? {
		val directoryChooser = DirectoryChooser()
		directoryChooser.title = title
		directoryChooser.initialDirectory = currentDirectory
		val file = directoryChooser.showDialog(LauncherData.INSTANCE.stage)
		return if (file == null || !file.isDirectory) null else file
	}
	
}
