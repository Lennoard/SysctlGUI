package com.androidvip.sysctlgui.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter.RemovableViewHolder
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.show
import kotlin.math.abs


class SwipeToDeleteCallback(private val adapter: RemovableParamAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

    private val clearPaint: Paint = Paint()
    private val deleteDrawable: Drawable? = ContextCompat.getDrawable(
        adapter.context,
        R.drawable.ic_delete_sweep
    )
    private val background: ColorDrawable = ColorDrawable(ContextCompat.getColor(
        adapter.context,
        R.color.error
    ))
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        intrinsicWidth = deleteDrawable!!.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.removeItem(position, (viewHolder as RemovableViewHolder).removableView)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (viewHolder is RemovableViewHolder) {
            ItemTouchHelper.Callback.makeMovementFlags(0, ItemTouchHelper.START)
        } else 0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height

        val isCancelled = dX == 0f && !isCurrentlyActive
        if (isCancelled) {
            clearCanvas(
                canvas,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            (viewHolder as RemovableViewHolder).popupIcon.show()
            return super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, false)
        }

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        deleteDrawable?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)

        background.draw(canvas)
        if (dX < (-48F).dpToPx(recyclerView.context)) {
            deleteDrawable?.draw(canvas)
            (viewHolder as RemovableViewHolder).popupIcon.goAway()
        } else {
            (viewHolder as RemovableViewHolder).popupIcon.show()
        }

        val alpha = 1 - abs(dX) / viewHolder.itemView.width.toFloat()
        viewHolder.itemView.alpha = alpha
        viewHolder.itemView.translationX = dX

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }

    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        c.drawRect(left, top, right, bottom, clearPaint)
    }

    private fun Float.dpToPx(context: Context) : Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context.resources.displayMetrics
        )
    }
}