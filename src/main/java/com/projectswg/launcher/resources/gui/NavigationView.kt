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

import com.projectswg.launcher.resources.gui.style.Style
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.geometry.Side
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class NavigationView : View("ProjectSWG Launcher") {
	
	override val root = anchorpane {
		addClass(Style.background)
		
		tabpane {
			prefWidth = 900.0
			prefHeight = 450.0
			vgrow = Priority.ALWAYS
			side = Side.LEFT
			
			AnchorPane.setTopAnchor(this, 0.0)
			AnchorPane.setRightAnchor(this, 0.0)
			AnchorPane.setBottomAnchor(this, 0.0)
			AnchorPane.setLeftAnchor(this, 0.0)
			
			tab(messages["servers"]) {
				isClosable = false
				graphic = FontAwesomeIcon.SERVER.createGlyph(glyphSize = 24, fill = Color.LIGHTGRAY)
				graphic.tooltip(messages["servers"])
				
				this += find<ServerListView>().root
				selectionModel.select(this)
			}
			tab(messages["settings"]) {
				isClosable = false
				graphic = FontAwesomeIcon.SLIDERS.createGlyph(glyphSize = 24, fill = Color.LIGHTGRAY)
				graphic.tooltip(messages["settings"])
				
				this += find<SettingsView>().root
			}
		}
	}
	
}
