package com.projectswg.launcher.core.resources.intents

import com.projectswg.launcher.core.resources.game.GameInstance
import me.joshlarson.jlcommon.control.Intent

data class GameLaunchedIntent(val gameInstance: GameInstance): Intent()
