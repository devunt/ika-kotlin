package org.ozinger.ika.definition

interface IMode

data class Mode(
    val mode: Char,
    val param: String? = null,
) : IMode

data class MemberMode(
    val target: UniversalUserId,
    val mode: Char? = null,
) : IMode {
    constructor(target: String, mode: Char? = null) : this(UniversalUserId(target), mode)
}

typealias MutableModes = MutableSet<IMode>
typealias Modes = Set<IMode>

data class ModeModification(
    val adding: Modes = setOf(),
    val removing: Modes = setOf(),
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
