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

package com.projectswg.launcher.core;

import com.projectswg.common.javafx.ResourceUtilities;
import com.projectswg.common.utilities.LocalUtilities;
import com.projectswg.launcher.core.services.data.DataManager;
import com.projectswg.launcher.core.services.launcher.LauncherManager;
import me.joshlarson.jlcommon.control.IntentManager;
import me.joshlarson.jlcommon.control.Manager;
import me.joshlarson.jlcommon.control.SafeMain;
import me.joshlarson.jlcommon.control.ServiceBase;
import me.joshlarson.jlcommon.log.Log;
import me.joshlarson.jlcommon.log.log_wrapper.ConsoleLogWrapper;
import me.joshlarson.jlcommon.log.log_wrapper.FileLogWrapper;
import me.joshlarson.jlcommon.utilities.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Launcher {
	
	private final List<ServiceBase> services;
	
	private Launcher() {
		this.services = new ArrayList<>();
	}
	
	private void run() {
		IntentManager intentManager = new IntentManager(Runtime.getRuntime().availableProcessors());
		intentManager.initialize();
		IntentManager.setInstance(intentManager);
		services.clear();
		services.add(new DataManager());
		services.add(new LauncherManager());
		for (ServiceBase s : services)
			s.setIntentManager(intentManager);
		Manager.start(services);
		Manager.run(services, 100);
		Collections.reverse(services); // Allows the data services to stay alive longer
		Manager.stop(services);
		intentManager.terminate();
		IntentManager.setInstance(null);
		ThreadUtilities.printActiveThreads();
	}
	
	public static void main(String [] args) {
		LocalUtilities.setApplicationName(".projectswg/launcher");
		ResourceUtilities.setPrimarySource(Launcher.class);
		Log.addWrapper(new ConsoleLogWrapper());
		Log.addWrapper(new FileLogWrapper(new File(LocalUtilities.getApplicationDirectory(), "log.txt")));
		
		SafeMain.main("launcher", new Launcher()::run);
		// No code can run after this point - SafeMain calls System.exit
	}
	
}
