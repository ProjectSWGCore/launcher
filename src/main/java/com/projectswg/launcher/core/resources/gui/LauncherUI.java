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

package com.projectswg.launcher.core.resources.gui;

import com.projectswg.common.javafx.FXMLUtilities;
import com.projectswg.launcher.core.resources.data.LauncherData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LauncherUI extends Application {
	
	private static final AtomicReference<LauncherUI> INSTANCE = new AtomicReference<>(null);
	
	private final AtomicBoolean operational;
	
	public LauncherUI() {
		this.operational = new AtomicBoolean(true);
		INSTANCE.set(this);
	}
	
	public boolean isOperational() {
		return operational.get();
	}
	
	@Override
	public void start(Stage primaryStage) {
		// TODO: Theme specific loading
		LauncherData data = LauncherData.getInstance();
		NavigationController controller = (NavigationController) FXMLUtilities.loadFxmlAsClassResource("/theme/projectswg/fxml/navigation.fxml", data.getGeneral().getLocale());
		if (controller == null) {
			operational.set(false);
			throw new NullPointerException("Invalid navigation controller");
		}
		primaryStage.setTitle("ProjectSWG Launcher");
		primaryStage.setScene(new Scene(controller.getRoot()));
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(e -> Platform.exit());
		primaryStage.show();
	}
	
	@Override
	public void stop() {
		operational.set(false);
	}
	
	public static LauncherUI getInstance() {
		return INSTANCE.get();
	}
	
}
