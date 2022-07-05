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

package com.projectswg.launcher.resources.data.login

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.joshlarson.jlcommon.javafx.beans.ConcurrentSet
import tornadofx.getValue
import tornadofx.select
import tornadofx.setValue
import java.util.concurrent.CopyOnWriteArraySet

class LoginData {
	
	val authenticationData = ConcurrentSet<AuthenticationData>(CopyOnWriteArraySet())
	val authenticationProperty: ObservableList<AuthenticationData> = FXCollections.observableArrayList()
	
	val servers = ConcurrentSet<LoginServer>(CopyOnWriteArraySet())
	val serversProperty: ObservableList<LoginServer> = FXCollections.observableArrayList()
	
	val activeServerProperty = SimpleObjectProperty<LoginServer>()
	val lastSelectedServerProperty = SimpleStringProperty()
	
	val localServerProperty = SimpleObjectProperty<LoginServer>()
	
	var activeServer: LoginServer? by activeServerProperty
	val lastSelectedServer: String by lastSelectedServerProperty
	
	var localServer: LoginServer by localServerProperty
	
	init {
		servers.addCollectionChangedListener("", Runnable { serversProperty.setAll(servers) })
		authenticationData.addCollectionChangedListener("", Runnable { authenticationProperty.setAll(authenticationData) })
		lastSelectedServerProperty.bind(activeServerProperty.select { it.nameProperty })
	}
	
	fun getAuthenticationData(authentication_name: String): AuthenticationData? {
		for (authentication in authenticationData) {
			if (authentication.name == authentication_name)
				return authentication
		}
		
		return null
	}
	
	fun getServerByName(serverName: String): LoginServer? {
		for (server in servers) {
			if (server.name == serverName)
				return server
		}
		
		return null
	}
	
}
