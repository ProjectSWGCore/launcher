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
package com.projectswg.launcher.resources.data.general

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.*
import tornadofx.getValue
import tornadofx.setValue

class GeneralData {
	
	val localeProperty = SimpleObjectProperty(Locale.ENGLISH)
	val wineProperty = SimpleStringProperty()
	val adminProperty = SimpleBooleanProperty(false)
	val remoteVersionProperty = SimpleStringProperty("")
	
	val locale: Locale by localeProperty
	var wine: String? by wineProperty
	var isAdmin: Boolean by adminProperty
	var remoteVersion: String by remoteVersionProperty
	
}