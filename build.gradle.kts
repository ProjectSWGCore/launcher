//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	application
	java
	idea
	kotlin("jvm") version "1.4.32"
	id("nebula.deb") version "8.4.1"
	id("nebula.rpm") version "8.4.1"
	id("edu.sc.seis.macAppBundle") version "2.3.0"
	id("org.beryx.jlink") version "2.23.6"
}

// Note: define javaVersion, javaMajorVersion, javaHomeLinux, javaHomeMac, and javaHomeWindows
//       inside your gradle.properties file
val javaVersion: String by project
val javaMajorVersion: String by project
val kotlinTargetJdk: String by project
val javaHomeLinux: String by project
val javaHomeMac: String by project
val javaHomeWindows: String by project

subprojects {
	ext {
		set("javaVersion", javaVersion)
		set("javaMajorVersion", javaMajorVersion)
		set("kotlinTargetJdk", kotlinTargetJdk)
	}
}

group = "com.projectswg.launcher"
version = "1.3.5"

application {
	mainModule.set("com.projectswg.launcher")
	mainClass.set("com.projectswg.launcher.core.LauncherKt")
	applicationDefaultJvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
}

repositories {
	mavenCentral()
	maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
	jcenter()
}

sourceSets {
	main {
        java.outputDir = File(java.outputDir.toString().replace("\\${File.separatorChar}java", ""))
		
		dependencies {
			val jfxOptions = object {
				val group = "org.openjfx"
				val version = javaVersion
				val fxModules = arrayListOf("javafx-base", "javafx-graphics", "javafx-controls", "javafx-fxml", "javafx-swing", "javafx-web", "javafx-media")
			}
			jfxOptions.run {
				val osName = System.getProperty("os.name")
				val platform = when {
					osName.startsWith("Mac", ignoreCase = true) -> "mac"
					osName.startsWith("Windows", ignoreCase = true) -> "win"
					osName.startsWith("Linux", ignoreCase = true) -> "linux"
					else -> ""
				}
				fxModules.forEach {
					implementation("$group:$it:$version:$platform")
				}
			}
			
			implementation(project(":pswgcommon"))
			implementation(project(":client-holocore"))
			implementation(project(":forwarder"))
			implementation(project(":zero_allocation_hashing"))
			implementation("javax.json:javax.json-api:1.1.4")
			implementation(group="me.joshlarson", name="fast-json", version="3.0.1")
			implementation(group="me.joshlarson", name="jlcommon-fx", version="1.0.3")
			implementation(group="no.tornado", name="tornadofx", version="2.0.0-SNAPSHOT") {
				exclude(group="org.jetbrains.kotlin")
			}
			implementation(kotlin("stdlib"))
			implementation(kotlin("reflect"))
			implementation(group="de.jensd", name="fontawesomefx-fontawesome", version="4.7.0-9.1.2")
			implementation(group="com.rometools", name="rome", version="1.15.0")
			implementation(group="org.apache.commons", name="commons-text", version="1.9")
		}
	}
	test {
		dependencies {
			implementation(group="junit", name="junit", version="4.12")
		}
	}
	create("utility") {
		dependencies {
			implementation(group="org.bouncycastle", name="bcprov-jdk15on", version="1.60")
			implementation(group="me.joshlarson", name="fast-json", version="3.0.1")
			implementation(project(":zero_allocation_hashing"))
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

jlink {
	addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
	javaHome.set(javaHomeLinux)
	targetPlatform("linux", javaHomeLinux)
	targetPlatform("mac", javaHomeMac)
	targetPlatform("windows", javaHomeWindows)
	forceMerge("kotlin-stdlib")
	
	launcher {
		name = "projectswg"
		jvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
	}
	
}

/**
 * Copies the JLink created JRE into a subdirectory in build/ that contains a /Contents/Home/jre directory.
 * This has to happen because the Mac App Bundle plugin relies on that structure unfortunately.
 */
val macJreLocation = "$projectDir/build/mock-mac-jre/Contents/Home"
tasks.create<Copy>("createMacJREStructure") {
	dependsOn(tasks.named("jlink"))
	from("build/image/projectswg-mac")
	include("**/*")
	into("$macJreLocation/jre")
}

macAppBundle {
	appName = "ProjectSWG"
	dmgName = "ProjectSWG"
	icon = "src/main/resources/graphics/ProjectSWGLaunchpad.icns"
	mainClassName = application.mainClass.get()
	jvmVersion = javaVersion
	jreHome = macJreLocation
	bundleJRE = true
}

// Enforce that the JRE is copied with a Mac based structure
tasks.named("bundleJRE") {
	dependsOn(tasks.named("createMacJREStructure"))
}

tasks.create<com.netflix.gradle.plugins.deb.Deb>("linuxDeb") {
	dependsOn("jlink")
	release = "1"
	packageName = "projectswg"
	maintainer = "ProjectSWG"
	
	preInstall(file("packaging/linux/preInstall.sh"))
	postInstall(file("packaging/linux/postInstall.sh"))
	preUninstall(file("packaging/linux/preUninstall.sh"))
	postUninstall(file("packaging/linux/postUninstall.sh"))
	
	from ("build/image/projectswg-linux") {
		into("/opt/ProjectSWG")
	}
	from ("packaging/linux") {
		into("/opt/ProjectSWG")
	}
	
	link("/usr/share/applications/ProjectSWG.desktop", "/opt/ProjectSWG/ProjectSWG.desktop")
}

tasks.create<com.netflix.gradle.plugins.rpm.Rpm>("linuxRpm") {
	dependsOn("jlink")
	release = "1"
	packageName = "projectswg"
	maintainer = "ProjectSWG"
	
	preInstall(file("packaging/linux/preInstall.sh"))
	postInstall(file("packaging/linux/postInstall.sh"))
	preUninstall(file("packaging/linux/preUninstall.sh"))
	postUninstall(file("packaging/linux/postUninstall.sh"))
	
	from ("build/image/projectswg-linux") {
		into("/opt/ProjectSWG")
	}
	from ("packaging/linux") {
		exclude("*.sh")
		into("/opt/ProjectSWG")
	}
	
	link("/usr/share/applications/ProjectSWG.desktop", "/opt/ProjectSWG/ProjectSWG.desktop")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = kotlinTargetJdk
	}
	destinationDir = sourceSets.main.get().java.outputDir
}
