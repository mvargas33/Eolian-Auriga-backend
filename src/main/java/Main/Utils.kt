package Main

/**
 * Interprets a value using two complement
 *
 * @property value The value to interpret
 * @property sbits The amount of significant bits to be considered (1 indexed)
 */
fun twoComp(value: Int, sbits: Int) : Int {
    return if(value and (1 shl (sbits-1)) == 0) value; // sign bit is marked, return as it is
    else value - (1 shl sbits);   // calculate 2comp representation
}

fun main() {
    println(twoComp(0, 8)) // -> 0
    println(twoComp(1, 8)) // -> 1
    println(twoComp(2, 8)) // -> 2
    println(twoComp(126, 8)) // -> 126
    println(twoComp(127, 8)) // -> 127
    println(twoComp(128, 8)) // -> -128
    println(twoComp(129, 8)) // -> -127
    println(twoComp(130, 8)) // -> -126
    println(twoComp(254, 8)) // -> -2
    println(twoComp(255, 8)) // -> -1
}