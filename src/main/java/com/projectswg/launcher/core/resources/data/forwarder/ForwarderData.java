/***********************************************************************************
 * Copyright (C) 2018 /// Project SWG /// www.projectswg.com                       *
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

package com.projectswg.launcher.core.resources.data.forwarder;

import me.joshlarson.jlcommon.concurrency.beans.ConcurrentInteger;
import org.jetbrains.annotations.NotNull;

public class ForwarderData {
	
	public static final int DEFAULT_SEND_INTERVAL = 20;
	public static final int DEFAULT_SEND_MAX = 100;
	
	private final ConcurrentInteger sendInterval;
	private final ConcurrentInteger sendMax;
	
	public ForwarderData() {
		this.sendInterval = new ConcurrentInteger(DEFAULT_SEND_INTERVAL);
		this.sendMax = new ConcurrentInteger(DEFAULT_SEND_MAX);
	}
	
	@NotNull
	public ConcurrentInteger getSendIntervalProperty() {
		return sendInterval;
	}
	
	@NotNull
	public ConcurrentInteger getSendMaxProperty() {
		return sendMax;
	}
	
	public int getSendInterval() {
		return sendInterval.getValue();
	}
	
	public int getSendMax() {
		return sendMax.getValue();
	}
	
	public void setSendInterval(int sendInterval) {
		this.sendInterval.set(sendInterval);
	}
	
	public void setSendMax(int sendMax) {
		this.sendMax.set(sendMax);
	}
	
}
