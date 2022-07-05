package com.projectswg.launcher.resources.gui.servers

import com.projectswg.launcher.resources.data.LauncherData
import com.projectswg.launcher.resources.gui.style.Style
import com.projectswg.launcher.resources.intents.DownloadLauncherIntent
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.geometry.Pos
import javafx.scene.layout.Region
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class LauncherUpdatePopup : Fragment() {
	
	override val root = vbox {
		alignment = Pos.CENTER
		prefWidth = 200.0
		maxWidth = 200.0
		prefHeight = 100.0
		maxHeight = 100.0
		spacing = 5.0
		padding = insets(5.0)
		
		addClass(Style.popup)
		
		imageview(resources.image("/graphics/large_load_elbow_grease.png")) {
			fitWidth = 200.0
			fitHeight = 200.0
			isPreserveRatio = true
			alignment = Pos.CENTER
		}
		
		label {
			textProperty().bind(LauncherData.INSTANCE.general.remoteVersionProperty.select {
				ReadOnlyStringWrapper("A new version of the launcher is available: $it")
			})
			
			minHeight = Region.USE_PREF_SIZE
			textAlignment = TextAlignment.CENTER
			isWrapText = true
			style {
				fontWeight = FontWeight.BOLD
			}
		}
		
		buttonbar {
			alignment = Pos.CENTER_RIGHT
			button("Ignore") {
				action {
					close()
				}
				
				style {
					backgroundColor += Style.backgroundColorTertiary
				}
			}
			button("Download") {
				action {
					DownloadLauncherIntent().broadcast()
				}
			}
		}
	}
	
}
