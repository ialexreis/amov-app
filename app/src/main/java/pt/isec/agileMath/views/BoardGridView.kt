package pt.isec.agileMath.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import pt.isec.agileMath.constants.Constants
import pt.isec.agileMath.constants.GameState
import pt.isec.agileMath.viewModels.gameViewModel.GameViewModel
import kotlin.math.abs
import kotlin.math.ceil

class BoardGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): GridView(context, attrs, defStyleAttr, defStyleRes), OnGestureListener
{
    private val gestureDetector = GestureDetector(context, this)

    private val boardTextViews = MutableList(Constants.BOARD_LINES * Constants.BOARD_LINES){ View(context) }

    private lateinit var viewModel: GameViewModel

    private var boardVector: MutableList<String> = MutableList(Constants.BOARD_LINES * Constants.BOARD_LINES){""}

    private var gridCellSize = 1

    private var swipePositions = object {
        // (x,y)
        var firstDownPositions = arrayOf(0.0f,0.0f)
        var moveMotionEventPositions = arrayOf(0.0f,0.0f)
    }

    constructor(context: Context, viewModel: GameViewModel): this(context) {
        this.viewModel = viewModel

        refreshBoard()
    }

    fun refreshBoard() {
        this.numColumns = 5
        this.boardVector = viewModel.vector
        this.adapter = getGridViewAdapter()
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
            val swipePosition = getBoardPositionFromSwipe()
            viewModel.executeMove(swipePosition)
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

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    private fun getBoardPositionFromSwipe(): Constants.BOARD_POSITION {
        val firstDownCellIndexX = getCellIndexFromPosition(swipePositions.firstDownPositions[0])
        val firstDownCellIndexY = getCellIndexFromPosition(swipePositions.firstDownPositions[1])
        val lastMovedCellIndexX = getCellIndexFromPosition(swipePositions.moveMotionEventPositions[0])
        val lastMovedCellIndexY = getCellIndexFromPosition(swipePositions.moveMotionEventPositions[1])

        val cellsScrolledX = abs(firstDownCellIndexX - lastMovedCellIndexX)
        val cellsScrolledY = abs(firstDownCellIndexY - lastMovedCellIndexY)

        if (firstDownCellIndexX == lastMovedCellIndexX && cellsScrolledY > 1) {
            when(lastMovedCellIndexX) {
                0 -> return Constants.BOARD_POSITION.COLUMN_RIGHT
                2 -> return Constants.BOARD_POSITION.COLUMN_CENTER
                4 -> return Constants.BOARD_POSITION.COLUMN_LEFT
            }
        }

        if (firstDownCellIndexY == lastMovedCellIndexY && cellsScrolledX > 1) {
            when(lastMovedCellIndexY) {
                0 -> return Constants.BOARD_POSITION.LINE_TOP
                2 -> return Constants.BOARD_POSITION.LINE_MIDDLE
                4 -> return Constants.BOARD_POSITION.LINE_BOTTOM
            }
        }

        return Constants.BOARD_POSITION.NONE
    }

    private fun getCellIndexFromPosition(position: Float): Int {
        return ceil(position / gridCellSize).toInt() - 1
    }


    private fun getGridViewAdapter(): BaseAdapter {
        val cellSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()

        return object : BaseAdapter() {
            override fun getCount(): Int = boardVector.size

            override fun getItem(index: Int): String = boardVector[index]

            override fun getItemId(index: Int): Long {
                return index.toLong()
            }

            override fun getView(
                index: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val backgroundColor = when(index % 2) {
                    0 -> "#994700"
                    else -> "#E8E5AC"
                }

                boardTextViews[index] = TextView(context).apply {
                    text = boardVector[index]
                    gravity = 1
                    setTextColor(Color.parseColor("#000000"))
                    setBackgroundColor(Color.parseColor(backgroundColor))
                    textSize = 20F
                    width = cellSize
                    height = cellSize
                    textAlignment = TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER_VERTICAL
                }

                return boardTextViews[index]
            }
        }
    }
}