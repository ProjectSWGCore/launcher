package com.projectswg.launcher.core.resources.data.announcements

import java.util.Date

data class WebsitePostMessage(var title: String, val author: String, val date: Date, val link: String, val description: String, val image: WebsitePostMessageImage)
