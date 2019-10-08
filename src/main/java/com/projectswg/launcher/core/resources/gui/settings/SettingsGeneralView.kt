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
import com.projectswg.launcher.core.resources.data.general.LauncherTheme
import com.projectswg.launcher.core.resources.gui.createGlyph
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import tornadofx.View
import java.io.File
import java.io.IOException
import java.util.*

class SettingsGeneralView : View() {
	
	override val root: Parent by fxml()
	private val soundCheckbox: CheckBox by fxid()
	private val themeComboBox: ComboBox<LauncherTheme> by fxid()
	private val localeComboBox: ComboBox<Locale> by fxid()
	private val wineTextField: TextField by fxid()
	private val wineSelectionButton: Button by fxid()
	private val adminCheckBox: CheckBox by fxid()
	
	init {
		val data = LauncherData.INSTANCE.general
		
		wineSelectionButton.graphic = FontAwesomeIcon.FOLDER_ALT.createGlyph()
		
		themeComboBox.items.setAll(*LauncherTheme.values())
		localeComboBox.items.setAll(Locale.ENGLISH, Locale.GERMAN)
		
		soundCheckbox.selectedProperty().bindBidirectional(data.soundProperty)
		themeComboBox.valueProperty().bindBidirectional(data.themeProperty)
		localeComboBox.valueProperty().bindBidirectional(data.localeProperty)
		wineTextField.textProperty().bindBidirectional(data.wineProperty)
		wineSelectionButton.setOnAction { this.processWineSelectionButtonAction() }
		adminCheckBox.selectedProperty().bindBidirectional(data.adminProperty)
	}
	
	private fun processWineSelectionButtonAction() {
		val selection = chooseOpenFile("Choose Wine Path") ?: return
		try {
			wineTextField.text = selection.canonicalPath
		} catch (ex: IOException) {
			wineTextField.text = selection.absolutePath
		}
	}
	
	private fun chooseOpenFile(title: String): File? {
		val fileChooser = FileChooser()
		fileChooser.title = title
		val file = fileChooser.showOpenDialog(LauncherData.INSTANCE.stage)
		return if (file == null || !file.isFile) null else file
	}
	
}
