package com.example.flashcard1

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    var currentCardDisplayedIndex = 0
    lateinit var flashcardDatabase: FlashcardDatabase
    private var allFlashcards = mutableListOf<Flashcard>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        flashcardDatabase = FlashcardDatabase(this)
        flashcardDatabase.initFirstCard()
        allFlashcards = flashcardDatabase.getAllCards().toMutableList()

        val questionTextView = findViewById<TextView>(R.id.flashcard_question)
        val answerTextView = findViewById<TextView>(R.id.flashcard_reponse)
        val wrongAnswer1TextView = findViewById<TextView>(R.id.flashcard_reponse2)
        val wrongAnswer2TextView = findViewById<TextView>(R.id.flashcard_reponse3)
        val toggleButton = findViewById<ImageView>(R.id.toggle123) // Renommé le bouton toggle123 en toggleButton
        val nextButton = findViewById<ImageView>(R.id.next_button)
        val editButton = findViewById<ImageView>(R.id.edit_bouton) // Renommé le bouton edit_bouton en editButton
        val deleteButton = findViewById<ImageView>(R.id.delete_bouton)

        // Définition de l'action du bouton "Suivant"
        nextButton.setOnClickListener {
            if (allFlashcards.isEmpty()) {
                return@setOnClickListener  // Il n'y a pas de cartes à afficher
            }

            currentCardDisplayedIndex++

            if (currentCardDisplayedIndex >= allFlashcards.size) {
                currentCardDisplayedIndex = 0  // Revenir à la première carte si nous avons atteint la fin
            }

            val (question, answer, wrongAnswer1, wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]

            // Mettre à jour les TextViews avec la nouvelle carte
            questionTextView.text = question
            answerTextView.text = answer
            wrongAnswer1TextView.text = wrongAnswer1
            wrongAnswer2TextView.text = wrongAnswer2
        }

        // Définition de l'action du bouton "Supprimer"
        deleteButton.setOnClickListener {
            val currentQuestion = questionTextView.text.toString()
            flashcardDatabase.deleteCard(currentQuestion)

            // Mettre à jour la liste des flashcards
            allFlashcards = flashcardDatabase.getAllCards().toMutableList()

            // Vérifier s'il reste des cartes
            if (allFlashcards.isNotEmpty()) {
                // Afficher la carte précédente (si disponible)
                currentCardDisplayedIndex = max(0, currentCardDisplayedIndex - 1)
                val (question, answer, wrongAnswer1, wrongAnswer2) = allFlashcards[currentCardDisplayedIndex]
                questionTextView.text = question
                answerTextView.text = answer
                wrongAnswer1TextView.text = wrongAnswer1
                wrongAnswer2TextView.text = wrongAnswer2
            } else {
                // S'il n'y a plus de cartes, afficher un état vide
                questionTextView.text = ""
                answerTextView.text = ""
                wrongAnswer1TextView.text = ""
                wrongAnswer2TextView.text = ""
            }
        }

        // Définition de l'action du bouton "Ajouter/Modifier"
        toggleButton.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            resultLauncher.launch(intent)
        }

        // Définition de l'action du bouton "Éditer"
        editButton.setOnClickListener {
            val question = questionTextView.text.toString()

            if (question.isNotEmpty()) {
                // Vérifier si une question est affichée, puis lancer MainActivity2 en mode édition
                val intent = Intent(this, MainActivity2::class.java)
                intent.putExtra("questionToEdit", question)
                resultLauncher.launch(intent)
            } else {
                // Aucune question affichée, afficher un message d'erreur
                Snackbar.make(editButton, "Aucune question à éditer.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // Déclaration du launcher pour gérer le résultat de MainActivity2
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val extras = data?.extras

        if (extras != null) { // Check that we have data returned
            val question = extras.getString("question")
            val answer = extras.getString("answer")
            val wrongAnswer1 = extras.getString("wrongAnswer1")
            val wrongAnswer2 = extras.getString("wrongAnswer2")

            // Display newly created flashcard
            findViewById<TextView>(R.id.flashcard_question).text = question
            findViewById<TextView>(R.id.flashcard_reponse).text = answer
            findViewById<TextView>(R.id.flashcard_reponse2).text = wrongAnswer1
            findViewById<TextView>(R.id.flashcard_reponse3).text = wrongAnswer2

            // Save newly created flashcard to database
            if (question != null && answer != null && wrongAnswer1 != null && wrongAnswer2 != null) {
                flashcardDatabase.insertCard(
                    Flashcard(
                        question,
                        answer,
                        wrongAnswer1,
                        wrongAnswer2
                    )
                )
                // Update set of flashcards to include new card
                allFlashcards = flashcardDatabase.getAllCards().toMutableList()
            } else {
                Log.e(
                    "TAG",
                    "Missing question or answer to input into database. Question is $question and answer is $answer and wrongAnswer1 is $wrongAnswer1 and wrongAnswer2 is $wrongAnswer2"
                )
            }
        } else {
            Log.i("MainActivity", "Returned null data from MainActivity2")
        }
    }
}
