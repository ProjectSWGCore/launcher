plugins {
	application
	java
	idea
	kotlin("jvm") version "1.7.10"
	id("org.beryx.jlink") version "2.25.0"
	id("de.undercouch.download") version "5.0.5"
}

val javaVersion = "18.0.2"
val javaMajorVersion = "18"
val kotlinTargetJdk = "18"

val osName: String = System.getProperty("os.name")
val platform: String = when {
	osName.startsWith("Mac", ignoreCase = true) -> "mac"
	osName.startsWith("Windows", ignoreCase = true) -> "win"
	osName.startsWith("Linux", ignoreCase = true) -> "linux"
	else -> ""
}
println("Configuration:")
println("    Java Version:       $javaVersion")
println("    Java Major Version: $javaMajorVersion")
println("    Kotlin Target:      $kotlinTargetJdk")
println("    Platform:           $platform")

subprojects {
	ext {
		set("javaVersion", javaVersion)
		set("javaMajorVersion", javaMajorVersion)
		set("kotlinTargetJdk", kotlinTargetJdk)
	}
}

group = "com.projectswg.launcher"
version = "2.1.2"

application {
	mainModule.set("com.projectswg.launcher")
	mainClass.set("com.projectswg.launcher.LauncherKt")
	applicationDefaultJvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
}

repositories {
	maven("https://dev.joshlarson.me/maven2")
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	mavenCentral()
}

sourceSets {
	main {
		dependencies {
			val jfxOptions = object {
				val group = "org.openjfx"
				val version = javaVersion
				val fxModules = arrayListOf("javafx-base", "javafx-graphics", "javafx-controls")
			}
			jfxOptions.run {
				fxModules.forEach {
					implementation("$group:$it:$version:$platform")
				}
			}
			
			implementation(group="org.jetbrains", name="annotations", version="20.1.0")
			implementation(project(":pswgcommon"))
			implementation(project(":forwarder"))
			implementation("javax.json:javax.json-api:1.1.4")
			implementation(group="me.joshlarson", name="fast-json", version="3.0.2")
			implementation(group="me.joshlarson", name="jlcommon-fx", version="17.0.1") {
				exclude(group="org.openjfx")
			}
			implementation(group="no.tornado", name="tornadofx", version="2.0.0-SNAPSHOT") {
				exclude(group="org.jetbrains.kotlin")
				exclude(group="org.openjfx")
			}
			implementation(kotlin("stdlib"))
			implementation(kotlin("reflect"))
			implementation(group="de.jensd", name="fontawesomefx-fontawesome", version="4.7.0-9.1.2")
			implementation(group="com.rometools", name="rome", version="1.15.0") {
				exclude(group="org.slf4j")
			}
			implementation(group="org.apache.commons", name="commons-text", version="1.9")
			implementation("org.slf4j:slf4j-nop:1.7.36")
		}
	}
	test {
		dependencies {
			testImplementation(group="junit", name="junit", version="4.12")
		}
	}
}

idea {
	targetVersion = javaVersion
    module {
        inheritOutputDirs = true
    }
}

java {
	modularity.inferModulePath.set(true)
}

task("downloadJmods", de.undercouch.gradle.tasks.download.Download::class) {
	val zipPath = buildDir.absolutePath + "/jmods.zip"
	val unzipPath = buildDir.absolutePath + "/jmods"
	val baseUrl = "https://download2.gluonhq.com/openjfx/$javaVersion/"
	val platformUrl = when(platform) {
		"mac" -> "openjfx-${javaVersion}_osx-x64_bin-jmods.zip"
		"win" -> "openjfx-${javaVersion}_windows-x64_bin-jmods.zip"
		else -> "openjfx-${javaVersion}_linux-x64_bin-jmods.zip"
	}
	src(baseUrl + platformUrl)
	dest(zipPath)
	overwrite(false)
	tasks.getByName("prepareMergedJarsDir").dependsOn(this)
	tasks.getByName("prepareMergedJarsDir").mustRunAfter(this)
	
	doLast {
		copy {
			from(zipTree(zipPath))
			into(unzipPath)
		}
	}
}

jlink {
	addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages", "--ignore-signing-information")
	forceMerge("kotlin-stdlib")
	
	jlink {
		addExtraModulePath("${buildDir.absolutePath}/jmods/javafx-jmods-${javaVersion}")
	}
	
	launcher {
		name = "projectswg"
		jvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
	}
	
	jpackage {
		vendor = "Project SWG"
		
		imageName = "ProjectSWG"
		installerName = "ProjectSWG"
		installerOutputDir = File("${buildDir.absolutePath}/installer")
		
		installerType = when(platform) {
			"linux" -> "deb"
			"win" -> "exe"
			"mac" -> "dmg"
			else -> null
		}
		imageOptions = when(platform) {
			"win" -> listOf("--icon", "${projectDir.absolutePath}/src/main/resources/graphics/ProjectSWG.ico")
			else -> listOf("--icon", "${projectDir.absolutePath}/src/main/resources/graphics/ProjectSWG.png")
		}
		installerOptions = when(platform) {
			"linux" -> listOf("--linux-shortcut", "--icon", "${projectDir.absolutePath}/src/main/resources/graphics/ProjectSWG.png")
			"win" -> listOf("--win-dir-chooser", "--win-shortcut", "--win-menu", "--win-menu-group", "Project SWG", "--icon", "${projectDir.absolutePath}/src/main/resources/graphics/ProjectSWG.ico")
			else -> listOf("--icon", "${projectDir.absolutePath}/src/main/resources/graphics/ProjectSWG.png")
		}
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = kotlinTargetJdk
	}
	destinationDirectory.set(File(destinationDirectory.get().asFile.path.replace("kotlin", "java")))
}
