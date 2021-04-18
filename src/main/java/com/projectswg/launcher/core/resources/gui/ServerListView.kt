/***********************************************************************************
 * Copyright (C) 2020 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * This file is part of the ProjectSWG Launcher.                                   *
 *                                                                                 *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 *                                                                                 *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.          *
 *                                                                                 *
 ***********************************************************************************/

package com.projectswg.launcher.core.resources.gui

import com.projectswg.launcher.core.resources.gui.style.Style
import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.login.LoginServer
import com.projectswg.launcher.core.resources.gui.servers.ServerPlayCell
import com.projectswg.launcher.core.resources.gui.servers.WebsitePostFeedList
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.layout.Priority
import me.joshlarson.jlcommon.log.Log
import tornadofx.*
import java.util.*

class ServerListView : View() {
	
	private val feedList: WebsitePostFeedList by inject()
	
	override val root = vbox {
		imageview(url = "/graphics/headers/server-table.png") {
			fitWidthProperty().bind(this@vbox.widthProperty())
		}
		tableview<LoginServer> {
			items = LauncherData.INSTANCE.login.serversProperty
			isFocusTraversable = false
			placeholder = label(messages["noServers"])
			setSortPolicy { _ -> Comparator.comparing<LoginServer, String> { it.name }; true }
			
			column(messages["servers.column.name"], valueProvider={cellDataFeatures:TableColumn.CellDataFeatures<LoginServer, String> -> ReadOnlyStringWrapper(cellDataFeatures.value?.name) }).apply {
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
		hbox {
			isFillWidth = true
			
			hboxConstraints {
				this.marginRight = 5.0
			}
			
			vbox leftBox@ {
				// TODO: RSS-based list of new posts
				prefWidthProperty().bind(this@hbox.widthProperty().divide(2).subtract(5))
				
				this += feedList.root
			}
			vbox rightBox@ {
				prefWidthProperty().bind(this@hbox.widthProperty().divide(2).subtract(5))
				
				// Login container
				form {
					val username = SimpleStringProperty()
					val password = SimpleStringProperty()
					fieldset(messages["servers.login.form.title"]) {
						field(messages["servers.login.form.username"]) {
							textfield(username)
						}
						field(messages["servers.login.form.password"]) {
							passwordfield(password)
						}
					}
					button(messages["servers.login.form.submit"]) {
						action {
							Log.i("Logging in with %s / %s", username.get(), password.get())
						}
					}
				}
				
				region { minHeight = 5.0; maxHeight = 5.0 }
				separator()
				region { minHeight = 5.0; maxHeight = 5.0 }
				
				gridpane {
					this.hgap = 5.0
					this.vgap = 5.0
					
					row {
						button(messages["servers.login.buttons.website"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							action {
								// TODO add hyperlink
							}
						}
						button(messages["servers.login.buttons.create_account"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							action {
								// TODO: this, somehow
							}
						}
					}
					
					row {
						button(messages["servers.login.buttons.configuration"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							action {
								// TODO add hyperlink
							}
						}
						button(messages["servers.login.buttons.server_list"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							action {
								// TODO: this, somehow
							}
						}
					}
				}
				
				region { vgrow = Priority.ALWAYS }
				
				label("%s: %s".format(messages["servers.login.launcher_version"], LauncherData.VERSION)) {
					prefWidthProperty().bind(this@rightBox.widthProperty())
					alignment = Pos.BASELINE_RIGHT
				}
			}
		}
	}
	
	companion object {
		
		private const val COL_WIDTH_MEDIUM = 110.0
		private const val COL_WIDTH_LARGE = 150.0
		
	}
	
}
