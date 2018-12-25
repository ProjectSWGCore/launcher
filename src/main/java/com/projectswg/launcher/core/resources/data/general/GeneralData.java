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

import me.joshlarson.jlcommon.javafx.beans.ConcurrentBoolean;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentReference;
import me.joshlarson.jlcommon.javafx.beans.ConcurrentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class GeneralData {
	
	private final ConcurrentBoolean sound;
	private final ConcurrentReference<LauncherTheme> theme;
	private final ConcurrentReference<Locale> locale;
	private final ConcurrentString wine;
	private final ConcurrentBoolean admin;
	
	public GeneralData() {
		this.sound = new ConcurrentBoolean();
		this.theme = new ConcurrentReference<>(LauncherTheme.DEFAULT);
		this.locale = new ConcurrentReference<>(Locale.getDefault());
		this.wine = new ConcurrentString();
		this.admin = new ConcurrentBoolean();
	}
	
	@NotNull
	public ConcurrentBoolean getSoundProperty() {
		return sound;
	}
	
	@NotNull
	public ConcurrentReference<LauncherTheme> getThemeProperty() {
		return theme;
	}
	
	@NotNull
	public ConcurrentReference<Locale> getLocaleProperty() {
		return locale;
	}
	
	@NotNull
	public ConcurrentString getWineProperty() {
		return wine;
	}
	
	@NotNull
	public ConcurrentBoolean getAdminProperty() {
		return admin;
	}
	
	public boolean isSound() {
		return sound.get();
	}
	
	@NotNull
	public LauncherTheme getTheme() {
		return theme.get();
	}
	
	@NotNull
	public Locale getLocale() {
		return locale.get();
	}
	
	@Nullable
	public String getWine() {
		return wine.get();
	}
	
	public boolean isAdmin() {
		return admin.get();
	}
	
	public void setSound(boolean sound) {
		this.sound.set(sound);
	}
	
	public void setTheme(@NotNull LauncherTheme theme) {
		this.theme.set(theme);
	}
	
	public void setLocale(@NotNull Locale locale) {
		this.locale.set(locale);
	}
	
	public void setWine(@Nullable String wine) {
		this.wine.set(wine);
	}
	
	public void setAdmin(boolean admin) {
		this.admin.set(admin);
	}
	
}
