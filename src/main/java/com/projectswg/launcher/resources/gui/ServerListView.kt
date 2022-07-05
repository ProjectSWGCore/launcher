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

package com.projectswg.launcher.resources.gui

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.data.update.UpdateServer
import com.projectswg.launcher.resources.game.GameInstance
import com.projectswg.launcher.resources.game.ProcessExecutor
import com.projectswg.launcher.resources.gui.events.LauncherNewVersionEvent
import com.projectswg.launcher.resources.gui.servers.LauncherUpdatePopup
import com.projectswg.launcher.resources.gui.servers.WebsitePostFeedList
import com.projectswg.launcher.resources.gui.style.Style
import com.projectswg.launcher.resources.intents.DownloadPatchIntent
import com.projectswg.launcher.resources.intents.GameLaunchedIntent
import com.projectswg.launcher.resources.intents.RequestScanIntent
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import tornadofx.*

class ServerListView : View() {
	
	private val feedList: WebsitePostFeedList by inject()
	
	override val root = vbox {
		subscribe<LauncherNewVersionEvent>(times = 1) {
			find<LauncherUpdatePopup>().openModal(stageStyle = StageStyle.UNDECORATED)
		}
		
		addClass(Style.serverList)
		
		hbox {
			isFillWidth = true
			
			vbox leftBox@ {
				prefWidthProperty().bind(this@hbox.widthProperty().subtract(5).divide(2))
				
				hbox {
					label(messages["servers.login.feed.title"]) {
						addClass(Style.settingsHeaderLabel)
						style {
							padding = box(20.px)
						}
					}
					
					style {
						backgroundColor += Style.backgroundColorSecondary
					}
				}
				
				this += feedList.root
			}
			region {
				prefWidth = 5.0
			}
			vbox rightBox@ {
				prefWidthProperty().bind(this@hbox.widthProperty().subtract(5).divide(2))
				
				hbox {
					label(messages["servers.login.form.title"]) {
						addClass(Style.settingsHeaderLabel)
						style {
							padding = box(20.px)
						}
					}
					
					style {
						backgroundColor += Style.backgroundColorSecondary
					}
				}
				
				// Login container
				form {
					fieldset {
						field("Server:") {
							combobox(LauncherData.INSTANCE.login.activeServerProperty) {
								items = LauncherData.INSTANCE.login.serversProperty
								valueProperty().bindBidirectional(LauncherData.INSTANCE.login.activeServerProperty)
								prefWidth = 300.0
							}
						}
						
						field(messages["servers.login.form.username"]) {
							textfield(LauncherData.INSTANCE.login.activeServerProperty.select { it.authenticationProperty }.select { it.usernameProperty })
						}
						field(messages["servers.login.form.password"]) {
							passwordfield(LauncherData.INSTANCE.login.activeServerProperty.select { it.authenticationProperty }.select { it.passwordProperty })
						}
						field(messages["servers.column.localStatus"]) {
							val updateStatus = LauncherData.INSTANCE.login.activeServerProperty.select { it.updateServerProperty }.select { it.statusProperty }
							
							label(observable=LauncherData.INSTANCE.login.activeServerProperty.select { it.instanceInfo.updateStatusProperty }.select { ReadOnlyStringWrapper(messages[it]) }) {
								maxWidth = Double.POSITIVE_INFINITY
								isFillWidth = true
								
								textFillProperty().bind(updateStatus.select { when (it) {
									UpdateServer.UpdateServerStatus.READY -> ReadOnlyObjectWrapper(Color.rgb(0, 255, 0))
									UpdateServer.UpdateServerStatus.REQUIRES_DOWNLOAD -> ReadOnlyObjectWrapper(Color.RED)
									else -> ReadOnlyObjectWrapper(Color.WHITE)
								} })
								
								style {
									fontWeight = FontWeight.BOLD
								}
							}
							button("Patch") {
								val targetWidthProperty = updateStatus.select { ReadOnlyDoubleWrapper(if (it == UpdateServer.UpdateServerStatus.REQUIRES_DOWNLOAD) 75.0 else 0.0) }
								visibleWhen { updateStatus.select { ReadOnlyBooleanWrapper(it == UpdateServer.UpdateServerStatus.REQUIRES_DOWNLOAD) } }
								minWidthProperty().bind(targetWidthProperty)
								maxWidthProperty().bind(targetWidthProperty)
								
								setOnAction {
									DownloadPatchIntent(LauncherData.INSTANCE.login.activeServer?.updateServer ?: return@setOnAction).broadcast()
								}
							}
							button("Scan") {
								minWidth = 75.0
								setOnAction {
									RequestScanIntent(LauncherData.INSTANCE.login.activeServer?.updateServer ?: return@setOnAction).broadcast()
								}
							}
						}
						progressbar {
							val updateStatus = LauncherData.INSTANCE.login.activeServerProperty.select { it.updateServerProperty }
							visibleWhen { updateStatus.select { it.statusProperty }.select { ReadOnlyBooleanWrapper(it == UpdateServer.UpdateServerStatus.DOWNLOADING) } }
							prefHeight = 12.0
							maxHeight = 12.0
							maxWidth = Double.POSITIVE_INFINITY
							
							progressProperty().bind(updateStatus.select { it.downloadProgressProperty })
							isFillWidth = true
						}
					}
					button("Play") { // TODO: Get proper string for this
						isFillWidth = true
						maxWidth = Double.POSITIVE_INFINITY
						
						setOnAction {
							val activeServer = LauncherData.INSTANCE.login.activeServer ?: return@setOnAction
							val gameInstance = GameInstance(activeServer)
							gameInstance.forwarder.data.username = activeServer.authentication.username
							gameInstance.forwarder.data.password = activeServer.authentication.password
							gameInstance.start()
							GameLaunchedIntent(gameInstance).broadcast()
						}
						
						style {
							fontSize = 15.px
							fontWeight = FontWeight.BOLD
							backgroundColor += Style.playButtonColor
							textFill = Style.playButtonTextColor
						}
					}
				}
				
				region { minHeight = 20.0; maxHeight = 20.0 }
				separator()
				region { minHeight = 5.0; maxHeight = 5.0 }
				
				gridpane {
					this.hgap = 5.0
					this.vgap = 5.0
					paddingRight = 5.0
					
					row {
						button(messages["servers.login.buttons.website"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							setOnAction {
								LauncherData.INSTANCE.application.hostServices.showDocument("https://projectswg.com")
							}
						}
						button(messages["servers.login.buttons.create_account"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							setOnAction {
								// TODO: this, somehow
							}
							
							isDisable = true
						}
					}
					
					row {
						button(messages["servers.login.buttons.configuration"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							setOnAction {
								// TODO add hyperlink
							}
							
							isDisable = true
						}
						button(messages["servers.login.buttons.client_options"]) {
							useMaxWidth = true
							gridpaneColumnConstraints {
								hgrow = Priority.ALWAYS
							}
							setOnAction {
								val updateServer = LauncherData.INSTANCE.login.activeServer?.updateServer ?: return@setOnAction
								ProcessExecutor.INSTANCE.buildProcess(updateServer, "SwgClientSetup_r.exe")
							}
						}
					}
				}
				
				region { vgrow = Priority.ALWAYS }
				
				label("%s: %s".format(messages["servers.login.launcher_version"], LauncherData.VERSION)) {
					paddingRight = 5.0
					
					prefWidthProperty().bind(this@rightBox.widthProperty())
					alignment = Pos.BASELINE_RIGHT
				}
			}
		}
	}
	
}
