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
import com.projectswg.launcher.core.resources.data.LauncherData;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AnnouncementsController implements FXMLController {
	
	private static final String LISTENER_KEY = "announcements-controller";
	
	@FXML
	private Region root;
	
	@FXML
	private Pane cardContainer;
	
	public AnnouncementsController() {
		
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LauncherData.getInstance().getAnnouncements().getAnnouncementCards().addCollectionChangedListener(LISTENER_KEY, this::updateAnnouncements);
		updateAnnouncements();
	}
	
	private void updateAnnouncements() {
		List<Card> cards = LauncherData.getInstance().getAnnouncements().getAnnouncementCards();
		for (Card card : cards) {
			card.minWidthProperty().bind(cardContainer.widthProperty().subtract(10).divide(2));
			card.maxWidthProperty().bind(cardContainer.widthProperty().subtract(10).divide(2));
			card.minHeightProperty().bind(cardContainer.heightProperty().subtract(10).divide(2));
			card.maxHeightProperty().bind(cardContainer.heightProperty().subtract(10).divide(2));
		}
		cardContainer.getChildren().setAll(cards);
	}
	
}
