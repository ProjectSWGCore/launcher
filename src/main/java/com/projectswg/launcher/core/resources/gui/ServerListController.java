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
import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.gui.servers.ServerPlayCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import me.joshlarson.jlcommon.concurrency.beans.ConcurrentBase;
import me.joshlarson.jlcommon.concurrency.beans.ConcurrentString;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ServerListController implements FXMLController {
	
	private static final String LISTENER_KEY = "server-list-controller";
	private static final double COL_WIDTH_MEDIUM = 110;
	private static final double COL_WIDTH_LARGE = 150;
	
	@FXML
	private Region root;
	
	@FXML
	private ImageView headerImage;
	
	@FXML
	private TableView<LoginServer> serverTable;
	
	@FXML
	private CardContainer cardContainer;
	
	public ServerListController() {
		
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		headerImage.fitWidthProperty().bind(root.widthProperty());
		addCenterAlignColumn(resources.getString("servers.column.name"), COL_WIDTH_LARGE, t->t, s -> new ConcurrentString(s.getName()));
		addCenterAlignColumn(resources.getString("servers.column.gameVersion"), COL_WIDTH_MEDIUM, t->t, s -> new ConcurrentString(s.getUpdateServer().getGameVersion()));
		addCenterAlignColumn(resources.getString("servers.column.remoteStatus"), COL_WIDTH_LARGE, t->t, s -> s.getInstanceInfo().getLoginStatusProperty());
		addCenterAlignColumn(resources.getString("servers.column.localStatus"), COL_WIDTH_LARGE, resources::getString, s -> s.getInstanceInfo().getUpdateStatusProperty());
		addPlayColumn(resources);
		
		LauncherData.getInstance().getLogin().getServers().addCollectionChangedListener(LISTENER_KEY, this::updateServerTable);
		LauncherData.getInstance().getAnnouncements().getServerListCards().addCollectionChangedListener(LISTENER_KEY, this::updateAnnouncements);
		updateServerTable();
		updateAnnouncements();
	}
	
	private void updateServerTable() {
		serverTable.getItems().setAll(LauncherData.getInstance().getLogin().getServers());
		serverTable.getItems().sort(Comparator.comparing(LoginServer::getName));
	}
	
	private void updateAnnouncements() {
		cardContainer.clearCards();
		LauncherData.getInstance().getAnnouncements().getServerListCards().forEach(cardContainer::addCard);
	}
	
	private <S, T> void addCenterAlignColumn(String name, double prefWidth, Function<S, T> conv, Function<LoginServer, ConcurrentBase<S>> transform) {
		TableColumn<LoginServer, T> col = addColumn(name, prefWidth, conv, transform);
		col.getStyleClass().add("center-table-cell");
	}
	
	private <S, T> TableColumn<LoginServer, T> addColumn(String name, double prefWidth, Function<S, T> conv, Function<LoginServer, ConcurrentBase<S>> transform) {
		TableColumn<LoginServer, T> col = new TableColumn<>(name);
		col.setPrefWidth(prefWidth);
		col.setCellValueFactory(param -> {
			ConcurrentBase<S> val = transform.apply(param.getValue());
			SimpleObjectProperty<T> obj = new SimpleObjectProperty<>(conv.apply(val.get()));
			val.addTransformListener(LISTENER_KEY, conv, obj::set);
			return obj;
		});
		serverTable.getColumns().add(col);
		return col;
	}
	
	private void addPlayColumn(ResourceBundle resources) {
		TableColumn<LoginServer, LoginServer> col = new TableColumn<>(resources.getString("servers.column.play"));
		col.setCellFactory(param -> new ServerPlayCell(resources));
		col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
		col.getStyleClass().add("center-table-cell");
		col.setPrefWidth(COL_WIDTH_LARGE);
		serverTable.getColumns().add(col);
	}
	
}
