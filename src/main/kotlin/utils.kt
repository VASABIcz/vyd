import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

suspend fun Boolean.ifLaunch(fn: suspend () -> Unit): Boolean {
    println(this)
    if (this) {
        CoroutineScope(Dispatchers.IO).launch {
            fn()
        }
    }
    return this
}

inline fun<T> measureTimeMillis(text: String,function: () -> Unit){
    val startTime = System.currentTimeMillis()
    function()
    val endTime = System.currentTimeMillis()
    println("$text took ${endTime-startTime} ms")
}

fun base10to36(number: Int): String {
    var number = number
    val array = arrayOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z"
    )
    val l: MutableList<String> = ArrayList() //list to store reminder
    val strB = StringBuffer()
    while (number != 0) {
        l.add(array[number % 36]) //store reminder -- NOTE use array
        number /= 36 //change number
    }
    for (i in l.size - 1 downTo -1 + 1) {
        strB.append(l[i])
    }
    return if (strB.isBlank()) {
        return "0"
    }
    else {
        strB.toString()
    }
}

fun base36to10(number: String): Int {
    var res = 0
    val array = arrayOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z"
    )
    for (n in number.indices) {
        for (i in array.indices) {
            if (number[n].toString() == array[i]) {
                res += i*36.toDouble().pow(number.length - n.toDouble()-1).toInt()
            }
        }
    }
    return res
}