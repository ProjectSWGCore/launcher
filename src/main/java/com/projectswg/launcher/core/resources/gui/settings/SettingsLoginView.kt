/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
 * *
 * This file is part of the ProjectSWG Launcher.                                   *
 * *
 * This program is free software: you can redistribute it and/or modify            *
 * it under the terms of the GNU Affero General Public License as published by     *
 * the Free Software Foundation, either version 3 of the License, or               *
 * (at your option) any later version.                                             *
 * *
 * This program is distributed in the hope that it will be useful,                 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 * *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with this program.  If not, see <https:></https:>//www.gnu.org/licenses/>.          *
 * *
 */

package com.projectswg.launcher.core.resources.gui.settings

import com.projectswg.launcher.core.resources.data.LauncherData
import com.projectswg.launcher.core.resources.data.login.LoginServer
import com.projectswg.launcher.core.resources.data.update.UpdateServer
import com.projectswg.launcher.core.resources.gui.createGlyph
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.skin.TextFieldSkin
import javafx.scene.paint.Color
import javafx.util.converter.NumberStringConverter
import tornadofx.View
import tornadofx.select


class SettingsLoginView : View() {
	
	override val root: Parent by fxml()
	private val nameComboBox: ComboBox<LoginServer> by fxid()
	private val addressTextField: TextField by fxid()
	private val portTextField: TextField by fxid()
	private val usernameTextField: TextField by fxid()
	private val passwordField: PasswordField by fxid()
	private val hidePasswordButton: ToggleButton by fxid()
	private val updateServerComboBox: ComboBox<UpdateServer> by fxid()
	private val verifyServerCheckBox: CheckBox by fxid()
	private val enableEncryptionCheckBox: CheckBox by fxid()
	
	init {
		val passwordSkin = PasswordFieldSkin(passwordField, hidePasswordButton.selectedProperty().not())
		passwordField.skin = passwordSkin
		hidePasswordButton.isSelected = false
		hidePasswordButton.graphicProperty().bind(hidePasswordButton.selectedProperty().select { if (it) createGlyph(FontAwesomeIcon.EYE_SLASH) else createGlyph(FontAwesomeIcon.EYE) })
		hidePasswordButton.selectedProperty().addListener { _, _, _ -> passwordField.text = passwordField.text }
		
		// TODO: Add/remove login servers
		addressTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.addressProperty })
		portTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.portProperty }, NumberStringConverter("#"))
		usernameTextField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.usernameProperty })
		passwordField.textProperty().bindBidirectional(nameComboBox.valueProperty().select { it.passwordProperty })
		updateServerComboBox.valueProperty().bindBidirectional(nameComboBox.valueProperty().select { it.updateServerProperty })
		verifyServerCheckBox.selectedProperty().bindBidirectional(nameComboBox.valueProperty().select { it.verifyServerProperty })
		enableEncryptionCheckBox.selectedProperty().bindBidirectional(nameComboBox.valueProperty().select { it.enableEncryptionProperty })
		
		nameComboBox.items = FXCollections.observableArrayList(LauncherData.INSTANCE.login.servers)
		updateServerComboBox.items = FXCollections.observableArrayList(LauncherData.INSTANCE.update.servers)
		
		nameComboBox.value = nameComboBox.items.getOrNull(0)
	}
	
	private fun createGlyph(icon: FontAwesomeIcon): ObjectProperty<Node> {
		val node = icon.createGlyph(fill = Color.BLACK)
		return ReadOnlyObjectWrapper(node)
	}
	
	inner class PasswordFieldSkin(passwordField: PasswordField, private val hiddenProperty: BooleanBinding?) : TextFieldSkin(passwordField) {
		
		override fun maskText(txt: String): String = if (hiddenProperty?.value != false) "\u25CF".repeat(txt.length) else txt
		
	}
	
}
