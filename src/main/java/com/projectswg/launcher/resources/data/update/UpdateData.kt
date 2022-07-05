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
package com.projectswg.launcher.resources.data.update

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.joshlarson.jlcommon.javafx.beans.ConcurrentSet
import tornadofx.getValue
import tornadofx.setValue
import java.util.concurrent.CopyOnWriteArraySet

class UpdateData {
	
	val servers: ConcurrentSet<UpdateServer> = ConcurrentSet(CopyOnWriteArraySet())
	val serversProperty: ObservableList<UpdateServer> = FXCollections.observableArrayList()
	
	val localPathProperty = SimpleStringProperty("")
	var localPath: String by localPathProperty
	
	init {
		servers.addCollectionChangedListener("", Runnable { serversProperty.setAll(servers) })
	}
	
	fun getServer(server_name: String): UpdateServer? {
		for (server in servers) {
			if (server.name == server_name)
				return server
		}
		
		return null
	}
	
}