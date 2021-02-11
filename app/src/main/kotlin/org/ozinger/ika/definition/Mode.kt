package org.ozinger.ika.definition

data class Mode(
    val mode: Char,
    val param: String? = null,
) {
    var isMemberMode = false
}

typealias Modes = MutableSet<Mode>

data class ModeModification(
    val adding: Set<Mode>? = null,
    val removing: Set<Mode>? = null,
)

data class ModeDefinition(
    val stackable: List<Char>,
    val parameterized: List<Char>,
    val parameterizedAdd: List<Char>,
    val general: List<Char>,
) {
    constructor(modeDefs: List<String>) : this(
        modeDefs[0].toList(),
        modeDefs[1].toList(),
        modeDefs[2].toList(),
        modeDefs[3].toList(),
    )

    constructor(modeDefString: String) : this(modeDefString.split(","))
}
