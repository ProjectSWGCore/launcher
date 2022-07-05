package com.projectswg.launcher.resources.intents

import com.projectswg.launcher.resources.game.GameInstance
import me.joshlarson.jlcommon.control.Intent

data class GameLaunchedIntent(val gameInstance: GameInstance): Intent()
