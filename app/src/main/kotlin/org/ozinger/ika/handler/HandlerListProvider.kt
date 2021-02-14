package org.ozinger.ika.handler

import kotlin.reflect.KFunction

interface HandlerListProvider {
    val list: List<KFunction<Unit>>
}
