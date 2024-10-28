group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly(project(":api"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }
}