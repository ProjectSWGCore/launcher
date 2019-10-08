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

package com.projectswg.launcher.core.resources.gui

import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.announcements.CardData
import com.projectswg.launcher.core.resources.gui.style.CardStyle
import javafx.scene.Cursor
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import tornadofx.*

class Card : Fragment() {
	
	val data: CardData by param()
	
	override val root = vbox {
		importStylesheet(CardStyle::class)
		isFillWidth = true
		addClass(CardStyle.card)
		hgrow = Priority.ALWAYS
		vgrow = Priority.ALWAYS
		
		imageview("file://"+data.imageUrl) {
			isPreserveRatio = true
			fitHeight = 108.0
			addClass(CardStyle.cardTitle)
			
			fitWidthProperty().bind(this@vbox.widthProperty())
			
			if (data.link != null) {
				// Clicking the image takes you to the link
				Tooltip.install(this, Tooltip(data.link))
				
				cursor = Cursor.HAND
				setOnMouseClicked { LauncherData.INSTANCE.application.hostServices.showDocument(data.link) }
			}
		}
		label(data.title) {
			addClass(CardStyle.cardTitle)
			
			maxWidthProperty().bind(this@vbox.widthProperty())
		}
		separator()
		textarea(data.description) {
			isEditable = false
			addClass(CardStyle.cardDescription)
			
			maxWidthProperty().bind(this@vbox.widthProperty())
		}
	}
	
}
