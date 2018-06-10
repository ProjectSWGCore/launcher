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

package com.projectswg.launcher.core.resources.gui.servers;

import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import me.joshlarson.jlcommon.concurrency.beans.ConcurrentDouble;

import java.util.ResourceBundle;

public class ServerPlayCell extends TableCell<LoginServer, LoginServer> {
	
	private final ServerPlayButton button;
	private final ServerPlayLabel label;
	private final VBox cellContents;
	
	public ServerPlayCell(ResourceBundle resources) {
		ConcurrentDouble progressBar = new ConcurrentDouble(-1);
		this.button = new ServerPlayButton(resources, progressBar);
		this.label = new ServerPlayLabel(resources, progressBar);
		this.cellContents = new VBox(button, label);
		cellContents.getStyleClass().add("server-play-cell");
	}
	
	@Override
	protected void updateItem(LoginServer item, boolean empty) {
		LoginServer previousLoginServer = getItem();
		if (previousLoginServer != null) {
			previousLoginServer.getUpdateServerProperty().removeListener(this);
		}
		super.updateItem(item, empty);
		if (item != null) {
			item.getUpdateServerProperty().addListener(this, updateServer -> update(item, updateServer));
		}
		update(item, item==null?null:item.getUpdateServer());
		if (empty) {
			setGraphic(null);
		} else {
			setGraphic(cellContents);
		}
		setText(null);
	}
	
	private void update(LoginServer loginServer, UpdateServer updateServer) {
		button.setLoginServer(loginServer);
		button.setUpdateServer(updateServer);
		label.setUpdateServer(updateServer);
	}
	
}
