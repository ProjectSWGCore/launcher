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

import com.projectswg.launcher.resources.gui.createGlyph
import com.projectswg.launcher.resources.gui.style.Style
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.*

class SettingsGeneralView : View() {
	
	override val root = vbox {
		val data = com.projectswg.launcher.resources.data.LauncherData.INSTANCE.general
		
		label(messages["settings.general.header"]) {
			addClass(Style.settingsHeaderLabel)
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.general.locale"])
			combobox {
				items.setAll(Locale.ENGLISH, Locale.GERMAN)
				valueProperty().bindBidirectional(data.localeProperty)
			}
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.general.wine"])
			val winePathTextField = textfield {
				isDisable = true
				textProperty().bindBidirectional(data.wineProperty)
			}
			region {
				prefWidth = 10.0
			}
			button {
				graphic = FontAwesomeIcon.FOLDER_ALT.createGlyph()
				setOnAction {
					processWineSelectionButtonAction(winePathTextField)
				}
				
				style {
					prefWidth = 30.px
				}
			}
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.general.admin"])
			checkbox {
				selectedProperty().bindBidirectional(data.adminProperty)
			}
			region {
				prefWidth = 10.0
			}
			label(messages["settings.general.admin_disclaimer"]) {
				maxWidth = Double.POSITIVE_INFINITY
				hgrow = Priority.ALWAYS
				style {
					fontSize = 10.px
				}
			}
		}
	}
	
	private fun processWineSelectionButtonAction(winePathTextField: TextField) {
		val selection = chooseWinePath() ?: return
		try {
			winePathTextField.text = selection.canonicalPath
		} catch (ex: IOException) {
			winePathTextField.text = selection.absolutePath
		}
	}
	
	private fun chooseWinePath(): File? {
		val fileChooser = FileChooser()
		fileChooser.title = "Choose Wine Path"
		val file = fileChooser.showOpenDialog(com.projectswg.launcher.resources.data.LauncherData.INSTANCE.stage)
		return if (file == null || !file.isFile) null else file
	}
	
}
