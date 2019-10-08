package com.projectswg.launcher.core.resources.gui.style

import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class CardStyle : Stylesheet() {
	companion object {
		val cardContainer by cssclass()
		val card by cssclass()
		val cardContent by cssclass()
		val cardImage by cssclass()
		val cardTitle by cssclass()
		val cardDescription by cssclass()
	}
	
	init {
		cardContainer {
			hgap = 10.px
			vgap = 10.px
		}
		card {
			backgroundColor += c("#454545")
			vBarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
			hBarPolicy = ScrollPane.ScrollBarPolicy.NEVER
		}
		cardContent {
			vgap = 5.px
			alignment = Pos.CENTER
		}
		cardImage {
			alignment = Pos.CENTER
		}
		cardTitle {
			fontWeight = FontWeight.BOLD
			textFill = Color.WHITE
			wrapText = true
			fontSize = 14.px
			backgroundColor += c("#313131")
			padding = box(5.px)
		}
		cardDescription {
			fontSize = 12.px
		}
	}
}
