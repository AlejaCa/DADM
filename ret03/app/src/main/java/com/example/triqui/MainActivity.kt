package com.example.triqui

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    enum class DifficultyLevel {
        Easy, Harder, Expert
    };
    // Current difficulty level
    private var mDifficultyLevel = DifficultyLevel.Expert
    private lateinit var currentDifficulty: DifficultyLevel
    private lateinit var menuItemDifficulty: MenuItem
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

        currentDifficulty = DifficultyLevel.Expert
        currentPlayer = "X"
        textViewTurn.text = "Player Turn"

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        menuItemDifficulty = menu?.findItem(R.id.menu_difficulty)!!
        updateDifficultyMenuItem() // Update the difficulty in the menu
        return true
    }

    // Handle menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_new_game -> {
                restartGame()
                jugador = 0
                empate = 0
                computador =0
                textViewPoint4.text = empate.toString()
                textViewPoint2.text = jugador.toString()
                textViewPoint6.text = computador.toString()
                // Restart the game
                true
            }
            R.id.menu_difficulty -> {
                // Toggle between difficulty levels
                showDifficultyDialog()
                updateDifficultyMenuItem()// Show difficulty options
                true
            }
            R.id.menu_quit -> {
                confirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quit")
            .setMessage("Are you sure you want to quit the game?")
            .setCancelable(false) // Prevent closing dialog if clicking outside
            .setPositiveButton("Yes") { dialog, id ->
                finish() // Close the app if the user clicks "Yes"
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss() // Dismiss the dialog if the user clicks "No"
            }
        val alert = builder.create()
        alert.show()
    }

    private fun updateDifficultyMenuItem() {
        // Update the difficulty menu item with the current difficulty level
        menuItemDifficulty.title = "Difficulty: ${currentDifficulty.name}"
    }

    // Show difficulty options dialog
    private fun showDifficultyDialog() {
        val difficulties = arrayOf("Easy", "Harder", "Expert")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Difficulty")
            .setItems(difficulties) { dialog, which ->
                // Set the difficulty level based on user selection
                when (which) {
                    0 -> {
                        setDifficulty(DifficultyLevel.Easy)
                    }
                    1 -> setDifficulty(DifficultyLevel.Harder)
                    2 -> setDifficulty(DifficultyLevel.Expert)
                }
            }
        builder.create().show()
    }
    private fun setDifficulty(difficulty: DifficultyLevel) {
        // Update the AI difficulty level
        mDifficultyLevel = difficulty
        currentDifficulty = mDifficultyLevel
        updateDifficultyMenuItem()
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
        textViewTurn.text = if (currentPlayer == "X") "Player Turn" else "Computer Turn"

        // If it's the computer's turn, make the computer move
        if (currentPlayer == "O" && checkWinner() == "") {
            computerMove()
        }
    }

    private fun computerMove() {
        textViewTurn.text = "Computer Turn"
        var move = -1
        if (mDifficultyLevel == DifficultyLevel.Easy) {
            makeRandomMove()
        }
        else if (mDifficultyLevel == DifficultyLevel.Harder) {
            move = makeWinningMove()
            if (move == -1) makeRandomMove()
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
            move = makeWinningMove()
            if (move == -1) {
                move = makeBlockingMove()
            }
            if (move == -1) makeRandomMove()
        }
        // Check if the computer won
        val winner = checkWinner()
        if (winner != "") {
            showWinner(winner)
        } else if (isBoardFull()) {
            showTie()
        } else {
            currentPlayer = "X"  // Change back to the player
            textViewTurn.text = "Player Turn"
        }
    }
    private fun checkAndPlaceO(index: Int, isRow: Boolean, isDiagonal: Boolean = false, move: String): Boolean {
        val line = when {
            isRow -> board[index]
            isDiagonal -> arrayOf(board[0][0], board[1][1], board[2][2]) // Main diagonal
            index == 1 -> arrayOf(board[0][2], board[1][1], board[2][0]) // Anti-diagonal
            else -> arrayOf(board[0][index], board[1][index], board[2][index])
        }

        val emptyIndex = line.indexOf("")

        if (emptyIndex != -1 && line.count { it == move } == 2) {
            when {
                isRow ->{
                    board[index][emptyIndex] = "O"
                    val button = gridLayout.getChildAt(index * 3 + emptyIndex) as Button
                    button.text = "O"
                }
                isDiagonal && index == 0 -> {
                    board[emptyIndex][emptyIndex] = "O"
                    val button = gridLayout.getChildAt(emptyIndex * 3 + emptyIndex) as Button
                    button.text = "O"
                } // For main diagonal
                isDiagonal && index == 1 -> {
                    board[emptyIndex][2 - emptyIndex] = "O"
                    val button = gridLayout.getChildAt(emptyIndex * 3 + 2 - emptyIndex) as Button
                    button.text = "O"
                } // For anti-diagonal
                else -> {
                    board[emptyIndex][index] = "O"
                    val button = gridLayout.getChildAt(emptyIndex * 3 + index) as Button
                    button.text = "O"
                }
            }
            return true // Successfully placed "O", return true
        }
        return false // No placement made
    }
    private fun checkAndBlockX(index: Int, isRow: Boolean, isDiagonal: Boolean = false): Boolean {
        val line = when {
            isRow -> board[index]
            isDiagonal -> {
                if (index == 0) arrayOf(board[0][0], board[1][1], board[2][2]) // Main diagonal
                else arrayOf(board[0][2], board[1][1], board[2][0]) // Anti-diagonal
            }
            else -> arrayOf(board[0][index], board[1][index], board[2][index]) // Column
        }

        // Look for two "X"s and one empty spot in the line
        val emptyIndex = line.indexOf("")  // Get the index of the empty spot

        // If two "X"s are found and there's an empty spot, block it with "O"
        if (emptyIndex != -1 && line.count { it == "X" } == 2) {
            when {
                isRow -> {
                    board[index][emptyIndex] = "O"
                    val button = gridLayout.getChildAt(index * 3 + emptyIndex) as Button
                    button.text = "O"
                }
                isDiagonal && index == 0 -> {
                    board[emptyIndex][emptyIndex] = "O" // For main diagonal
                    val button = gridLayout.getChildAt(emptyIndex * 3 + emptyIndex) as Button
                    button.text = "O"
                }
                isDiagonal && index == 1 -> {
                    board[emptyIndex][2 - emptyIndex] = "O" // For anti-diagonal
                    val button = gridLayout.getChildAt(emptyIndex * 3 + 2 - emptyIndex) as Button
                    button.text = "O"
                }
                else -> {
                    board[emptyIndex][index] = "O" // For column
                    val button = gridLayout.getChildAt(emptyIndex * 3 + index) as Button
                    button.text = "O"
                }
            }
            return true // Blocked successfully
        }
        return false // No blocking needed
    }

    private fun makeWinningMove(): Int {
        // Try checking rows and columns, stop once a move is made
        for (i in 0..2) {
            if (checkAndPlaceO( i, isRow = true,isDiagonal = false, "O")) return 1 // Check rows
            if (checkAndPlaceO( i, isRow = false,isDiagonal = false, "O")) return 1// Check columns
        }

        // Check diagonals
        if (checkAndPlaceO( 0, isRow = false, isDiagonal = true, "O")) return 1 // Check main diagonal
        if (checkAndPlaceO( 1, isRow = false, isDiagonal = true, "O")) return  1// Check anti-diagonal
        return -1
    }
    private fun makeBlockingMove(): Int {
        // Check rows and columns to block "X"
        for (i in 0..2) {
            if (checkAndBlockX(i, isRow = true)) return 1 // Block in row
            if (checkAndBlockX(i, isRow = false)) return 1 // Block in column
        }

        // Check diagonals to block "X"
        if (checkAndBlockX(0, isRow = false, isDiagonal = true)) return 1  // Block in main diagonal
        if (checkAndBlockX(1, isRow = false, isDiagonal = true)) return 1  // Block in anti-diagonal

        return -1 // No blocking needed
    }

    private fun makeRandomMove() {
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
            message= "Player Wins!"
            jugador += 1
            textViewPoint2.text = jugador.toString()

        }else {
            message = "Computer Wins!"
            computador += 1
            textViewPoint6.text = computador.toString()
        }


        restartButton.text = message
        restartButton.isEnabled = true
    }

    private fun showTie() {
        restartButton.text = "Tie!"
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
            textViewTurn.text = "Player Turn"
        } else {
            currentPlayer = "O"
            textViewTurn.text = "Computer Turn"
            computerMove()
        }

        restartButton.text = "Restart"
        restartButton.isEnabled = false
    }
}
