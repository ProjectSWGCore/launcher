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
import com.projectswg.launcher.resources.data.update.UpdateServer
import com.projectswg.launcher.resources.gui.style.Style
import tornadofx.*


class SettingsLoginView : View() {
	
	override val root = vbox {
		label(messages["settings.login.header"]) {
			addClass(Style.settingsHeaderLabel)
		}
		
		val localServer = LauncherData.INSTANCE.login.localServer
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.login.local_connection_url"])
			textfield {
				textProperty().bindBidirectional(localServer.connectionUriProperty)
			}
		}
		
		hbox {
			addClass(Style.settingsRow)
			label(messages["settings.login.update_server"])
			combobox<UpdateServer> {
				items = LauncherData.INSTANCE.update.serversProperty
				valueProperty().bindBidirectional(localServer.updateServerProperty)
			}
		}
	}
	
}
