package com.projectswg.launcher.core.resources.gui

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.scene.paint.Color

fun FontAwesomeIcon.createGlyph(glyphSize: Int = 16, fill: Color = Color.WHITE): FontAwesomeIconView {
	val view = FontAwesomeIconView(this)
	view.glyphSize = glyphSize
	view.fill = fill
	return view
}
