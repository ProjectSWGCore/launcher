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

import com.projectswg.common.javafx.FXMLController;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class NavigationController implements FXMLController {
	
	@FXML
	private TabPane tabPane;
	@FXML
	public Tab announcementsTab, serverListTab, settingsTab;
	@FXML
	private Label selectedTabLabel;
	@FXML
	private Parent root;
	
	public NavigationController() {
		
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
	}
	
	private static FontAwesomeIconView createGlyph(FontAwesomeIcon icon) {
		FontAwesomeIconView view = new FontAwesomeIconView(icon);
		view.setGlyphSize(24);
		view.setFill(Color.LIGHTGRAY);
		return view;
	}
	
}
