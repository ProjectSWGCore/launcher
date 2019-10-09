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

package com.projectswg.launcher.core.resources.gui.servers

import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.login.LoginServer
import com.projectswg.launcher.core.resources.data.update.UpdateServer
import com.projectswg.launcher.core.resources.data.update.UpdateServer.UpdateServerStatus.*
import com.projectswg.launcher.core.resources.gui.admin.AdminDisplay
import com.projectswg.launcher.core.resources.game.GameInstance
import com.projectswg.launcher.core.resources.intents.CancelDownloadIntent
import com.projectswg.launcher.core.resources.intents.DownloadPatchIntent
import com.projectswg.launcher.core.resources.intents.GameLaunchedIntent
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableStringValue
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.util.converter.NumberStringConverter
import tornadofx.*

class ServerPlayCell : TableCellFragment<LoginServer, LoginServer>() {
	
	private val loginServerProperty = itemProperty
	private val updateServerProperty = loginServerProperty.select { it.updateServerProperty }
	private val updateServerStatusProperty = updateServerProperty.select { it.statusProperty }
	
	override val root = vbox {
		spacing = 5.0
		padding = Insets(5.0)
		alignment = Pos.CENTER
		styleClass.add("server-play-cell")
		
		button(updateServerStatusProperty.select { ReadOnlyStringWrapper(getButtonText(it)) }) {
			disableProperty().bind(updateServerStatusProperty.select { ReadOnlyBooleanWrapper(getButtonDisabled(it)) })
			setOnAction { onButtonClicked() }
		}
		
		label(updateServerStatusProperty.select { getLabelText(it) }) {
			managedProperty().bind(updateServerStatusProperty.select { ReadOnlyBooleanWrapper(getLabelVisible(it)) })
		}
	}
	
	private fun getButtonText(status: UpdateServer.UpdateServerStatus?): String {
		return when (status) {
			SCANNING, UNKNOWN, READY -> messages["servers.play.play"]
			REQUIRES_DOWNLOAD -> messages["servers.play.update"]
			DOWNLOADING -> messages["servers.play.cancel"]
			null -> ""
		}
	}
	private fun getButtonDisabled(status: UpdateServer.UpdateServerStatus?): Boolean = status == SCANNING
	
	private fun onButtonClicked() {
		when (updateServerStatusProperty.value) {
			UNKNOWN, READY -> {
				val loginServer = loginServerProperty.value ?: return
				runAsync {
					val gameInstance = GameInstance(loginServer)
					gameInstance.start()
					GameLaunchedIntent(gameInstance).broadcast()
					gameInstance
//				} ui {
//					if (LauncherData.INSTANCE.general.isAdmin) {
//						find<AdminDisplay>(AdminDisplay::forwarder to it.forwarder).openWindow()
						// TODO: Fix admin display
//					}
				}
			}
			REQUIRES_DOWNLOAD -> DownloadPatchIntent(updateServerProperty.value ?: return).broadcast()
			DOWNLOADING -> CancelDownloadIntent(updateServerProperty.value ?: return).broadcast()
			else -> { }
		}
	}
	
	private fun getLabelText(status: UpdateServer.UpdateServerStatus?): ObservableStringValue {
		return when (status) {
			null, UNKNOWN, READY, SCANNING -> ReadOnlyStringWrapper(messages["servers.action_info.empty"])
			REQUIRES_DOWNLOAD -> ReadOnlyStringWrapper(calculateDownloadSize(updateServerProperty.value?.requiredFiles ?: return ReadOnlyStringWrapper("")) + " " + messages["servers.action_info.required"])
			DOWNLOADING -> {
				val wrapper = SimpleStringProperty()
				wrapper.bindBidirectional(updateServerProperty.select { it.downloadProgressProperty }, NumberStringConverter("0.00% ${messages["servers.action_info.progress"]}"))
				wrapper
			}
		}
	}
	
	private fun getLabelVisible(status: UpdateServer.UpdateServerStatus?): Boolean {
		return status != UNKNOWN && status != READY && status != SCANNING
	}
	
	companion object {
		
		private val SIZE_SUFFIX = listOf("B", "kB", "MB", "GB", "TB", "PB")
		
		private fun calculateDownloadSize(files: Collection<UpdateServer.RequiredFile>): String {
			var totalSize = files.stream().mapToLong { it.length }.sum().toDouble()
			for ((i, suffix) in SIZE_SUFFIX.withIndex()) {
				if (i != 0)
					totalSize /= 1024.0
				if (totalSize < 1024)
					return String.format("%.2f%s", totalSize, suffix)
			}
			return String.format("%.2f%s", totalSize, SIZE_SUFFIX[SIZE_SUFFIX.size - 1])
		}
	}
	
}
