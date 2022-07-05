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
import com.projectswg.launcher.resources.data.forwarder.ForwarderData
import com.projectswg.launcher.resources.gui.style.Style
import javafx.scene.control.TextField
import javafx.util.converter.NumberStringConverter
import tornadofx.*

class SettingsForwarderView : View() {
	
	override val root = vbox {
		val data = com.projectswg.launcher.resources.data.LauncherData.INSTANCE.forwarder
		
		label(messages["settings.forwarder.header"]) {
			addClass(Style.settingsHeaderLabel)
		}
		
		lateinit var sendIntervalTextField: TextField
		lateinit var sendMaxTextField: TextField
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.forwarder.sendInterval"])
			sendIntervalTextField = textfield(LauncherData.INSTANCE.forwarder.sendInterval.toString()) {
				textProperty().bindBidirectional(data.sendIntervalProperty, NumberStringConverter())
			}
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.forwarder.sendMax"])
			sendMaxTextField = textfield(LauncherData.INSTANCE.forwarder.sendMax.toString()) {
				textProperty().bindBidirectional(data.sendMaxProperty, NumberStringConverter())
			}
		}
		
		hbox {
			addClass(Style.settingsRow)
			label("")
			button(messages["settings.forwarder.reset"]) {
				setOnAction {
					val textConverter = NumberStringConverter()
					sendIntervalTextField.text = textConverter.toString(ForwarderData.DEFAULT_SEND_INTERVAL)
					sendMaxTextField.text = textConverter.toString(ForwarderData.DEFAULT_SEND_MAX)
				}
			}
		}
	}
	
}
