package com.projectswg.launcher.core.resources.gui.style

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.c
import tornadofx.cssclass

class Style : Stylesheet() {
	companion object {
		val statusNormal by cssclass()
		val statusFail by cssclass()
		val statusInProgress by cssclass()
		val statusGood by cssclass()
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
	}
}
