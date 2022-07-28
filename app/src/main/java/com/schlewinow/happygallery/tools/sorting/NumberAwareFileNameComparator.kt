package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer
import java.math.BigInteger
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Sort gallery entries by file name.
 * Regular string comparison will have a sorting order with numbers that may be unpleasant.
 * E.g. "1", "2" and "10" would be sorted as "1", "10" and "2".
 * This comparator will sort strings according to present number values instead.
 * Original code found here: https://codereview.stackexchange.com/questions/37192/number-aware-string-sorting-with-comparator
 */
class NumberAwareFileNameComparator(ascending: Boolean) :  BaseFileComparator(ascending) {
    private val PATTERN: Pattern = Pattern.compile("(\\D*)(\\d*)")

    override val valueComparator: Comparator<GalleryFileContainer>
        get() = object : Comparator<GalleryFileContainer> {
            override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
                val matcher1: Matcher = PATTERN.matcher(file1?.name?.lowercase()?: "")
                val matcher2: Matcher = PATTERN.matcher(file2?.name?.lowercase()?: "")

                // The only way find() could fail is at the end of a string
                while (matcher1.find() && matcher2.find()) {
                    // matcher.group(1) fetches any non-digits captured by the
                    // first parentheses in PATTERN.
                    val matcher1group1 = matcher1.group(1)?: ""
                    val matcher2group1 = matcher2.group(1)?: ""

                    val nonDigitCompare: Int = matcher1group1.compareTo(matcher2group1)
                    if (0 != nonDigitCompare) {
                        return nonDigitCompare
                    }

                    // matcher.group(2) fetches any digits captured by the
                    // second parentheses in PATTERN.
                    val matcher1group2 = matcher1.group(2)?: ""
                    val matcher2group2 = matcher2.group(2)?: ""
                    if (matcher1group2.isEmpty()) {
                        return if(matcher2group2.isEmpty()) 0 else -1
                    }
                    else if (matcher2group2.isEmpty()) {
                        return 1
                    }

                    val number1 = BigInteger(matcher1group2)
                    val number2 = BigInteger(matcher2group2)
                    val numberCompare: Int = number1.compareTo(number2)
                    if (0 != numberCompare) {
                        return numberCompare
                    }
                }

                // Handle if one string is a prefix of the other.
                // Nothing comes before something.
                return if(matcher1.hitEnd() && matcher2.hitEnd()) 0 else if(matcher1.hitEnd()) -1 else 1
            }
        }
}