import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	application
	java
	idea
	kotlin("jvm") version "1.3.50"
	id("nebula.deb") version "6.2.1"
	id("nebula.rpm") version "6.2.1"
	id("edu.sc.seis.macAppBundle") version "2.3.0"
	id("com.github.johnrengelman.shadow") version "5.1.0"
	id("org.openjfx.javafxplugin") version "0.0.7"
	id("org.beryx.jlink") version "2.16.0"
	id("org.javamodularity.moduleplugin") version "1.5.0"
}

// Note: define javaVersion, javaMajorVersion, javaHomeLinux, javaHomeMac, and javaHomeWindows
//       inside your gradle.properties file
val javaVersion: String by project
val javaMajorVersion: String by project
val javaHomeLinux: String by project
val javaHomeMac: String by project
val javaHomeWindows: String by project

group = "com.projectswg.launcher"
version = "1.3.4"

application {
	mainClassName = "com.projectswg.launcher/com.projectswg.launcher.core.LauncherKt"
	applicationDefaultJvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
}

repositories {
	mavenLocal()
	maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    jcenter()
}

sourceSets {
	main {
		dependencies {
			implementation(project(":pswgcommon"))
			implementation(project(":client-holocore"))
			implementation(project(":forwarder"))
			implementation(group="net.openhft", name="zero-allocation-hashing", version="0.8")
			implementation(group="me.joshlarson", name="fast-json", version="2.2.3")
			implementation(group="me.joshlarson", name="jlcommon-fx", version="1.0.3")
			implementation(group="no.tornado", name="tornadofx", version="2.0.0-SNAPSHOT") {
				exclude(group="org.jetbrains.kotlin")
			}
			implementation(kotlin("stdlib"))
			implementation(kotlin("reflect"))
			implementation(group="de.jensd", name="fontawesomefx-fontawesome", version="4.7.0-9.1.2")
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
			implementation(group="me.joshlarson", name="fast-json", version="2.2.3")
			implementation(group="net.openhft", name="zero-allocation-hashing", version="0.8")
		}
	}
}

idea {
	targetVersion = javaVersion
    module {
        inheritOutputDirs = true
    }
}

jlink {
	addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
	targetPlatform("linux", javaHomeLinux)
	targetPlatform("mac", javaHomeMac)
	targetPlatform("windows", javaHomeWindows)
	
	launcher {
		name = "projectswg"
		jvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
	}
}

javafx {
	version = javaVersion
	modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web", "javafx.media")
}

tasks.named<ShadowJar>("shadowJar") {
	archiveBaseName.set("Launcher")
	archiveClassifier.set("")
	archiveVersion.set("")
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
	mainClassName = application.mainClassName
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

tasks.create<ShadowJar>("CreateUpdateListTask") {
	archiveFileName.set("CreateUpdateList.jar")
	manifest.attributes["Main-Class"] = "com.projectswg.launcher.utility.CreateUpdateList"
	from(sourceSets["utility"].output)
	exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
	kotlinOptions {
		jvmTarget = javaMajorVersion
		noReflect = false
		noStdlib = false
	}
}
