plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.18.2" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("net.neoforged.moddev.legacyforge") version "2.0.147" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}

stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    val loader = node.metadata.project.substringAfterLast('-')
    constants.match(loader, "fabric", "forge", "neoforge")
    constants["forgelike"] = loader != "fabric"
    fun rename(from: String, to: String) = listOf("\\b$from\\b", to, "\\b$to\\b", from)
    replacements {
        regex(eval(node.metadata.version, ">=26.1")) {
            for ((a, b) in listOf(
                "ResourceLocation" to "Identifier",
                "GuiGraphics" to "GuiGraphicsExtractor",
            )) {
                val (p1, r1, p2, r2) = rename(a, b)
                replace(p1, r1, p2, r2)
            }
        }
        regex(eval(node.metadata.version, ">=26.2")) {
            replace(
                "(?<=\\b(?:minecraft|mc|getInstance\\(\\)))\\.screen\\b(?!\\()", ".gui.screen()",
                "\\.gui\\.screen\\(\\)", ".screen"
            )
        }
    }
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

for (version in stonecutter.versions.map { it.version }.distinct()) tasks.register("publish$version") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishMods") { metadata.version == version })
}
