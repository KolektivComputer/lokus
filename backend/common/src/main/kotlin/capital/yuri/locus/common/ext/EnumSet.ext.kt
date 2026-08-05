package capital.yuri.locus.common.ext

import java.util.EnumSet

/** Returns an [EnumSet] containing every constant of [E]. */
inline fun <reified E : Enum<E>> enumSetAll(): EnumSet<E> =
    EnumSet.allOf(E::class.java)

/** Returns an empty [EnumSet] for [E]. */
inline fun <reified E : Enum<E>> enumSetNone(): EnumSet<E> =
    EnumSet.noneOf(E::class.java)

/** [EnumSet] of the given [values]. */
inline fun <reified E : Enum<E>> enumSetOf(vararg values: E): EnumSet<E> =
    if (values.isEmpty()) enumSetNone() else EnumSet.copyOf(values.toList())
