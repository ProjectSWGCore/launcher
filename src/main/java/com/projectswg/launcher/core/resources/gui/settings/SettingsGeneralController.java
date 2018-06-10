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
import com.projectswg.launcher.core.resources.data.general.GeneralData;
import com.projectswg.launcher.core.resources.data.general.LauncherTheme;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsGeneralController implements FXMLController {
	
	@FXML
	private Parent root;
	@FXML
	private CheckBox soundCheckbox;
	@FXML
	private ComboBox<LauncherTheme> themeComboBox;
	@FXML
	private ComboBox<Locale> localeComboBox;
	@FXML
	private TextField wineTextField;
	@FXML
	private Button wineSelectionButton;
	
	@Override
	public Parent getRoot() {
		return root;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GeneralData data = LauncherData.getInstance().getGeneral();
		
		wineSelectionButton.setGraphic(createFolderGlyph());
		
		soundCheckbox.selectedProperty().addListener((obs, prev, s) -> data.setSound(s));
		themeComboBox.valueProperty().addListener((obs, prev, v) -> data.setTheme(v));
		localeComboBox.valueProperty().addListener((obs, prev, v) -> data.setLocale(v));
		wineTextField.textProperty().addListener((obs, prev, t) -> data.setWine(t));
		wineSelectionButton.setOnAction(this::processWineSelectionButtonAction);
		
		themeComboBox.getItems().setAll(LauncherTheme.values());
		localeComboBox.getItems().setAll(Locale.ENGLISH, Locale.GERMAN);
		
		soundCheckbox.setSelected(data.isSound());
		themeComboBox.setValue(data.getTheme());
		localeComboBox.setValue(data.getLocale());
		wineTextField.setText(data.getWine());
	}
	
	private static FontAwesomeIconView createFolderGlyph() {
		FontAwesomeIconView view = new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT);
		view.setGlyphSize(16);
		return view;
	}
	
	private void processWineSelectionButtonAction(ActionEvent e) {
		File selection = chooseOpenFile("Choose Wine Path");
		if (selection == null)
			return;
		try {
			wineTextField.setText(selection.getCanonicalPath());
		} catch (IOException ex) {
			wineTextField.setText(selection.getAbsolutePath());
		}
	}
	
	private static File chooseOpenFile(String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		File file = fileChooser.showOpenDialog(LauncherData.getInstance().getStage());
		if (file == null || !file.isFile())
			return null;
		return file;
	}
	
}
