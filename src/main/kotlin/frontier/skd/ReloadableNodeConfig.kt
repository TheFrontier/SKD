package frontier.skd

import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader

abstract class ReloadableNodeConfig(open val loader: ConfigurationLoader<out ConfigurationNode>) :
    NodeConfig() {

    override lateinit var node: ConfigurationNode

    fun reload() {
        node = loader.load()
    }

    fun save() {
        loader.save(node)
    }

    init {
        reload()
    }
}