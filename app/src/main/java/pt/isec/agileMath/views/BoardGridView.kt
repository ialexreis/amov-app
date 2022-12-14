package pt.isec.agileMath.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import pt.isec.agileMath.constants.Constants
import kotlin.math.ceil

class BoardGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): GridView(context, attrs, defStyleAttr, defStyleRes), OnGestureListener
{
    private val gestureDetector = GestureDetector(context, this)
    private var boardVector: ArrayList<String>? = ArrayList()

    private var gridCellSize = 1

    private var swipePositions = object {
        // (x,y)
        var firstDownPositions = arrayOf(0.0f,0.0f)
        var moveMotionEventPositions = arrayOf(0.0f,0.0f)
    }

    constructor(context: Context, boardVector: ArrayList<String>): this(context) {
        this.boardVector = boardVector
        this.numColumns = 5

        this.adapter = object : BaseAdapter() {
            override fun getCount(): Int = boardVector.size

            override fun getItem(index: Int): String = boardVector[index]

            override fun getItemId(index: Int): Long {
                return index.toLong()
            }

            @SuppressLint("ResourceAsColor")
            override fun getView(
                index: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val textView = convertView ?: TextView(context).apply {
                    text = boardVector[index]
                    gravity = 1
                    setTextColor(Color.parseColor("#000000"))
                    textSize = 60F
                }
                return textView
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        gridCellSize = canvas.height / numColumns
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val boardPosition = getBoardPositionFromSwipe()

            // TODO executar uma func do view model que recebe a boardPosition e executa a logica
            // de negócio da board
            return true
        }

        if (ev != null && gestureDetector.onTouchEvent(ev)) {
            return true
        }

        return super.onTouchEvent(ev)
    }

    override fun onDown(e: MotionEvent): Boolean {
        swipePositions.firstDownPositions[0] = e.x
        swipePositions.firstDownPositions[1] = e.y

        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("onSingleTapUp", "onSingleTapUp")
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        swipePositions.moveMotionEventPositions[0] = e2.x
        swipePositions.moveMotionEventPositions[1] = e2.y
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d("onLongPress", "onLongPress")
    }

    override fun onTouchModeChanged(isInTouchMode: Boolean) {
        Log.d("onTouchModeChanged", isInTouchMode.toString())

        super.onTouchModeChanged(isInTouchMode)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    private fun getBoardPositionFromSwipe(): Constants.BOARD_POSITION {
        val firstDownLineIndex = ceil(swipePositions.firstDownPositions[0] / gridCellSize)
        val firstDownColumnIndex = ceil(swipePositions.firstDownPositions[1] / gridCellSize)
        val lastMovedLineIndex = ceil(swipePositions.moveMotionEventPositions[0] / gridCellSize)
        val lastMovedColumnIndex = ceil(swipePositions.moveMotionEventPositions[1] / gridCellSize)


        return Constants.BOARD_POSITION.NONE
    }
}