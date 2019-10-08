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

import com.projectswg.launcher.core.resources.gui.style.Style
import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.login.LoginServer
import com.projectswg.launcher.core.resources.gui.servers.ServerPlayCell
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableColumn
import tornadofx.*
import java.util.*

class ServerListView : View() {
	
	override val root = vbox {
		imageview(url = "/graphics/headers/server-table.png") {
			fitWidthProperty().bind(this@vbox.widthProperty())
		}
		tableview<LoginServer> {
			items = LauncherData.INSTANCE.login.serversProperty
			isFocusTraversable = false
			placeholder = label(messages["noServers"])
			setSortPolicy { _ -> Comparator.comparing<LoginServer, String> { it.name }; true }
			
			column(messages["servers.column.name"], LoginServer::nameProperty) {
				prefWidth = COL_WIDTH_LARGE
				styleClass += "center-table-cell"
			}
			column(messages["servers.column.gameVersion"], valueProvider={cellDataFeatures:TableColumn.CellDataFeatures<LoginServer, String> -> cellDataFeatures.value?.updateServerProperty?.select { it.gameVersionProperty } ?: ReadOnlyStringWrapper("N/A") }).apply {
				prefWidth = COL_WIDTH_MEDIUM
				styleClass += "center-table-cell"
			}
			column(messages["servers.column.remoteStatus"], valueProvider={cellDataFeatures:TableColumn.CellDataFeatures<LoginServer, String> -> cellDataFeatures.value?.instanceInfo?.loginStatusProperty ?: ReadOnlyStringWrapper("") } ).apply {
				cellFormat {
					text = it
					toggleClass(Style.statusFail, it == "OFFLINE")
					toggleClass(Style.statusInProgress, it == FX.messages["servers.loginStatus.checking"] || it == "LOADING")
					toggleClass(Style.statusGood, it == "UP")
				}
				
				prefWidth = COL_WIDTH_LARGE
				styleClass += "center-table-cell"
			}
			column(messages["servers.column.localStatus"], valueProvider={cellDataFeatures:TableColumn.CellDataFeatures<LoginServer, String> -> cellDataFeatures.value?.instanceInfo?.updateStatusProperty ?: ReadOnlyStringWrapper("") } ).apply {
				cellFormat {
					text = messages[it]
				}
				prefWidth = COL_WIDTH_LARGE
				styleClass += "center-table-cell"
			}
			column(messages["servers.column.play"], LoginServer::class) {
//				setCellFactory { ServerPlayCell() }
				cellFragment(ServerPlayCell::class)
				setCellValueFactory { param -> SimpleObjectProperty(param.value) }
				
				prefWidth = COL_WIDTH_LARGE
				styleClass.add("center-table-cell")
			}
		}
		region { prefHeight = 5.0 }
		this += find<CardContainer>(mapOf(CardContainer::children to LauncherData.INSTANCE.announcements.serverListCardsProperty)).root
	}
	
	companion object {
		
		private const val COL_WIDTH_MEDIUM = 110.0
		private const val COL_WIDTH_LARGE = 150.0
		
	}
	
}
