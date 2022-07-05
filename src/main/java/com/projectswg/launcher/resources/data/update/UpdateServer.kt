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

import com.projectswg.launcher.resources.data.LauncherData
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.getValue
import tornadofx.setValue
import java.io.File
import java.net.URL

class UpdateServer(val name: String) {
	
	val friendlyNameProperty = SimpleStringProperty("")
	val urlProperty = SimpleStringProperty("")
	val statusProperty = SimpleObjectProperty(UpdateServerStatus.UNKNOWN)
	val gameVersionProperty = SimpleStringProperty("")
	val requiredFiles: ObservableList<RequiredFile> = FXCollections.observableArrayList()
	val downloadProgressProperty = SimpleDoubleProperty(-1.0)
	
	val localPath: File
		get() {
			return File(LauncherData.INSTANCE.update.localPath, gameVersion)
		}
	
	var friendlyName: String by friendlyNameProperty
	var url: String by urlProperty
	var status: UpdateServerStatus by statusProperty
	var gameVersion: String by gameVersionProperty
	var downloadProgress by downloadProgressProperty
	
	override fun toString(): String {
		return name
	}
	
	class RequiredFile(val localPath: File, val remotePath: URL, val length: Long, val hash: String)
	
	enum class UpdateServerStatus constructor(val friendlyName: String) {
		UNKNOWN("servers.status.unknown"),
		SCANNING("servers.status.scanning"),
		REQUIRES_DOWNLOAD("servers.status.requires_download"),
		DOWNLOADING("servers.status.downloading"),
		READY("servers.status.ready")
	}
	
}
