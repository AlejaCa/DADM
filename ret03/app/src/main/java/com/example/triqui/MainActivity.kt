package com.example.triqui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.triqui.TicTacToeGame.DifficultyLevel.Easy
import com.example.triqui.TicTacToeGame.DifficultyLevel.Expert
import com.example.triqui.TicTacToeGame.DifficultyLevel.Harder


class MainActivity : ComponentActivity() {

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
    private var gameOver: Boolean = false
    private var isMuted = false // To track mute status
    private lateinit var muteButton: Button
    private var isPlayerTurn = true

    enum class DifficultyLevel {
        Easy, Harder, Expert
    }
    // Current difficulty level
    private var mDifficultyLevel = DifficultyLevel.Expert
    private var aDifficultyLevel = DifficultyLevel.Expert
    private lateinit var currentDifficulty: DifficultyLevel
    private lateinit var menuItemDifficulty: MenuItem
    private lateinit var boardView: BoardView
    private lateinit var ticTacToeGame: TicTacToeGame
    private lateinit var humanMediaPlayer: MediaPlayer
    private lateinit var computerMediaPlayer: MediaPlayer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ticTacToeGame = TicTacToeGame()
        boardView = findViewById(R.id.board) // Ensure this is done after setContentView

        // Now you can safely interact with boardView
        boardView.initialize() //
        boardView.setGame(ticTacToeGame)
        boardView.setOnTouchListener(mTouchListener(this))

        // Initialize views
        gridLayoutPoints = findViewById(R.id.gridLayoutPoints)
        restartButton = findViewById(R.id.restartButton)
        textViewTurn = findViewById(R.id.textViewTurn)
        textViewPoint2 = findViewById(R.id.textViewPoint2)
        textViewPoint4 = findViewById(R.id.textViewPoint4)
        textViewPoint6 = findViewById(R.id.textViewPoint6)
        humanMediaPlayer = MediaPlayer.create(this,R.raw.player_sound)
        computerMediaPlayer = MediaPlayer.create(this, R.raw.computer_sound)
        muteButton = findViewById(R.id.muteButton)
        // Set up restart button click listener
        restartButton.setOnClickListener {
            restartGame()
        }

        currentDifficulty = DifficultyLevel.Expert
        currentPlayer = "X"
        textViewTurn.text = "It's your Turn"
        muteButton.setOnClickListener {
            toggleMute()
        }

    }

    private fun toggleMute() {
        if (isMuted) {
            // Unmute by restoring the volume
            humanMediaPlayer.setVolume(1f, 1f)
            computerMediaPlayer.setVolume(1f, 1f)
            muteButton.text = "Mute"
        } else {

            humanMediaPlayer.setVolume(0f, 0f)
            computerMediaPlayer.setVolume(0f, 0f)
            muteButton.text = "Unmute"
        }

        // Toggle the mute status
        isMuted = !isMuted
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        menuItemDifficulty = menu?.findItem(R.id.menu_difficulty)!!
        updateDifficultyMenuItem() // Update the difficulty in the menu
        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        // Release the media player when the activity is destroyed
        humanMediaPlayer.release()
        computerMediaPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        humanMediaPlayer.release()
        computerMediaPlayer.release()
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Difficulty")
            .setItems(difficulties) { dialog, which ->
                // Set the difficulty level based on user selection
                when (which) {
                    0 -> {
                        ticTacToeGame.setDifficulty(Easy)
                        currentDifficulty = DifficultyLevel.Easy
                        updateDifficultyMenuItem()
                    }
                    1 -> {
                        ticTacToeGame.setDifficulty(Harder)
                        currentDifficulty = DifficultyLevel.Harder
                        updateDifficultyMenuItem()
                    }

                    2 -> {
                        ticTacToeGame.setDifficulty(Expert)
                        currentDifficulty = DifficultyLevel.Expert
                        updateDifficultyMenuItem()
                    }
                }
            }
        builder.create().show()
    }


    private fun showWinner(winner: String) {
        gameOver= true
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
        gameOver=true
        restartButton.text = "Tie!"
        empate += 1
        textViewPoint4.text = empate.toString()
        restartButton.isEnabled = true
    }

    private fun restartGame() {
        ticTacToeGame.restartGame()
        boardView.invalidate()
        isPlayerTurn = false
        gameOver=false
        // Reset the board

        restartButton.text = "Restart"
        restartButton.isEnabled = false
        if(ticTacToeGame.currentPlayer=="X"){
            textViewTurn.text="Your turn."
            isPlayerTurn = true
        }else{
            textViewTurn.text ="Computers turn."

            Handler(mainLooper).postDelayed({
                makeComputerMove()
            }, 1000)

        }
    }
    private class mTouchListener(val game: MainActivity) : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (game.gameOver || !game.isPlayerTurn) return false

            val col = event?.x?.toInt()?.div(game.boardView.getBoardCellWidth())
            val row = event?.y?.toInt()?.div(game.boardView.getBoardCellHeight())
            val pos = col?.let { (row?.times(3))?.plus(it) }

            if (pos != null) {
                // Human player makes a move
                if (game.ticTacToeGame.setMove("X", pos)) {
                    game.boardView.invalidate()
                    game.humanMediaPlayer.start()

                    val winner = game.ticTacToeGame.checkWinner()
                    if (winner == "") {
                        // Proceed to computer's turn after human move
                        game.textViewTurn.text = "Computer's turn."
                        game.isPlayerTurn = false
                        Handler(game.mainLooper).postDelayed({
                            game.makeComputerMove()
                        }, 1000)
                    } else {
                        game.handleGameOver(winner)
                    }
                }
            }
            return true
        }
    }



    private fun setMove(player: String, location: Int): Boolean {
        if (ticTacToeGame.setMove(player, location)) {
            boardView.invalidate() // Redraw the board
            return true
        }
        return false
    }
    private fun makeComputerMove() {
        // Make the computer move
        ticTacToeGame.computerMove()
        computerMediaPlayer.start()

        // Update the board and check for the winner
        boardView.invalidate()
        val winner = ticTacToeGame.checkWinner()

        // Handle winner or tie after computer move
        when (winner) {
            "X" -> showWinner("X")
            "O" -> showWinner("O")
            else -> {
                if (ticTacToeGame.isBoardFull()) {
                    showTie()
                    isPlayerTurn = false
                } else {
                    textViewTurn.text = "Your turn."
                    isPlayerTurn = true
                }
            }
        }
    }
    private fun handleGameOver(winner: String) {
        gameOver = true
        isPlayerTurn = false
        when (winner) {
            "X" -> {
                textViewTurn.text = "You won!"
                jugador += 1
                textViewPoint2.text = jugador.toString()
            }
            "O" -> {
                textViewTurn.text = "Computer won!"
                computador += 1
                textViewPoint6.text = computador.toString()
            }
            else -> {
                textViewTurn.text = "Tie!"
                empate += 1
                textViewPoint4.text = empate.toString()
            }
        }
        restartButton.text = "Restart"
        restartButton.isEnabled = true
    }


}








