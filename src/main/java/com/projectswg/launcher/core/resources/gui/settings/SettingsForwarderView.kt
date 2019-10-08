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
import com.projectswg.launcher.core.resources.data.forwarder.ForwarderData
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.util.converter.NumberStringConverter
import tornadofx.View

class SettingsForwarderView : View() {
	
	override val root: Parent by fxml()
	private val sendIntervalTextField: TextField by fxid()
	private val sendMaxTextField: TextField by fxid()
	private val resetButton: Button by fxid()
	
	init {
		sendIntervalTextField.text = LauncherData.INSTANCE.forwarderData.sendInterval.toString()
		sendMaxTextField.text = LauncherData.INSTANCE.forwarderData.sendMax.toString()
		sendIntervalTextField.textProperty().bindBidirectional(LauncherData.INSTANCE.forwarderData.sendIntervalProperty, NumberStringConverter())
		sendMaxTextField.textProperty().bindBidirectional(LauncherData.INSTANCE.forwarderData.sendMaxProperty, NumberStringConverter())
		
		resetButton.setOnAction {
			sendIntervalTextField.text = ForwarderData.DEFAULT_SEND_INTERVAL.toString()
			sendMaxTextField.text = ForwarderData.DEFAULT_SEND_MAX.toString()
		}
	}
	
}
