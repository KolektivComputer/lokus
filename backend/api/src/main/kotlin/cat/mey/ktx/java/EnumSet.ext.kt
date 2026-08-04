package cat.mey.ktx.java

import java.util.EnumSet

inline fun <reified E : Enum<E>> enumSetOf(elements: Collection<E>): EnumSet<E> =
    if (elements.isEmpty()) EnumSet.noneOf(E::class.java)
    else EnumSet.copyOf(elements)