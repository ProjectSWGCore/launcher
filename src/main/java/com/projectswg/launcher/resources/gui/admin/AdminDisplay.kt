package com.projectswg.launcher.resources.gui.admin

import com.projectswg.common.network.NetBuffer
import com.projectswg.common.network.packets.PacketType
import com.projectswg.common.network.packets.SWGPacket
import com.projectswg.common.network.packets.swg.zone.object_controller.ObjectController
import com.projectswg.forwarder.Forwarder
import com.projectswg.forwarder.intents.DataPacketInboundIntent
import com.projectswg.forwarder.intents.DataPacketOutboundIntent
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority
import tornadofx.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class AdminDisplay: Fragment("Admin Panel") {
	
	val forwarder: Forwarder by param()
	private val packets: ObservableList<ForwarderPacket> = FXCollections.observableArrayList()
	
	override val root = vbox {
		prefWidth = 300.0
		prefHeight = 250.0
		
		tableview(packets) {
			vgrow = Priority.ALWAYS
			
//			column("Sender", String::class) {
//				setCellValueFactory {
//					if (it.value.client) ReadOnlyStringWrapper("Client") else ReadOnlyStringWrapper("Server")
//				}
//			}
			column("Packet", ForwarderPacket::packetNameProperty)
			
			rowExpander(expandOnDoubleClick = false) {
				tableview(FXCollections.observableArrayList(it.packet::class.memberProperties.toList())) {
					vgrow = Priority.ALWAYS
					readonlyColumn("Name", KProperty1<out SWGPacket, *>::name)
					onDoubleClick {
						println("$padding  $layoutX-$layoutY  $width  $height")
					}
				}
			}
		}
	}
	
	init {
		forwarder.intentManager.registerForIntent(DataPacketInboundIntent::class.java, "admin-display") {
			val type = PacketType.fromCrc(ByteBuffer.wrap(it.data).order(ByteOrder.LITTLE_ENDIAN).getInt(2))
			val packet = if (type == PacketType.OBJECT_CONTROLLER) {
				ObjectController.decodeController(NetBuffer.wrap(it.data))
			} else {
				val packet = PacketType.getForCrc(type?.crc ?: return@registerForIntent) ?: return@registerForIntent
				packet.decode(NetBuffer.wrap(it.data))
				packet
			}
			runLater {
				packets.add(ForwarderPacket(true, packet))
			}
		}
		forwarder.intentManager.registerForIntent(DataPacketOutboundIntent::class.java, "admin-display") {
			val type = PacketType.fromCrc(ByteBuffer.wrap(it.data).order(ByteOrder.LITTLE_ENDIAN).getInt(2))
			val packet = if (type == PacketType.OBJECT_CONTROLLER) {
				ObjectController.decodeController(NetBuffer.wrap(it.data))
			} else {
				val packet = PacketType.getForCrc(type?.crc ?: return@registerForIntent) ?: return@registerForIntent
				packet.decode(NetBuffer.wrap(it.data))
				packet
			}
			runLater {
				packets.add(ForwarderPacket(false, packet))
			}
		}
	}
	
	override fun onUndock() {
		super.onUndock()
		forwarder.intentManager.unregisterForIntent(DataPacketInboundIntent::class.java, "admin-display")
		forwarder.intentManager.unregisterForIntent(DataPacketOutboundIntent::class.java, "admin-display")
	}
}
