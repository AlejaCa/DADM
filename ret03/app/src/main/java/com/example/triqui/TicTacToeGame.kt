package com.example.triqui

import android.util.Log
import androidx.compose.animation.core.rememberTransition
import kotlin.random.Random

class TicTacToeGame {
    enum class DifficultyLevel {
        Easy, Harder, Expert
    }


    val HUMAN_PLAYER: String = "X"
    val COMPUTER_PLAYER: String = "O"
    val OPEN_SPOT: Char = ' '
    var board = Array(3) { Array(3) { "" } }
    var currentPlayer = HUMAN_PLAYER
    var mDifficultyLevel = DifficultyLevel.Expert


    fun restartGame() {
        board = Array(3) { Array(3) { "" } }
        currentPlayer = if (currentPlayer == HUMAN_PLAYER) COMPUTER_PLAYER else HUMAN_PLAYER
    }

    fun setMove(player: String, location: Int = 2): Boolean {
        val row: Int = location / 3
        val col: Int = location % 3
        // Player makes a move
        if (board[row][col] != "" || checkWinner() != "") return false
        board[row][col] = player
        if (checkWinner() == "") {
            if (isBoardFull()) {
                return true
            }
            // Switch to the next player after making a move
            currentPlayer = if (player == HUMAN_PLAYER) COMPUTER_PLAYER else HUMAN_PLAYER
        }
        return true
    }

    fun computerMove(): Int {
        if (currentPlayer == COMPUTER_PLAYER) {
            var move = -1
            when (mDifficultyLevel) {
                DifficultyLevel.Easy -> move = makeRandomMove()
                DifficultyLevel.Harder -> {
                    move = makeWinningMove()
                    if (move == -1) move = makeRandomMove()
                }
                DifficultyLevel.Expert -> {
                    move = makeWinningMove()
                    if (move == -1) {
                        move = makeBlockingMove()
                        if (move == -1) move = makeRandomMove()
                    }
                }
            }
            if (checkWinner() == "") {
                if (isBoardFull()) {
                    return move
                }
                // Switch to the next player after making a move
                currentPlayer = if (currentPlayer == HUMAN_PLAYER) COMPUTER_PLAYER else HUMAN_PLAYER
            }
             // After computer's move, switch to player
            return move
        }
        return -1
    }


    fun checkWinner(): String {
        // Check rows and columns
        for (i in 0..2) {
            if (board[i][0] != "" && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
            if (board[0][i] != "" && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }

        // Check diagonals
        if (board[0][0] != "" && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != "" && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }

        return ""
    }

    public fun isBoardFull(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") return false
            }
        }
        return true
    }

    fun makeWinningMove(): Int {
        var m = -1
        for (i in 0..2) {
            m = checkAndPlaceO(i, isRow = true,  isDiagonal = false, "O")
            if (m != -1) {
                return m
            }
            m = checkAndPlaceO(i, isRow = false,  isDiagonal = false,"O")
            if (m != -1) {
                return m
            }
        }
        m = checkAndPlaceO(0, isRow = false, isDiagonal = true, "O")
        if (m!= -1) {
            return m
        }
        m = checkAndPlaceO(1, isRow = false, isDiagonal = true, "O")
        if (m != -1) {
            return m
        }

        return -1
    }

     fun makeBlockingMove(): Int {
         var m = -1
        for (i in 0..2) {
            m= checkAndBlockX(i, isRow = true, isDiagonal = false)
            if (m != -1){
                return m
            }
            m = checkAndBlockX(i, isRow = false,  isDiagonal = false)
            if (m!= -1) {
                return m
            }
        }
        m = checkAndBlockX(0, isRow = false, isDiagonal = true)
        if (m!= -1){
            return m
        }
         m= checkAndBlockX(1, isRow = false, isDiagonal = true)
        if (m!= -1) {
            return m
        }

        return -1
    }

    fun makeRandomMove():Int {
        var row: Int
        var col: Int
        do {
            row = Random.nextInt(3)
            col = Random.nextInt(3)
        } while (board[row][col] != "")

        board[row][col] = COMPUTER_PLAYER
        return 3*row +col
    }

     fun checkAndPlaceO(index: Int, isRow: Boolean,isDiagonal:Boolean, move: String): Int {
        var row=0
        var col=-1
        val line = when {
            isRow -> board[index]
            isDiagonal -> arrayOf(board[0][0], board[1][1], board[2][2]) // Main diagonal
            index == 1 -> arrayOf(board[0][2], board[1][1], board[2][0]) // Anti-diagonal
            else -> arrayOf(board[0][index], board[1][index], board[2][index])
        }

        val emptyIndex = line.indexOf("")
        if (emptyIndex != -1 && line.count { it == move } == 2) {
            when {
                isRow -> {
                    board[index][emptyIndex] = COMPUTER_PLAYER
                    row = index
                    col = emptyIndex
                }
                isDiagonal && index == 0 ->{
                    board[emptyIndex][emptyIndex] = COMPUTER_PLAYER
                    row = emptyIndex
                    col = emptyIndex
                }
                isDiagonal && index == 1 -> {
                    board[emptyIndex][2 - emptyIndex] = COMPUTER_PLAYER
                    row= emptyIndex
                    col= 2 -emptyIndex
                }
                else ->{
                    board[emptyIndex][index] = COMPUTER_PLAYER
                    row = emptyIndex
                    col = index
                }
            }
            //currentPlayer = HUMAN_PLAYER
            return 3*row +col
        }
        return -1
    }

     fun checkAndBlockX(index: Int, isRow: Boolean,isDiagonal:Boolean): Int {
         var row=0
         var col=-1
         val line = when {
            isRow -> board[index]
            isDiagonal -> {
                if (index == 0) arrayOf(board[0][0], board[1][1], board[2][2]) // Main diagonal
                else arrayOf(board[0][2], board[1][1], board[2][0]) // Anti-diagonal
            }
            else -> arrayOf(board[0][index], board[1][index], board[2][index])
        }

        val emptyIndex = line.indexOf("")
        if (emptyIndex != -1 && line.count { it == HUMAN_PLAYER } == 2) {
            when {
                isRow -> {
                    board[index][emptyIndex] = COMPUTER_PLAYER
                    row = index
                    col = emptyIndex
                }
                isDiagonal && index == 0 -> {
                    board[emptyIndex][emptyIndex] = COMPUTER_PLAYER
                    row = emptyIndex
                    col = emptyIndex
                } // For main diagonal
                isDiagonal && index == 1 ->  {
                    board[emptyIndex][2 - emptyIndex] = COMPUTER_PLAYER
                    row = emptyIndex
                    col = 2 -emptyIndex
                }
                else ->  {
                    board[emptyIndex][index] = COMPUTER_PLAYER
                    row = emptyIndex
                    col = index
                }
            }
            //currentPlayer = HUMAN_PLAYER
            return 3*row +col
        }
        return -1
    }
    fun setDifficulty(difficulty: DifficultyLevel) {
        // Update the AI difficulty level
        mDifficultyLevel = difficulty

    }



}
