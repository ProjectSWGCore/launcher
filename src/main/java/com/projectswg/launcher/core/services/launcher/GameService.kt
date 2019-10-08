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

package com.projectswg.launcher.core.services.launcher

import com.projectswg.launcher.core.resources.game.GameInstance
import com.projectswg.launcher.core.resources.intents.GameLaunchedIntent
import me.joshlarson.jlcommon.control.IntentHandler
import me.joshlarson.jlcommon.control.Service
import me.joshlarson.jlcommon.log.Log
import java.util.concurrent.CopyOnWriteArrayList

class GameService : Service() {
	
	private val instances = CopyOnWriteArrayList<GameInstance>()
	
	override fun stop(): Boolean {
		for (instance in instances) {
			instance.stop()
		}
		instances.clear()
		return true
	}
	
	@IntentHandler
	private fun handleGameLaunchedIntent(gli: GameLaunchedIntent) {
		Log.i("Game Instance Launched")
		instances.add(gli.gameInstance)
	}
	
}
