/***********************************************************************************
 * Copyright (C) 2020 /// Project SWG /// www.projectswg.com                       *
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

package com.projectswg.launcher.core.resources.gui.style

import javafx.scene.paint.Color
import tornadofx.*

class Style : Stylesheet() {
	companion object {
		val statusNormal by cssclass()
		val statusFail by cssclass()
		val statusInProgress by cssclass()
		val statusGood by cssclass()
		
		val background by cssclass()
	}
	
	init {
		statusNormal {
			s(text) {
				fill = Color.WHITE
			}
		}
		statusFail {
			s(text) {
				fill = c("#FF0000")
			}
		}
		statusInProgress {
			s(text) {
				fill = Color.YELLOW
			}
		}
		statusGood {
			s(text) {
				fill = c("#00FF00")
			}
		}
		background {
			cell {
				and(even) {
					backgroundColor += c("#4e4e4e")
					and(hover) {
						backgroundColor += c("#404040")
					}
				}
				and(odd) {
					backgroundColor += c("#484848")
					and(hover) {
						backgroundColor += c("#404040")
					}
				}
			}
		}
	}
}
