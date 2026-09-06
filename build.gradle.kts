import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	application
	java
	idea
	kotlin("jvm") version "2.4.10"
	id("org.beryx.jlink") version "4.1.1"
	id("de.undercouch.download") version "5.7.0"
}

val javaVersion = JavaVersion.current()
val javafxVersion = JavaVersion.current()
val kotlinTargetJdk = JvmTarget.JVM_26
val junit5Version = "5.12.2"

val osName: String = System.getProperty("os.name")
val platform: String = when {
	osName.startsWith("Mac", ignoreCase = true) -> "mac"
	osName.startsWith("Windows", ignoreCase = true) -> "win"
	osName.startsWith("Linux", ignoreCase = true) -> "linux"
	else -> ""
}
println("Configuration:")
println("    Java Version:       $javaVersion")
println("    JavaFX Version:     $javafxVersion")
println("    Kotlin Target:      $kotlinTargetJdk")
println("    Platform:           $platform")

subprojects {
	ext {
		set("javaVersion", javaVersion)
		set("javafxVersion", javafxVersion)
		set("junit5Version", junit5Version)
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
	mavenCentral()
}

sourceSets {
	main {
		dependencies {
			val jfxOptions = object {
				val group = "org.openjfx"
				val version = javafxVersion
				val fxModules = arrayListOf("javafx-base", "javafx-graphics", "javafx-controls", "javafx-fxml", "javafx-media", "javafx-swing", "javafx-web")
			}
			jfxOptions.run {
				fxModules.forEach {
					implementation("$group:$it:$version:$platform")
				}
			}
			
			implementation("org.jetbrains:annotations:20.1.0")
			implementation(project(":pswgcommon"))
			implementation(project(":forwarder"))
			implementation("javax.json:javax.json-api:1.1.4")
			implementation("me.joshlarson:fast-json:3.0.2")
			implementation("me.joshlarson:jlcommon-fx:17.0.1") {
				exclude(group="org.openjfx")
			}
			implementation(project(":tornadofx"))
			implementation(kotlin("stdlib"))
			implementation(kotlin("reflect"))
			implementation("de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2")
			implementation("com.rometools:rome:1.15.0") {
				exclude(group="org.slf4j")
			}
			implementation("org.apache.commons:commons-text:1.9")
			implementation("org.slf4j:slf4j-nop:1.7.36")
			
			testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
			testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
			testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
			testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
		}
	}
	test {
		dependencies {
			testImplementation("junit:junit:4.12")
		}
	}
}

idea {
    module {
        inheritOutputDirs = true
    }
}

java {
	modularity.inferModulePath.set(true)
	toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
}

val downloadJmods = tasks.register("downloadJmods", de.undercouch.gradle.tasks.download.Download::class) {
	val zipPath = layout.buildDirectory.file("jmods.zip").get().asFile
	val unzipPath = layout.buildDirectory.dir("jmods").get().asFile
	val baseUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/"
	val platformUrl = when(platform) {
		"mac" -> "openjfx-${javafxVersion}_osx-x64_bin-jmods.zip"
		"win" -> "openjfx-${javafxVersion}_windows-x64_bin-jmods.zip"
		else -> "openjfx-${javafxVersion}_linux-x64_bin-jmods.zip"
	}
	src(baseUrl + platformUrl)
	dest(zipPath)
	overwrite(false)
	
	doLast {
		copy {
			from(zipTree(zipPath))
			into(unzipPath)
		}
	}
}

tasks.named("prepareMergedJarsDir") {
	dependsOn(downloadJmods)
	mustRunAfter(downloadJmods)
}

jlink {
	addOptions("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages", "--ignore-signing-information")
	forceMerge("kotlin-stdlib")
	
	jlink {
		addExtraModulePath(layout.buildDirectory.dir("jmods/javafx-jmods-${javafxVersion}").get().asFile.absolutePath)
	}
	
	launcher {
		name = "projectswg"
		jvmArgs = listOf("--add-opens", "javafx.graphics/javafx.scene=tornadofx")
	}
	
	jpackage {
		vendor = "Project SWG"
		
		imageName = "ProjectSWG"
		installerName = "ProjectSWG"
		setInstallerOutputDir(layout.buildDirectory.dir("installer").get().asFile)
		
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
	compilerOptions {
		jvmTarget.set(kotlinTargetJdk)
	}
}

// Make Kotlin classes visible while javac compiles the JPMS module descriptor.
tasks.named<JavaCompile>("compileJava") {
	options.compilerArgumentProviders.add(CommandLineArgumentProvider {
		listOf("--patch-module", "com.projectswg.launcher=${sourceSets.main.get().output.asPath}")
	})
}
