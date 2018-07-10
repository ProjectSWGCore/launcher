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

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import me.joshlarson.jlcommon.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Card extends VBox {
	
	private final VBox content;
	private final ImageView headerImage;
	private final Label title;
	private final TextArea description;
	private String link;
	
	public Card() {
		this.content = new VBox();
		this.headerImage = new ImageView();
		this.title = new Label("");
		this.description = new TextArea("");
		this.link = null;
		
		description.setEditable(false);
		
		headerImage.setPreserveRatio(true);
		headerImage.setFitHeight(108);
		headerImage.fitWidthProperty().bind(widthProperty());
		
		title.maxWidthProperty().bind(widthProperty());
		description.maxWidthProperty().bind(widthProperty());
		
		getStyleClass().add("card");
		content.getStyleClass().add("card-content");
		headerImage.getStyleClass().add("header-image");
		title.getStyleClass().add("title");
		description.getStyleClass().add("description");
		
		content.getChildren().addAll(headerImage, title, new Separator(), description);
		getChildren().add(content);
	}
	
	public void setHeaderImage(File image) {
		try {
			this.headerImage.setImage(new Image(new FileInputStream(image)));
		} catch (FileNotFoundException e) {
			Log.e("Failed to set image. File not found: %s", image);
		}
	}
	
	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public void setDescription(String description) {
		this.description.setText(description);
	}
	
	public void setLink(String link) {
		this.link = link;
		
		if (headerImage == null) {
			// In case there's no image, for whatever reason
			return;
		}
		
		// Clicking the image takes you to the link
		Tooltip tooltip = new Tooltip(link);
		Tooltip.install(headerImage, tooltip);
		
		headerImage.setCursor(Cursor.HAND);
		headerImage.setOnMouseClicked(e -> gotoLink());
	}
	
	private void gotoLink() {
		LauncherUI.getInstance().getHostServices().showDocument(link);
	}
	
	private static Region createPaddedRegion() {
		Region region = new Region();
		region.setPrefHeight(10);
		return region;
	}
	
}
