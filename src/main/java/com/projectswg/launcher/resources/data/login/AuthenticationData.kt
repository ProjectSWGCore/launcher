package com.projectswg.launcher.resources.data.login

import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.setValue

class AuthenticationData(val name: String) {
	
	val nameProperty = ReadOnlyStringWrapper(name)
	val usernameProperty = SimpleStringProperty("")
	val passwordProperty = SimpleStringProperty("")
	
	var username: String by usernameProperty
	var password: String by passwordProperty
	
}
