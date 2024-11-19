package com.example.triqui

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var restartButton: Button
    private lateinit var textViewTurn: TextView
    private lateinit var gridLayoutPoints: GridLayout
    private lateinit var textViewPoint2: TextView
    private lateinit var textViewPoint4: TextView
    private lateinit var textViewPoint6: TextView
    private var currentPlayer = "X"
    private var jugador = 0
    private var empate = 0
    private var computador =0
    private val board = Array(3) { Array(3) { "" } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.restartButton)
        textViewTurn = findViewById(R.id.textViewTurn)
        textViewPoint2 = findViewById(R.id.textViewPoint2)
        textViewPoint4 = findViewById(R.id.textViewPoint4)
        textViewPoint6 = findViewById(R.id.textViewPoint6)

        // Initialize the board with buttons and set up their click listeners
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.setBackgroundResource(R.drawable.border)
            button.setOnClickListener { onCellClick(button, i) }
        }

        // Restart game listener
        restartButton.setOnClickListener {
            restartGame()
        }


        currentPlayer = "X"
        textViewTurn.text = "Turno Jugador"

    }

    private fun onCellClick(button: Button, index: Int) {
        val row = index / 3
        val col = index % 3

        // If the cell is already marked or the game is over, don't do anything
        if (button.text != "" || checkWinner() != "") return

        // Mark the cell with the current player's symbol
        board[row][col] = currentPlayer
        button.text = currentPlayer

        // Check if there's a winner
        val winner = checkWinner()
        if (winner != "") {
            showWinner(winner)
            return
        }

        // If the board is full, it's a tie
        if (isBoardFull()) {
            showTie()
            return
        }

        // Switch to the next player
        currentPlayer = if (currentPlayer == "X") "O" else "X"
        textViewTurn.text = if (currentPlayer == "X") "Turno Jugador" else "Turno Computador"

        // If it's the computer's turn, make the computer move
        if (currentPlayer == "O" && checkWinner() == "") {
            computerMove()
        }
    }

    private fun computerMove() {
        textViewTurn.text = "Turno Computador"
        var row: Int
        var col: Int

        // Search for an empty cell for the computer
        do {
            row = Random.nextInt(3)
            col = Random.nextInt(3)
        } while (board[row][col] != "")

        // Make the move
        board[row][col] = "O"
        val button = gridLayout.getChildAt(row * 3 + col) as Button
        button.text = "O"

        // Check if the computer won
        val winner = checkWinner()
        if (winner != "") {
            showWinner(winner)
        } else if (isBoardFull()) {
            showTie()
        } else {
            currentPlayer = "X"  // Change back to the player
            textViewTurn.text = "Turno Jugador"
        }
    }

    private fun checkWinner(): String {
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

        // No winner
        return ""
    }

    private fun isBoardFull(): Boolean {
        // Check if the board is full
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    return false
                }
            }
        }
        return true
    }

    private fun showWinner(winner: String) {
        var message = ""
        if (winner == "X") {
            message= "¡Jugador gana!"
            jugador += 1
            textViewPoint2.text = jugador.toString()

        }else {
            message = "¡Computadora gana!"
            computador += 1
            textViewPoint6.text = computador.toString()
        }


        restartButton.text = message
        restartButton.isEnabled = true
    }

    private fun showTie() {
        restartButton.text = "¡Empate!"
        empate += 1
        textViewPoint4.text = empate.toString()
        restartButton.isEnabled = true
    }

    private fun restartGame() {
        // Reset the board
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.text = ""
        }

        // Reset the board data
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = ""
            }
        }

        // Decide who starts the new game
        if (currentPlayer == "O" ) {
            currentPlayer = "X"
            textViewTurn.text = "Turno Jugador"
        } else {
            currentPlayer = "O"
            textViewTurn.text = "Turno Computador"
            computerMove()
        }

        restartButton.text = "Reiniciar"
        restartButton.isEnabled = false
    }
}
