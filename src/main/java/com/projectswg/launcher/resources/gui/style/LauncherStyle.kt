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

package com.projectswg.launcher.resources.gui.style

import javafx.geometry.Pos
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Style : Stylesheet() {
	companion object {
		// Previous color scheme
//		val backgroundColorPrimary = c("#484848")
//		val backgroundColorSecondary = c("#4e4e4e")
//		val backgroundColorTertiary = c("#313131")
//		val textColorPrimary = c("#FFFFFF")
//		val additionalColorPrimary = c("#007fcf")
//		val additionalColorSecondary = c("#7f7f7f")
//		val buttonColor = c("#007fcf")
		
		// New color scheme
		val backgroundColorPrimary = c("#213639")
		val backgroundColorSecondary = c("#294749")
		val backgroundColorTertiary = c("#092729")
		val textColorPrimary = c("#FFFFFF")
		val additionalColorPrimary = c("#ff8819")
		val additionalColorSecondary = c("#6f9c99")
		val buttonColor = c("#ff8819")
		
		val playButtonColor: Color = Color.GREEN
		val playButtonTextColor: Color = textColorPrimary
		
		val statusNormal by cssclass()
		val statusFail by cssclass()
		val statusInProgress by cssclass()
		val statusGood by cssclass()
		
		val selectedTabLabel by cssclass()
		
		// Tables
		val leftTableCell by cssclass()
		val centerTableCell by cssclass()
		
		// Settings
		val settingsHeaderLabel by cssclass()
		val settingsRow by cssclass()
		
		// Server List
		val serverList by cssclass()
		
		val background by cssclass()
		
		val popup by cssclass()
	}
	
	init {
		separator {
			s(line) {
				borderStyle += BorderStrokeStyle.SOLID
				borderWidth += box(3.px, 0.px, 0.px, 0.px)
				borderColor += box(additionalColorPrimary)
			}
		}
		
		scrollPane {
			backgroundColor += Color.TRANSPARENT
		}
		
		selectedTabLabel {
			fontSize = 28.px
			rotate = (-90).deg
		}
		
		// Various arrows
		s(decrementArrow, incrementArrow, arrow) {
			backgroundColor += textColorPrimary
		}
		
		s(filler, track) {
			backgroundColor += backgroundColorTertiary
		}
		
		// ProgressBar, TextField, ListView, and ComboBox inner-component styling
		s(  textField,
			listView,
			progressBar child track,
			comboBox child listCell) {
			
			backgroundColor += backgroundColorTertiary
			backgroundRadius += box(0.px)
			highlightFill = additionalColorPrimary
			textFill = textColorPrimary
		}
		
		// ComboBox, Button, and ScrollBar button styling
		s(  comboBox,
			button,
			scrollBar child decrementButton,
			scrollBar child incrementButton) {
			
			backgroundColor += additionalColorPrimary
			textFill = textColorPrimary
		}
		
		s(scrollBar child thumb) {
			backgroundColor += additionalColorSecondary
			backgroundRadius += box(0.px)
			backgroundInsets += box(0.px)
		}
		
		/* ------------------
		 * ----- Tables -----
		 * ------------------ */
		cell {
			backgroundColor += backgroundColorPrimary
			and(even and filled) {
				backgroundColor += backgroundColorPrimary
//				and(hover) {
//					backgroundColor += backgroundColorHover
//				}
			}
			and(odd and filled) {
				backgroundColor += backgroundColorSecondary
//				and(hover) {
//					backgroundColor += backgroundColorHover
//				}
			}
		}
		
		tableView {
			padding = box(0.px)
		}
		
		tableColumn {
			fontWeight = FontWeight.BOLD
			borderWidth += box(0.px)
		}
		
		columnHeader {
			backgroundColor += backgroundColorTertiary
		}
		
		columnHeaderBackground {
			borderColor += box(additionalColorPrimary)
			borderWidth += box(0.px, 0.px, 3.px, 0.px)
		}
		
		leftTableCell {
			alignment = Pos.CENTER_LEFT
		}
		
		centerTableCell {
			alignment = Pos.CENTER
		}
		
		// OTHER
		s(text, label, content) {
			fill = textColorPrimary
			textFill = textColorPrimary
		}
		
		// Buttons
		s(comboBox, button, decrementButton, incrementButton) {
			textFill = textColorPrimary
			backgroundColor += buttonColor
			backgroundInsets += box(0.px)
			backgroundRadius += box(0.px)
		}
		
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
		
		settingsHeaderLabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			padding = box(5.px, 0.px, 10.px, 5.px)
		}
		
		settingsRow {
			prefHeight = 25.px
			padding = box(5.px, 5.px, 5.px, 50.px)
			hgap = 5.px
			
			s(button) {
				prefWidth = 150.px
			}
			
			s(label) {
				prefWidth = 150.px
				prefHeight= 25.px
			}
			
			s(checkBox) {
				prefHeight = 25.px
			}
			
			s(comboBox, textField) {
				prefWidth = 400.px
				prefHeight = 20.px
			}
		}
		
		// Server List
		serverList {
//			padding = box(5.px)
		}
		
		background {
			backgroundColor += backgroundColorPrimary
		}
		
		s(".tab-header-background") {
			backgroundColor += backgroundColorPrimary
		}
		
		s(tabContentArea) {
			backgroundColor += backgroundColorPrimary
		}
		
		tab {
			backgroundColor += backgroundColorSecondary
			
			and(selected) {
				backgroundColor += backgroundColorTertiary
			}
		}
		
		s(focusIndicator) {
			borderColor += box(Color.TRANSPARENT)
		}
		
		popup {
			backgroundColor += backgroundColorPrimary
			borderWidth += box(5.px)
			borderColor += box(Color.WHITE)
			
			s(label) {
				fill = Color.WHITE
			}
		}
	}
	
}
