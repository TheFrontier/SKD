package frontier.skd

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import kotlin.reflect.KProperty

abstract class NodeConfig {

    abstract val node: ConfigurationNode

    fun string(key: String? = null, comment: String? = null, default: () -> String = { "" }): ValueProvider<String> =
        ValueProvider(key, comment) { this.getString(default()) }

    fun boolean(
        key: String? = null,
        comment: String? = null,
        default: () -> Boolean = { false }
    ): ValueProvider<Boolean> =
        ValueProvider(key, comment) { this.getBoolean(default()) }

    fun int(key: String? = null, comment: String? = null, default: () -> Int = { 0 }): ValueProvider<Int> =
        ValueProvider(key, comment) { this.getInt(default()) }

    fun long(key: String? = null, comment: String? = null, default: () -> Long = { 0 }): ValueProvider<Long> =
        ValueProvider(key, comment) { this.getLong(default()) }

    fun float(key: String? = null, comment: String? = null, default: () -> Float = { 0.0f }): ValueProvider<Float> =
        ValueProvider(key, comment) { this.getFloat(default()) }

    fun double(key: String? = null, comment: String? = null, default: () -> Double = { 0.0 }): ValueProvider<Double> =
        ValueProvider(key, comment) { this.getDouble(default()) }

    fun <T> value(
        key: String? = null,
        comment: String? = null,
        type: TypeToken<T>,
        default: () -> T
    ): ValueProvider<T> =
        ValueProvider(key, comment) { this.getValue(type, default) }

    fun <T> list(
        key: String? = null,
        comment: String? = null,
        elementType: TypeToken<T>,
        default: () -> List<T> = { listOf() }
    ): ValueProvider<List<T>> =
        ValueProvider(key, comment) { this.getList(elementType, default) }

    fun <K, V> map(
        key: String? = null,
        comment: String? = null,
        keyType: TypeToken<K>,
        valueType: TypeToken<V>,
        default: () -> Map<K, V> = { mapOf() }
    ): ValueProvider<Map<K, V>> =
        ValueProvider(key, comment) { this.getValue(mapTypeTokenOf(keyType, valueType), default) }

    fun <T : NodeConfig> section(
        key: String? = null,
        comment: String? = null,
        init: (ConfigurationNode) -> T
    ): SectionProvider<T> =
        SectionProvider(key, comment, init)

    inner class ValueProvider<T>(
        private val key: String? = null,
        private val comment: String? = null,
        private val setter: ConfigurationNode.(T) -> Unit = { this.value = it },
        private val getter: ConfigurationNode.() -> T
    ) {

        operator fun provideDelegate(self: Any?, property: KProperty<*>): Value<T> {
            val childNode = node.getNode(key ?: property.name)
            (childNode as? CommentedConfigurationNode)?.setComment(comment)
            return Value(childNode, setter, getter)
        }
    }

    inner class Value<T>(
        private val node: ConfigurationNode,
        private val setter: ConfigurationNode.(T) -> Unit,
        private val getter: ConfigurationNode.() -> T
    ) {

        operator fun getValue(self: Any?, property: KProperty<*>): T =
            node.getter()

        operator fun setValue(self: Any?, property: KProperty<*>, value: T) {
            node.setter(value)
        }
    }

    inner class SectionProvider<T : NodeConfig>(
        private val key: String? = null,
        private val comment: String? = null,
        private val init: (ConfigurationNode) -> T
    ) {

        operator fun provideDelegate(self: Any?, property: KProperty<*>): Section<T> {
            val childNode = node.getNode(key ?: property.name)
            (childNode as? CommentedConfigurationNode)?.setComment(comment)
            return Section(init(childNode))
        }
    }

    inner class Section<T : NodeConfig>(private val instance: T) {

        operator fun getValue(self: Any?, property: KProperty<*>): T = instance
    }

    inline fun <reified T> value(
        key: String? = null,
        comment: String? = null,
        noinline default: () -> T
    ): NodeConfig.ValueProvider<T> =
        this.value(key, comment, typeToken(), default)

    inline fun <reified T> list(
        key: String? = null,
        comment: String? = null,
        noinline default: () -> List<T> = { listOf() }
    ): NodeConfig.ValueProvider<List<T>> =
        this.list(key, comment, typeToken(), default)

    inline fun <reified K, reified V> map(
        key: String? = null,
        comment: String? = null,
        noinline default: () -> Map<K, V> = { mapOf() }
    ): NodeConfig.ValueProvider<Map<K, V>> =
        this.map(key, comment, typeToken(), typeToken(), default)
}