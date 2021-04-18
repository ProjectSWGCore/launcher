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
package com.projectswg.launcher.core

import com.projectswg.launcher.core.resources.data.announcements.WebsitePostFeed
import me.joshlarson.jlcommon.log.Log
import me.joshlarson.jlcommon.log.log_wrapper.ConsoleLogWrapper
import java.io.File

fun main() {
	Log.addWrapper(ConsoleLogWrapper(Log.LogLevel.TRACE))
	WebsitePostFeed.query("https://projectswg.com/posts/feed/") {
		for (message in it.messages) {
			Log.d("Article: %s", message)
		}
	}
}
