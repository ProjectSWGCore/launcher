package com.projectswg.launcher.resources.gui.admin

import com.projectswg.common.network.packets.SWGPacket
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper

data class ForwarderPacket(val client: Boolean, val packet: SWGPacket) {
	val packetName
		get() = packet::class.simpleName
	
	val clientProperty = ReadOnlyBooleanWrapper(client)
	val packetProperty = ReadOnlyObjectWrapper(packet)
	val packetNameProperty = ReadOnlyStringWrapper(packetName)
	
}
