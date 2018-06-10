/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
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

package com.projectswg.launcher.core.resources.data.general;

import java.util.HashMap;
import java.util.Map;

public enum LauncherTheme {
	DEFAULT	("projectswg");
	
	private static final Map<String, LauncherTheme> TAG_TO_THEME = new HashMap<>();
	
	static {
		for (LauncherTheme theme : values()) {
			TAG_TO_THEME.put(theme.primaryTag, theme);
			for (String tag : theme.tags)
				TAG_TO_THEME.put(tag, theme);
		}
	}
	
	private final String [] tags;
	private final String primaryTag;
	
	LauncherTheme(String primaryTag, String ... tags) {
		this.tags = tags;
		this.primaryTag = primaryTag;
	}
	
	public String getTag() {
		return primaryTag;
	}
	
	public static LauncherTheme forThemeTag(String tag) {
		return TAG_TO_THEME.getOrDefault(tag, LauncherTheme.DEFAULT);
	}
}
