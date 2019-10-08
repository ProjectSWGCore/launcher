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

package com.projectswg.launcher.core.resources.data.login

import com.projectswg.launcher.core.resources.data.update.UpdateServer
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import tornadofx.getValue
import tornadofx.setValue

class LoginServer(val name: String) {
	
	val nameProperty = ReadOnlyStringWrapper(name)
	val addressProperty = SimpleStringProperty("")
	val portProperty = SimpleIntegerProperty(0)
	val usernameProperty = SimpleStringProperty("")
	val passwordProperty = SimpleStringProperty("")
	val updateServerProperty = SimpleObjectProperty<UpdateServer>(null)
	val verifyServerProperty = SimpleBooleanProperty(true)
	val enableEncryptionProperty = SimpleBooleanProperty(true)
	val instanceInfo = LoginServerInstanceInfo()
	
	private val updateServerStatusCallback= { _: ObservableValue<*>, _: UpdateServerStatus, status: UpdateServerStatus -> onUpdateServerStatusUpdated(status) }
	
	var address: String by addressProperty
	var port: Int by portProperty
	var username: String by usernameProperty
	var password: String by passwordProperty
	var updateServer: UpdateServer? by updateServerProperty
	var isVerifyServer: Boolean by verifyServerProperty
	var isEncryptionEnabled: Boolean by enableEncryptionProperty
	
	init {
		updateServerProperty.addListener { _, oldValue, newValue -> updateServerListener(oldValue, newValue) }
		instanceInfo.updateStatus = UpdateServerStatus.UNKNOWN.friendlyName
		updateServerListener(null, null)
	}
	
	override fun toString(): String {
		return name
	}
	
	private fun updateServerListener(prev: UpdateServer?, next: UpdateServer?) {
		prev?.statusProperty?.removeListener(updateServerStatusCallback)
		if (next != null) {
			next.statusProperty.addListener(updateServerStatusCallback)
			instanceInfo.isReadyToPlay = calculateReadyToPlay(next.status)
		} else {
			instanceInfo.isReadyToPlay = false
		}
	}
	
	private fun onUpdateServerStatusUpdated(status: UpdateServerStatus) {
		instanceInfo.isReadyToPlay = calculateReadyToPlay(status)
		instanceInfo.updateStatus = status.friendlyName
	}
	
	private fun calculateReadyToPlay(status: UpdateServerStatus): Boolean {
		return when (status) {
			UpdateServerStatus.UNKNOWN, UpdateServerStatus.READY -> true
			else -> false
		}
	}
	
}
