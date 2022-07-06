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

package com.projectswg.launcher.resources.gui

import com.projectswg.launcher.resources.gui.settings.SettingsForwarderView
import com.projectswg.launcher.resources.gui.settings.SettingsGeneralView
import com.projectswg.launcher.resources.gui.settings.SettingsLoginView
import com.projectswg.launcher.resources.gui.settings.SettingsUpdateView
import com.projectswg.launcher.resources.gui.style.Style
import javafx.geometry.Insets
import tornadofx.*

class SettingsView : View() {
	
	override val root = scrollpane {
		isFitToWidth = true
		isFitToHeight = true
		
		vbox {
			addClass(Style.background)
			padding = Insets(10.0)
			
			children.add(find<SettingsGeneralView>().root)
			separator()
			children.add(find<SettingsLoginView>().root)
			separator()
			children.add(find<SettingsUpdateView>().root)
			separator()
			children.add(find<SettingsForwarderView>().root)
		}
	}
}
