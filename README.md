# ProjectSWG Launcher

## Quick Start
* Install JDK 26.0.2 and set `JAVA_HOME` to its installation directory.
* Clone the repo
* Pull submodules, build, and run
```bash
git submodule update --init --recursive
./gradlew jlink
./build/image/bin/projectswg
```

Compile without running tests:
```bash
./gradlew clean classes
```

The wrapper uses Gradle 9.7.1. Java and Kotlin both target JVM 26; JavaFX remains at 18.0.2 for compatibility with the existing UI libraries.

The retired TornadoFX snapshot is preserved in `libs/`; see `libs/README.md` for provenance and its checksum. Build updates also touch the `pswgcommon` and `forwarder` submodules.
