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

import com.projectswg.launcher.core.resources.data.announcements.CardData
import com.projectswg.launcher.core.resources.gui.style.CardStyle
import javafx.application.Platform
import javafx.collections.ObservableList
import me.joshlarson.jlcommon.log.Log
import tornadofx.*
import kotlin.math.max

class CardContainer : Fragment() {
	
	override val root = flowpane {
		importStylesheet(CardStyle::class)
		addClass(CardStyle.cardContainer)
		
		minWidth = 300.0
		minHeight = 300.0
	}
	
	val children: ObservableList<CardData> by param()
	
	init {
		root.bindChildren(children) { 
			val card = find<Card>(mapOf(Card::data to it)).root
			card.prefWidthProperty().bind(root.widthProperty().doubleBinding { max -> createCardSize(max?.toDouble() ?: 0.0) })
			card.prefHeightProperty().bind(root.heightProperty().doubleBinding { max -> createCardSize(max?.toDouble() ?: 0.0) })
//			card.prefWidth = createCardSize(root.width)
//			card.prefHeight = createCardSize(root.height)
			Platform.runLater { root.applyCss() }
			Log.t("Initializing card '${it.title}' with size ${card.prefWidth}x${card.prefHeight}")
			card
		}
	}
	
	private fun createCardSize(max: Double): Double {
		val count = max(1, (max / 300).toInt())
		return ((max - (count - 1) * 10) / count - 0.5).toInt().toDouble()
	}
	
}
