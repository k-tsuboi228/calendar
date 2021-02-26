package jp.co.abs.calender

import android.util.Log

class Test {
    var test: String = "test"
    var test2: String? = "null"
    lateinit var test3: String
    var test1 = "test"

    val test4: String = ""

    val test5: Int = 123
    fun method(int: Int, string: String = "a", string2: String = "b"): String {
        test3 = "test"
        Log.i("test", "Int: $int String: $string String2: $string2")
        return ""
    }

    fun method1() {
        method(1)
        method(2, "test")
        method(2, "test", "test2")
        method(2, string2 = "test2")

        var result: String = ""

        for (int in 0..5) {
            if (int == 1) {
                result = "x1"
            } else if (int == 2) {
                result = "x2"
            } else {
                result = "else"
            }
            Log.i("test", "result: $result")

            result = if (int == 2) "true" else "false"
            Log.i("test", "result: $result")

            result = when (int) {
                1 -> "x1"
                2 -> "x2"
                else -> "else"
            }
            Log.i("test", "result: $result")
            Log.i("test", if (int in 2..4) "2～4" else "0.1.5")
            if (2 <= int && int <= 4)
                Log.i("test", "------------")
        }
//        Log.i("test", addABC("test"))
//        Log.i("test", "test".addABC())
        log("test".addABC())

        val userList = listOf(User("tsuboi", 25), User("a", 20), User("b", 15))
        for (user in userList.filter { it.name.contains("b") }) {
//            if (user.name.contains("b"))
            log(user.toString())
        }
    }
//    fun addABC(string: String):String{
//        return "$string ABC"
//    }

    private fun String.addABC() = "$this ABC"
//    fun method(a: Int){
//        method(a,"")
//    }

    class User(val name: String, val old: Int) {
        override fun toString(): String {
            super.toString()
            return "$name $old"
        }
    }


}