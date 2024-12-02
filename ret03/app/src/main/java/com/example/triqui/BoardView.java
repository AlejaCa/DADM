package com.example.triqui;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;


public class BoardView extends View {
    public static final int GRID_WIDTH = 6;
    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;
    private Paint mPaint;

    public BoardView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // Initialize Paint
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // Initialize Paint
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // Initialize Paint
        initialize();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Determine the width and height of the View
        int boardWidth = getWidth();
        int boardHeight = getHeight();

        // Set paint color and stroke width
        mPaint.setColor(Color.MAGENTA);
        mPaint.setStrokeWidth(GRID_WIDTH);

        // Calculate the grid lines (3x3 grid)
        int cellWidth = boardWidth / 3;
        int cellHeight = boardHeight / 3;

        // Draw vertical lines
        canvas.drawLine(cellWidth, 0, cellWidth, boardHeight, mPaint);
        canvas.drawLine(cellWidth * 2, 0, cellWidth * 2, boardHeight, mPaint);

        // Draw horizontal lines
        canvas.drawLine(0, cellHeight, boardWidth, cellHeight, mPaint);
        canvas.drawLine(0, cellHeight * 2, boardWidth, cellHeight * 2, mPaint);

        @SuppressLint("DrawAllocation") Bitmap scaledHumanBitmap = Bitmap.createScaledBitmap(mHumanBitmap, cellWidth, cellHeight, false);
        @SuppressLint("DrawAllocation") Bitmap scaledComputerBitmap = Bitmap.createScaledBitmap(mComputerBitmap, cellWidth, cellHeight, false);


        // Draw all the X and O images
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
// Define the boundaries of a destination rectangle for the image
            int left = col * cellWidth;
            int top = row * cellHeight;
            int right = left + cellWidth;
            int bottom = top + cellHeight;
            if (mGame == null) {
                Log.e("BoardView", "Game is null in onDraw.");
                return;
            }
            String[][] board =mGame.getBoard();
            String cellValue = board[row][col];
            if (mGame != null && cellValue.equals("X")) {

                canvas.drawBitmap(scaledHumanBitmap,
                        null, // src
                        new Rect(left, top, right, bottom), // dest
                        null);

            }
            else if (mGame != null && cellValue.equals("O")) {
                canvas.drawBitmap(scaledComputerBitmap,
                        null, // src
                        new Rect(left, top, right, bottom), // dest
                        null);

            }
        }
    }

    public void initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.x_img);
        mComputerBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.o_img);
        if (mHumanBitmap == null) {
            Log.e("BoardView", "Human bitmap (X) failed to load.");
        }
        if (mComputerBitmap == null) {
            Log.e("BoardView", "Computer bitmap (O) failed to load.");
        }
    }
    private TicTacToeGame mGame;
    public void setGame(TicTacToeGame game) {
        mGame = game;
    }
    public int getBoardCellWidth() {
        return getWidth() / 3;
    }
    public int getBoardCellHeight() {
        return getHeight() / 3;
    }
}

