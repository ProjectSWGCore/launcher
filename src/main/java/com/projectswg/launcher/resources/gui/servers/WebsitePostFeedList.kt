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

package com.projectswg.launcher.resources.gui.servers

import com.projectswg.launcher.resources.data.announcements.WebsitePostFeed
import com.projectswg.launcher.resources.data.announcements.WebsitePostMessage
import com.projectswg.launcher.resources.gui.events.LauncherClosingEvent
import com.projectswg.launcher.resources.gui.style.Style
import javafx.scene.text.FontWeight
import me.joshlarson.jlcommon.concurrency.ScheduledThreadPool
import me.joshlarson.jlcommon.log.Log
import tornadofx.*
import java.text.SimpleDateFormat
import kotlin.math.min

class WebsitePostFeedList : View() {
	
	private val updateThread = ScheduledThreadPool(1, "website-post-feed-list")
	private val postDateFormat = SimpleDateFormat("dd MMM yyyy")
	
	override val root = listview<WebsitePostMessage> {
		addClass(Style.background)
		cellFormat {
			val item = this@cellFormat.item
			
			tooltip = tooltip {
				text = item.description
				isWrapText = true
				
				this.maxWidth = 300.0
			}
			
			onDoubleClick {
				com.projectswg.launcher.resources.data.LauncherData.INSTANCE.application.hostServices.showDocument(item.link)
			}
			
			graphic = hbox {
				maxHeight = 40.0
				spacing = 10.0
				
				vbox {
					label(item.title) {
						style {
							fontSize = 12.px
							fontWeight = FontWeight.BOLD
						}
					}
					label("    " + item.image.descriptionStr + " [" + postDateFormat.format(item.date) + "]") {
						style {
							fontSize = 10.px
						}
					}
					label("    " + item.author) {
						style {
							fontSize = 10.px
						}
					}
				}
			}
		}
	}
	
	override fun onDock() {
		super.onDock()
		updateThread.start()
		updateThread.executeWithFixedDelay(0, 1000 * 60 * 60) {
			WebsitePostFeed.query("https://projectswg.com/posts/feed/") { feed ->
				Log.d("Received updated RSS feed with %d items", feed.messages.size)
				runLater {
					root.items = feed.messages.sortedByDescending { it.date }.subList(0, min(10, feed.messages.size)).asObservable()
					root.refresh()
				}
			}
		}
		
		subscribe<LauncherClosingEvent> {
			updateThread.stop()
		}
	}
	
}
