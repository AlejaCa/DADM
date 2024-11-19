package com.example.triqui

import android.os.Bundle

import android.widget.Button
import android.widget.GridLayout
import androidx.activity.ComponentActivity
import kotlin.random.Random


class MainActivity : ComponentActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var restartButton: Button
    private var currentPlayer = "X"
    private val board = Array(3) { Array(3) { "" } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.restartButton)

        // Inicializar el tablero con los botones
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.setBackgroundResource(R.drawable.border)
            button.setOnClickListener { onCellClick(button, i) }
        }

        // Reiniciar el juego
        restartButton.setOnClickListener {
            restartGame()
        }
    }

    private fun onCellClick(button: Button, index: Int) {
        val row = index / 3
        val col = index % 3

        // Si la celda ya tiene un valor o el juego ya terminó, no hacer nada
        if (button.text != "" || checkWinner() != "") return

        // Marcar la casilla con el jugador actual
        board[row][col] = currentPlayer
        button.text = currentPlayer

        // Verificar si alguien ganó
        val winner = checkWinner()
        if (winner != "") {
            showWinner(winner)
            return
        }
        if (isBoardFull()) {
            showTie()
            return
        }

        // Cambiar al siguiente jugador
        currentPlayer = if (currentPlayer == "X") "O" else "X"

        // Si es el turno de la computadora y no hay ganador, hacer su movimiento
        if (currentPlayer == "O" && checkWinner() == "") {
            computerMove()
        }
    }

    private fun computerMove() {
        var row: Int
        var col: Int

        // Buscar una celda vacía aleatoria para la computadora
        do {
            row = Random.nextInt(3)
            col = Random.nextInt(3)
        } while (board[row][col] != "")

        // Hacer el movimiento de la computadora
        board[row][col] = "O"
        val button = gridLayout.getChildAt(row * 3 + col) as Button
        button.text = "O"

        // Verificar si la computadora ha ganado
        val winner = checkWinner()
        if (winner != "") {
            showWinner(winner)
        } else if (isBoardFull()) {
            showTie()
        } else {
            currentPlayer = "X"  // Cambiar de vuelta al jugador
        }
    }

    private fun checkWinner(): String {
        // Revisar filas y columnas
        for (i in 0..2) {
            if (board[i][0] != "" && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
            if (board[0][i] != "" && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }

        // Revisar diagonales
        if (board[0][0] != "" && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != "" && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }

        // No hay ganador
        return ""
    }
    private fun isBoardFull(): Boolean {
        // Verificar si todas las casillas están ocupadas
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
        val message = if (winner == "X") "¡Jugador gana!" else "¡Computadora gana!"
        restartButton.text = message
        restartButton.isEnabled = true
    }
    private fun showTie() {
        restartButton.text = "¡Empate!"
        restartButton.isEnabled = true
    }

    private fun restartGame() {
        // Reiniciar el tablero
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.text = ""
        }

        // Reiniciar los valores del juego
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = ""
            }
        }

        currentPlayer = "X"
        restartButton.text = "Reiniciar"
        restartButton.isEnabled = false
    }
}