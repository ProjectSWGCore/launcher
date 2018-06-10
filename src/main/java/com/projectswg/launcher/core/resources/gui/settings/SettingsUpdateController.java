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
import com.projectswg.launcher.core.resources.data.update.UpdateServer;
import com.projectswg.launcher.core.resources.game.ProcessExecutor;
import com.projectswg.launcher.core.resources.intents.RequestScanIntent;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SettingsUpdateController implements FXMLController {
	
	private final AtomicReference<UpdateServer> server;
	
	@FXML
	private Parent root;
	@FXML
	private ComboBox<UpdateServer> nameComboBox;
	@FXML
	private TextField addressTextField, portTextField, basePathTextField, localPathTextField;
	@FXML
	private Button scanButton, clientOptionsButton, localPathSelectionButton;
	
	public SettingsUpdateController() {
		this.server = new AtomicReference<>(null);
	}
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		localPathSelectionButton.setGraphic(createFolderGlyph());
		
		// TODO: Add/remove login servers
		scanButton.setOnAction(this::processScanButtonAction);
		clientOptionsButton.setOnAction(this::processClientOptionsButtonAction);
		addressTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setAddress(t)));
		portTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setPort(Integer.parseInt(t))));
		basePathTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setBasePath(t)));
		localPathTextField.textProperty().addListener((obs, prev, t) -> setIfPresent(s -> s.setLocalPath(t)));
		localPathSelectionButton.setOnAction(this::processLocalPathSelectionButtonAction);
		
		nameComboBox.valueProperty().addListener((obs, prev, next) -> { server.set(next); updateFields(next); });
		nameComboBox.setItems(FXCollections.observableArrayList(LauncherData.getInstance().getUpdate().getServers()));
		
		UpdateServer def = nameComboBox.getItems().get(0);
		updateFields(def);
		nameComboBox.setValue(def);
	}
	
	private void setIfPresent(Consumer<UpdateServer> c) {
		UpdateServer s = server.get();
		if (s != null)
			c.accept(s);
	}
	
	private void updateFields(UpdateServer server) {
		addressTextField.setText(server.getAddress());
		portTextField.setText(Integer.toString(server.getPort()));
		basePathTextField.setText(server.getBasePath());
		localPathTextField.setText(server.getLocalPath());
	}
	
	private static FontAwesomeIconView createFolderGlyph() {
		FontAwesomeIconView view = new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT);
		view.setGlyphSize(16);
		return view;
	}
	
	private void processScanButtonAction(ActionEvent e) {
		UpdateServer server = this.server.get();
		if (server != null)
			RequestScanIntent.broadcast(server);
	}
	
	private void processClientOptionsButtonAction(ActionEvent e) {
		UpdateServer server = this.server.get();
		if (server != null)
			ProcessExecutor.INSTANCE.buildProcess(server, "SwgClientSetup_r.exe");
	}
	
	private void processLocalPathSelectionButtonAction(ActionEvent e) {
		File selection = chooseOpenDirectory("Choose Local Installation Path", getCurrentDirectory());
		if (selection == null)
			return;
		try {
			localPathTextField.setText(selection.getCanonicalPath());
		} catch (IOException ex) {
			localPathTextField.setText(selection.getAbsolutePath());
		}
		UpdateServer server = this.server.get();
		if (server != null)
			RequestScanIntent.broadcast(server);
	}
	
	private File getCurrentDirectory() {
		UpdateServer server = this.server.get();
		if (server == null)
			return new File(".");
		String localPathString = server.getLocalPath();
		if (localPathString.isEmpty())
			return new File(".");
		File localPath = new File(localPathString);
		if (!localPath.isDirectory())
			return new File(".");
		return localPath;
	}
	
	private static File chooseOpenDirectory(String title, File currentDirectory) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		directoryChooser.setInitialDirectory(currentDirectory);
		File file = directoryChooser.showDialog(LauncherData.getInstance().getStage());
		if (file == null || !file.isDirectory())
			return null;
		return file;
	}
	
}
