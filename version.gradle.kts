import xyz.enhancedpixel.gradle.utils.GameSide

plugins {
    `java-library`
    kotlin("jvm")
    `maven-publish`
    id("xyz.enhancedpixel.gradle.multiversion")
    id("xyz.enhancedpixel.gradle.tools")
    id("xyz.enhancedpixel.gradle.tools.loom")
    id("xyz.enhancedpixel.gradle.tools.blossom")
    id("xyz.enhancedpixel.gradle.tools.resources")
    id("xyz.enhancedpixel.gradle.tools.shadow")
}

loomHelper {
    disableRunConfigs(GameSide.CLIENT)
    disableRunConfigs(GameSide.SERVER)
    useProperty("elementa.dev", "true", GameSide.CLIENT)
}

fun Dependency?.excludeVitals(): Dependency = apply {
    check(this is ExternalModuleDependency)
    exclude(module = "kotlin-stdlib")
    exclude(module = "kotlin-stdlib-common")
    exclude(module = "kotlin-stdlib-jdk8")
    exclude(module = "kotlin-stdlib-jdk7")
    exclude(module = "kotlin-reflect")
    exclude(module = "annotations")
    exclude(module = "fabric-loader")
}!!

val bundle by configurations.creating {
    if (mcData.isFabric) {
        extendsFrom(configurations["include"])
    } else extendsFrom(configurations["shade"])
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    modApi(bundle(libs.versions.universalcraft.map {
        "gg.essential:universalcraft-${when(mcData.version) {
            11802 -> "1.18.1"
            11605 -> "1.16.2"
            else -> mcData.versionStr
        }}-${mcData.loader.name}:$it"
    }.get()).excludeVitals())
    modApi(bundle(libs.versions.elementa.map {
        "gg.essential:elementa-${when(mcData.version) {
            11902, 11802 -> "1.18.1"
            11605 -> "1.16.2"
            else -> mcData.versionStr
        }}-${mcData.loader.name}:$it"
    }.get()).excludeVitals())

    if (mcData.version >= 11602) {
        val lwjglVersion = "3.2.2"
        implementation("org.lwjgl:lwjgl-tinyfd:$lwjglVersion")
    }

    // We need Fabric API for testing purposes.
    if (mcData.isFabric) modRuntimeOnly(modCompileOnly("net.fabricmc.fabric-api:fabric-api:${when (mcData.version) {
        11902 -> "0.64.0+1.19.2"
        11802 -> "0.59.1+1.18.2"
        11701 -> "0.46.1+1.17"
        11605 -> "0.42.0+1.16"
        11502 -> "0.28.5+1.15"
        else -> throw IllegalArgumentException("Invalid Minecraft version")
    }}")!!)
}

afterEvaluate {
    publishing.publications.getByName<MavenPublication>("mavenJava") {
        group = modData.group
        artifactId = "${modData.name}-${mcData.versionStr}-${mcData.loader.name}".toLowerCase()
        version = modData.version
    }
}
