package com.example.triqui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.example.triqui.ui.OnlineGameActivity

class MainActivity : ComponentActivity() {


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
    lateinit var sharedPreferences : SharedPreferences

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
    private lateinit var computerMoveHandler: Handler
    private var computerMoveRunnable: Runnable? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ticTacToeGame = TicTacToeGame()
        boardView = findViewById(R.id.board) // Ensure this is done after setContentView

        Log.d("MainActivity", "Board View Initialized: $boardView")
        boardView.initialize("Solo") //
        boardView.setGame(ticTacToeGame)
        boardView.setOnTouchListener(mTouchListener(this))

        // Initialize views
        gridLayoutPoints = findViewById(R.id.gridLayoutPoints)

        textViewTurn = findViewById(R.id.textViewTurn)
        textViewPoint2 = findViewById(R.id.textViewPoint2)
        textViewPoint4 = findViewById(R.id.textViewPoint4)
        textViewPoint6 = findViewById(R.id.textViewPoint6)
        humanMediaPlayer = MediaPlayer.create(this,R.raw.player_sound)
        computerMediaPlayer = MediaPlayer.create(this, R.raw.computer_sound)
        muteButton = findViewById(R.id.muteButton)
        computerMoveHandler = Handler(mainLooper)

        sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        jugador = sharedPreferences.getInt("humanWins", 0)
        computador = sharedPreferences.getInt("computerWins", 0)
        empate= sharedPreferences.getInt("ties", 0)
        textViewPoint4.text = empate.toString()
        textViewPoint2.text = jugador.toString()
        textViewPoint6.text = computador.toString()
        currentDifficulty = DifficultyLevel.valueOf( sharedPreferences.getString("difficulty","Expert") ?: "Expert")
        ticTacToeGame.setDifficulty(currentDifficulty.toString())
        Log.d("MainActivity", "Current Player: $currentPlayer")
        currentPlayer = "X"
        textViewTurn.text = "It's your Turn"

        muteButton.setOnClickListener {
            toggleMute()
        }
        if (savedInstanceState != null) {
            ticTacToeGame.setBoardState(savedInstanceState.getStringArray("board"))
            Log.d("MainActivity", "Game State: ${ticTacToeGame.getBoardState().contentToString()}")
            gameOver = savedInstanceState.getBoolean("gameOver")
            textViewTurn.setText(savedInstanceState.getCharSequence("info"))
            currentPlayer = savedInstanceState.getString("currentPlayer") ?: "X"
            ticTacToeGame.currentPlayer = currentPlayer
            isPlayerTurn = savedInstanceState.getBoolean("isPlayerTurn", true)



        }
        loadMutePreference()

        if (!isPlayerTurn && !gameOver) {
            // Reschedule the computer move if it was the computer's turn
            textViewTurn.text = "Computer's turn."
            makeComputerMove()
        }

    }


    private fun toggleMute() {
        val editor = sharedPreferences.edit()
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
        editor.putBoolean("isMuted", isMuted)
        editor.apply()
    }
    private fun loadMutePreference() {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        isMuted = sharedPreferences.getBoolean("isMuted", false)

        if (isMuted) {
            humanMediaPlayer.setVolume(0f, 0f)
            computerMediaPlayer.setVolume(0f, 0f)
            muteButton.text = "Unmute"
        } else {
            humanMediaPlayer.setVolume(1f, 1f)
            computerMediaPlayer.setVolume(1f, 1f)
            muteButton.text = "Mute"
        }
    }
    override fun onStop() {
        super.onStop()
        cancelPendingComputerMove()
        val ed: SharedPreferences.Editor = sharedPreferences.edit()
        ed.putInt("humanWins", jugador)
        ed.putInt("computerWins", computador)
        ed.putInt("ties", empate)
        ed.putString("difficulty", currentDifficulty.name)
        ed.apply()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cancelPendingComputerMove()
        outState.putStringArray("board", ticTacToeGame.getBoardState())
        outState.putBoolean("gameOver", gameOver)
        outState.putCharSequence("info", textViewTurn.getText())
        outState.putString("currentPlayer", ticTacToeGame.currentPlayer)
        outState.putBoolean("isPlayerTurn", isPlayerTurn)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        menuItemDifficulty = menu?.findItem(R.id.menu_difficulty)!!
        updateDifficultyMenuItem() // Update the difficulty in the menu
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPendingComputerMove()
        humanMediaPlayer.release()
        computerMediaPlayer.release()

    }
    private var humanMediaPlayerReleased = false
    private var computerMediaPlayerReleased = false
    override fun onPause() {
        super.onPause()
        if (!humanMediaPlayerReleased) {
            humanMediaPlayer.release()
            humanMediaPlayerReleased = true
        }
        if (!computerMediaPlayerReleased) {
            computerMediaPlayer.release()
            computerMediaPlayerReleased = true
        }
    }
    override fun onResume() {
        super.onResume()
        if (humanMediaPlayerReleased) {
            humanMediaPlayer = MediaPlayer.create(this, R.raw.player_sound)
            humanMediaPlayerReleased = false
        }
        if (computerMediaPlayerReleased) {
            computerMediaPlayer = MediaPlayer.create(this, R.raw.computer_sound)
            computerMediaPlayerReleased = false
        }
    }


    // Handle menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_new_game -> {
                restartGame()
                // Restart the game
                true
            }
            R.id.menu_difficulty -> {
                // Toggle between difficulty levels
                showDifficultyDialog()
                updateDifficultyMenuItem()// Show difficulty options
                true
            }
            R.id.online_game ->{
                navigateToOnlinePlay()
                true
            }
            R.id.menu_reset -> {
                jugador = 0
                empate = 0
                computador =0
                textViewPoint4.text = empate.toString()
                textViewPoint2.text = jugador.toString()
                textViewPoint6.text = computador.toString()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToOnlinePlay() {
        val intent = Intent(this, OnlineGameActivity::class.java)
        startActivity(intent)
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
                        ticTacToeGame.setDifficulty(Easy.toString())
                        currentDifficulty = DifficultyLevel.Easy
                        updateDifficultyMenuItem()
                    }
                    1 -> {
                        ticTacToeGame.setDifficulty(Harder.toString())
                        currentDifficulty = DifficultyLevel.Harder
                        updateDifficultyMenuItem()
                    }

                    2 -> {
                        ticTacToeGame.setDifficulty(Expert.toString())
                        currentDifficulty = DifficultyLevel.Expert
                        updateDifficultyMenuItem()
                    }
                }
            }
        builder.create().show()
    }


    private fun showWinner(winner: String) {
        gameOver= true

        if (winner == "X") {
            textViewTurn.text= "Player Wins!"
            jugador += 1
            textViewPoint2.text = jugador.toString()

        }else {
            textViewTurn.text = "Computer Wins!"
            computador += 1
            textViewPoint6.text = computador.toString()
        }


    }

    private fun showTie() {
        gameOver=true
        empate += 1
        textViewPoint4.text = empate.toString()
        textViewTurn.text = "Tie!"

    }

    private fun restartGame() {
        cancelPendingComputerMove()
        ticTacToeGame.restartGame()
        boardView.invalidate()
        isPlayerTurn = false
        gameOver=false
        // Reset the board


        if(ticTacToeGame.currentPlayer=="X"){
            textViewTurn.text="Your turn."
            isPlayerTurn = true
        }else{
            textViewTurn.text ="Computers turn."

            computerMoveHandler.postDelayed({
                if (!gameOver) { // Ensure game is not over before making a move
                    makeComputerMove()
                }
            }, 800)

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
                        }, 800)
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
        cancelPendingComputerMove()
        computerMoveRunnable = Runnable {
            if (!gameOver) { // Ensure game is not over
                ticTacToeGame.computerMove()
                computerMediaPlayer.start()

                boardView.invalidate()
                val winner = ticTacToeGame.checkWinner()

                if (winner.isNotEmpty()) {
                    handleGameOver(winner)
                } else if (ticTacToeGame.isBoardFull()) {
                    showTie()
                } else {
                    textViewTurn.text = "Your turn."
                    isPlayerTurn = true
                }
            }
        }
        computerMoveHandler.postDelayed(computerMoveRunnable!!, 800)
    }

    private fun cancelPendingComputerMove() {
        computerMoveRunnable?.let {
            computerMoveHandler.removeCallbacks(it)
            computerMoveRunnable = null
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

    }


}








