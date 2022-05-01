package com.schlewinow.happygallery.views.layouts

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Simple custom ConstraintLayout to allow square views depending on dynamic width.
 */
class SquareConstraintLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}