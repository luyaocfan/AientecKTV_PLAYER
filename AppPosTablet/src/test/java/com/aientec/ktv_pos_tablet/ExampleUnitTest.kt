package com.aientec.ktv_pos_tablet

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val a: Int = 1 or 4
        print("origin val : $a\n")
        print("Step 0 : ${(a and 1) == 1}\n")
        print("Step 1 : ${(a and 2) == 2}\n")
        print("Step 2 : ${(a and 4) == 4}\n")

        assertEquals(4, 2 + 2)
    }
}