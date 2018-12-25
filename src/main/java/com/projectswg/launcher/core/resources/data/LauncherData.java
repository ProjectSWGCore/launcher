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

package com.projectswg.launcher.core.resources.data;

import com.projectswg.launcher.core.Launcher;
import com.projectswg.launcher.core.resources.data.announcements.AnnouncementsData;
import com.projectswg.launcher.core.resources.data.forwarder.ForwarderData;
import com.projectswg.launcher.core.resources.data.general.GeneralData;
import com.projectswg.launcher.core.resources.data.login.LoginData;
import com.projectswg.launcher.core.resources.data.update.UpdateData;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

public enum LauncherData {
	INSTANCE;
	
	public static final String VERSION = "1.1.13";
	public static final String UPDATE_ADDRESS = "login1.projectswg.com";
	
	private final AtomicReference<Application> application;
	private final AtomicReference<Stage> stage;
	private final AnnouncementsData announcementsData;
	private final GeneralData generalData;
	private final LoginData loginData;
	private final UpdateData updateData;
	private final ForwarderData forwarderData;
	
	LauncherData() {
		this.application = new AtomicReference<>(null);
		this.stage = new AtomicReference<>(null);
		this.announcementsData = new AnnouncementsData();
		this.generalData = new GeneralData();
		this.loginData = new LoginData();
		this.updateData = new UpdateData();
		this.forwarderData = new ForwarderData();
	}
	
	public Preferences getPreferences() {
		return Preferences.userNodeForPackage(Launcher.class);
	}
	
	public Application getApplication() {
		return application.get();
	}
	
	public Stage getStage() {
		return stage.get();
	}
	
	public AnnouncementsData getAnnouncements() {
		return announcementsData;
	}
	
	public GeneralData getGeneral() {
		return generalData;
	}
	
	public LoginData getLogin() {
		return loginData;
	}
	
	public UpdateData getUpdate() {
		return updateData;
	}
	
	public ForwarderData getForwarderData() {
		return forwarderData;
	}
	
	public void setApplication(Application application) {
		this.application.set(application);
	}
	
	public void setStage(Stage stage) {
		this.stage.set(stage);
	}
	
	public static LauncherData getInstance() {
		return INSTANCE;
	}
	
}
