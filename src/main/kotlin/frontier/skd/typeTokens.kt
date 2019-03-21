package frontier.skd

import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken

inline fun <reified T> typeToken(): TypeToken<T> = object : TypeToken<T>() {}

fun <K, V> mapTypeTokenOf(keyType: TypeToken<K>, valueType: TypeToken<V>): TypeToken<Map<K, V>> =
    (object : TypeToken<Map<K, V>>() {})
        .where(object : TypeParameter<K>() {}, keyType)
        .where(object : TypeParameter<V>() {}, valueType)