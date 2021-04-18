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

package com.projectswg.launcher.core.resources.data.announcements

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import me.joshlarson.jlcommon.log.Log
import org.apache.commons.text.StringEscapeUtils
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.util.*


class WebsitePostFeed(feed: SyndFeed) {
	
	val messages = feed.entries.map {
		WebsitePostMessage(
			title = it.title,
			author = it.author,
			date = it.publishedDate,
			link = it.link,
			description = StringEscapeUtils.unescapeXml(it.description.value),
			image = when(it.categories.getOrNull(0)?.name?.toLowerCase(Locale.US)) {
				"development update", "quality assurance" -> WebsitePostMessageImage.DEVELOPMENT
				"community update" -> WebsitePostMessageImage.COMMUNITY
				"holocore update" -> WebsitePostMessageImage.HOLOCORE
				else -> {
					Log.w("Unknown RSS category type: '%s'", it.categories.getOrNull(0)?.name?.toLowerCase(Locale.US))
					WebsitePostMessageImage.OPERATIONS
				}
			}
		)
	}
	
	companion object {
		
		fun query(url: String, callback: (WebsitePostFeed) -> Unit) {
			Log.t("Requesting updated announcement list from %s", url)
			val client = HttpClient.newHttpClient()
			val request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build()
			client.sendAsync(request, BodyHandlers.ofString())
				.thenApply { obj: HttpResponse<String?> -> obj.body() }
				.thenAccept { callback(parse(it)) }
				.join()
		}
		
		fun parse(feedStr: String?): WebsitePostFeed {
			return WebsitePostFeed(SyndFeedInput().build(XmlReader((feedStr ?: "").byteInputStream(StandardCharsets.UTF_8))))
		}
		
	}
	
}
