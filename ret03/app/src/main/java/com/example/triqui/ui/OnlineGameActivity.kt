package com.example.triqui.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triqui.BoardView

import com.example.triqui.R
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class OnlineGameActivity : ComponentActivity() {

    private lateinit var recyclerViewGames: RecyclerView
    private lateinit var buttonHostGame: Button
    private lateinit var textGameStatus: TextView
    private lateinit var boardView: BoardView
    private val database = Firebase.database
    private val gamesRef = database.getReference("games")
    private val gamesList = mutableListOf<Game>()
    private lateinit var adapter: GamesAdapter
    private var mboard = List(9) { "" }
    private var isGameOver = false
    private val playerId = RandomStringUtils() // Generate a unique ID for this session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_play)

        // Initialize views
        recyclerViewGames = findViewById(R.id.recyclerViewGames)
        buttonHostGame = findViewById(R.id.buttonHostGame)
        buttonHostGame.isEnabled
        textGameStatus = findViewById(R.id.textGameStatus)
        boardView = findViewById(R.id.board)
        boardView.initialize("online") //

         // Pass the correct game key
        val game = Game()
        val currentPlayer = game.turn
        val gameKey = game.key
        if (gameKey != null) {
            observeGameUpdates(gameKey, currentPlayer)
        }
        boardView.setOnTouchListener(gameKey?.let {
            mTouchListener(
                mboard.toMutableList(),
                game,
                it,
                currentPlayer,
                boardView
            ) { position ->
                makeMove(gameKey, position, currentPlayer)
            }
        })
        boardView.setBoard(game.board)
        boardView.invalidate()
        // Set up RecyclerView
        adapter = GamesAdapter(playerId,gamesList) { game -> joinGame(game) }
        recyclerViewGames.layoutManager = LinearLayoutManager(this)
        recyclerViewGames.adapter = adapter
        val sharedPref = getSharedPreferences("playerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("playerId", playerId)
        editor.apply()
        // Button to host a game
        buttonHostGame.setOnClickListener {
            hostGame(playerId) // Replace "Player1" with the actual player name
        }

        // Fetch available games
        fetchAvailableGames()
    }

    private fun hostGame(playerName: String) {
        val newGame = gamesRef.push()
        val game = Game(host = playerName, opponent = "", board = List(9) { "" }, turn = "X", winner = "")

        newGame.setValue(game)
            .addOnSuccessListener {
                textGameStatus.text = "Game hosted successfully! Waiting for an opponent..."
                textGameStatus.visibility = View.VISIBLE
                observeGameUpdates(newGame.key!!, "X") // Pass "X" for host
            }
            .addOnFailureListener { e ->
                textGameStatus.text = "Failed to host game. Please try again."
                textGameStatus.visibility = View.VISIBLE
            }
    }


    private fun fetchAvailableGames() {
        gamesRef.orderByChild("opponent").equalTo("")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gamesList.clear()
                    for (child in snapshot.children) {
                        val game = child.getValue(Game::class.java)?.apply {
                            key = child.key
                        }
                        if (game != null) gamesList.add(game)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OnlinePlay", "Error fetching games: ${error.message}")
                }
            })
    }

    private fun joinGame(game: Game) {
        if (game.key == null) return

        gamesRef.child(game.key!!).child("opponent").setValue(playerId) // Replace with actual player name
            .addOnSuccessListener {
                textGameStatus.text = "Joined game! Waiting for your turn..."
                textGameStatus.visibility = View.VISIBLE
                observeGameUpdates(game.key!!, "O") // Pass "O" for opponent
            }
            .addOnFailureListener { e ->
                textGameStatus.text = "Failed to join game. Please try again."
                textGameStatus.visibility = View.VISIBLE
            }
        buttonHostGame.isEnabled = false
    }


    private fun observeGameUpdates(gameKey: String, currentPlayer: String) {
        val gameRef = gamesRef.child(gameKey)

        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val game = snapshot.getValue(Game::class.java)
                    game?.let {
                        mboard = it.board // Update local board state
                        boardView.setBoard(mboard) // Update the BoardView
                        boardView.invalidate() // Redraw the board
                        isGameOver = it.winner.isNotEmpty() || it.board.all { cell -> cell.isNotEmpty() }
                        buttonHostGame.isEnabled = isGameOver
                        // Attach touch listener after the board is updated
                        if (it.opponent.isNotEmpty()) {
                            textGameStatus.text = "Game started! It's ${it.turn}'s turn."
                            boardView.setOnTouchListener(
                                mTouchListener(
                                    mboard.toMutableList(),
                                    game,
                                    gameKey,
                                    currentPlayer,
                                    boardView
                                ) { position ->
                                    makeMove(gameKey, position, currentPlayer)
                                }
                            )
                        } else {
                            textGameStatus.text = "Waiting for an opponent to join..."
                        }


                        textGameStatus.text = if (it.winner.isEmpty()) {
                            if (it.turn == currentPlayer) "Your turn!" else "Waiting for opponent..."
                        } else {
                            "Game Over! Winner: ${it.winner}"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlinePlay", "Error observing game updates", error.toException())
            }
        })
    }




    private fun makeMove(gameKey: String, position: Int, currentPlayer: String) {
        val gameRef = gamesRef.child(gameKey)

        gameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val gameData = mutableData.getValue(Game::class.java) ?: return Transaction.success(mutableData)

                // Validate the move
                if (gameData.turn != currentPlayer || gameData.board[position].isNotEmpty()) {
                    Log.e("OnlinePlay", "Invalid move: Not your turn or position already occupied.")
                    return Transaction.abort() // Invalid move
                }

                // Make the move
                val updatedBoard = gameData.board.toMutableList()
                updatedBoard[position] = currentPlayer
                gameData.board = updatedBoard
                gameData.turn = if (currentPlayer == "X") "O" else "X"
                gameData.winner = checkForWinner(updatedBoard)

                mutableData.value = gameData
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("OnlinePlay", "Error making move: ${error.message}", error.toException())
                } else if (!committed) {
                    Log.e("OnlinePlay", "Move transaction not committed.")
                } else {
                    Log.d("OnlinePlay", "Move successfully made.")
                }
            }
        })
    }


    private fun checkForWinner(board: List<String>): String {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (pattern in winPatterns) {
            val (a, b, c) = pattern
            if (board[a] == board[b] && board[b] == board[c] && board[a].isNotEmpty()) {
                return board[a] // Return "X" or "O"
            }
        }

        return if (board.all { it.isNotEmpty() }) "Tie" else "" // Return "Tie" or ""
    }

    private class mTouchListener(
        val board: MutableList<String>,
        val game: Game,
        val gameKey: String,
        val currentPlayer: String, // "X" or "O" for the player
        val boardView: BoardView,
        val makeMoveCallback: (Int) -> Unit // Callback to perform a move
    ) : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (game.winner.isNotEmpty() || game.turn != currentPlayer ||
                game.opponent.isEmpty()) return false // Validate turn or winner

            val col = event?.x?.toInt()?.div(boardView.getBoardCellWidth())
            val row = event?.y?.toInt()?.div(boardView.getBoardCellHeight())
            val pos = col?.let { (row?.times(3))?.plus(it) }

            if (pos != null && board[pos].isEmpty()) {
                makeMoveCallback(pos) // Trigger move callback
            }
            return true
        }
    }






    private fun updateGameUI(game: Game?) {
        game?.let {
            textGameStatus.text = "Game in progress. It's ${it.turn}'s turn."
            if (it.winner.isNotEmpty()) {
                textGameStatus.text = "Game over! Winner: ${it.winner}"
            }
        }
    }
}

fun RandomStringUtils(): String {
    val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val randomString: String = List(5) { alphabet.random() }.joinToString("")
    return randomString
}


class GamesAdapter(
    val playerName: String,
    private val games: List<Game>,
    private val onGameClick: (Game)  -> Unit
    ) : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

        class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textGameInfo: TextView = itemView.findViewById(R.id.textGameInfo)
            val buttonJoin: Button = itemView.findViewById(R.id.buttonJoin)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
            return GameViewHolder(view)
        }

        override fun onBindViewHolder(holder: GameViewHolder, position: Int ) {
            val game = games[position]
            holder.textGameInfo.text = "Host: ${game.host}, Turn: ${game.turn}"
            if (game.host != playerName) {
                holder.buttonJoin.visibility = View.VISIBLE
            } else {
                holder.buttonJoin.visibility = View.GONE
            }
            holder.buttonJoin.setOnClickListener {
                onGameClick(game)
            }
        }

        override fun getItemCount() = games.size

}
data class Game(
    var host: String = "",
    var opponent: String = "",
    var board: List<String> = listOf(),
    var turn: String = "",
    var winner: String = "",
    var key: String? = null // Firebase key for identifying the game
)


