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

package com.projectswg.launcher.core.resources.gui.settings;

import com.projectswg.common.javafx.FXMLController;
import com.projectswg.launcher.core.resources.data.LauncherData;
import com.projectswg.launcher.core.resources.data.login.LoginServer;
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SettingsLoginController implements FXMLController {
	
	private final AtomicReference<LoginServer> server;
	
	@FXML
	private Parent root;
	@FXML
	private ComboBox<LoginServer> nameComboBox;
	@FXML
	private TextField addressTextField, portTextField, usernameTextField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private ComboBox<UpdateServer> updateServerComboBox;
	
	public SettingsLoginController() {
		this.server = new AtomicReference<>(null);
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO: Add/remove login servers
		addressTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setAddress(t)));
		portTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setPort(Integer.parseInt(t))));
		usernameTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setUsername(t)));
		passwordField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setPassword(t)));
		updateServerComboBox.valueProperty().addListener((obs, prev, v) -> setIfPresent(s -> s.setUpdateServer(v)));
		
		nameComboBox.valueProperty().addListener((obs, prev, next) -> { server.set(next); updateFields(next); });
		nameComboBox.setItems(FXCollections.observableArrayList(LauncherData.getInstance().getLogin().getServers()));
		updateServerComboBox.setItems(FXCollections.observableArrayList(LauncherData.getInstance().getUpdate().getServers()));
		
		LoginServer def = nameComboBox.getItems().get(0);
		updateFields(def);
		nameComboBox.setValue(def);
	}
	
	private void updateFields(LoginServer server) {
		addressTextField.setText(server.getAddress());
		portTextField.setText(Integer.toString(server.getPort()));
		usernameTextField.setText(server.getUsername());
		passwordField.setText(server.getPassword());
		updateServerComboBox.setValue(server.getUpdateServer());
	}
	
	private void setIfPresent(Consumer<LoginServer> c) {
		LoginServer s = server.get();
		if (s != null)
			c.accept(s);
	}
	
}
