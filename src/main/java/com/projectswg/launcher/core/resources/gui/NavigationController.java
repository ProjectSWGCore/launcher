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

import com.projectswg.launcher.core.resources.data.LauncherData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.joshlarson.jlcommon.javafx.control.FXMLController;
import me.joshlarson.jlcommon.javafx.control.FXMLService;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class NavigationController extends FXMLService implements FXMLController {
	
	@FXML
	private TabPane tabPane;
	@FXML
	public Tab announcementsTab, serverListTab, settingsTab;
	@FXML
	private Label selectedTabLabel;
	@FXML
	private Parent root;
	
	private Stage primaryStage;
	
	public NavigationController() {
		this.primaryStage = LauncherData.INSTANCE.getStage();
		LauncherData.INSTANCE.getGeneral().getLocaleProperty().addSimpleListener("navigation-reinflate", this::updateLocale);
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		announcementsTab.setGraphic(createGlyph(FontAwesomeIcon.NEWSPAPER_ALT));
		serverListTab.setGraphic(createGlyph(FontAwesomeIcon.SERVER));
		settingsTab.setGraphic(createGlyph(FontAwesomeIcon.SLIDERS));
		
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, prev, tab) -> selectedTabLabel.setText(tab.getText()));
		tabPane.getSelectionModel().select(serverListTab);
		
		setupStage();
	}
	
	@Override
	public boolean stop() {
		LauncherData.INSTANCE.getGeneral().getLocaleProperty().removeListener("navigation-reinflate");
		this.primaryStage = null;
		return true;
	}
	
	@Override
	public boolean isOperational() {
		return primaryStage == null || primaryStage.isShowing();
	}
	
	private void setupStage() {
		if (this.primaryStage == null) {
			Stage primaryStage = new Stage();
			primaryStage.setTitle("ProjectSWG Launcher");
			primaryStage.setScene(new Scene(root));
			primaryStage.setResizable(false);
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/graphics/ProjectSWG.png")));
			primaryStage.show();
			LauncherData.INSTANCE.setApplication(getApplication());
			LauncherData.INSTANCE.setStage(primaryStage);
			this.primaryStage = primaryStage;
		} else {
			this.primaryStage.setScene(new Scene(root));
		}
	}
	
	private void updateLocale(Locale locale) {
		new Thread(() -> reinflate(locale), "reinflator").start();
	}
	
	private static FontAwesomeIconView createGlyph(FontAwesomeIcon icon) {
		FontAwesomeIconView view = new FontAwesomeIconView(icon);
		view.setGlyphSize(24);
		view.setFill(Color.LIGHTGRAY);
		return view;
	}
	
}
