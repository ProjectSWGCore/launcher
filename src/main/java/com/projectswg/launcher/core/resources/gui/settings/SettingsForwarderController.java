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
import com.projectswg.launcher.core.resources.data.forwarder.ForwarderData;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsForwarderController implements FXMLController {
	
	@FXML
	private Parent root;
	@FXML
	private TextField sendIntervalTextField, sendMaxTextField;
	@FXML
	private Button resetButton;
	
	public SettingsForwarderController() {
		
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sendIntervalTextField.setText(Integer.toString(LauncherData.getInstance().getForwarderData().getSendInterval()));
		sendMaxTextField.setText(Integer.toString(LauncherData.getInstance().getForwarderData().getSendMax()));
		sendIntervalTextField.textProperty().addListener(this::handleInterval);
		sendMaxTextField.textProperty().addListener(this::handleMax);
		
		resetButton.setOnAction(e -> {
			sendIntervalTextField.setText(Integer.toString(ForwarderData.DEFAULT_SEND_INTERVAL));
			sendMaxTextField.setText(Integer.toString(ForwarderData.DEFAULT_SEND_MAX));
		});
	}
	
	private void handleInterval(@SuppressWarnings("unused") ObservableValue<? extends String> prop, @SuppressWarnings("unused") String prev, String next) {
		String filtered = next.replaceAll("\\D*", "");
		if (!filtered.equals(next)) {
			sendIntervalTextField.setText(filtered);
			next = filtered;
		}
		LauncherData.getInstance().getForwarderData().setSendInterval(Integer.parseInt(next));
	}
	
	private void handleMax(@SuppressWarnings("unused") ObservableValue<? extends String> prop, @SuppressWarnings("unused") String prev, String next) {
		String filtered = next.replaceAll("\\D*", "");
		if (!filtered.equals(next)) {
			sendMaxTextField.setText(filtered);
			next = filtered;
		}
		LauncherData.getInstance().getForwarderData().setSendMax(Integer.parseInt(next));
	}
	
}
