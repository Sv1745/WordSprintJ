// game.js - Handles WordSprint Game Interface, Guesses, and Session Logout

document.addEventListener("DOMContentLoaded", function () {
    // Current active game state
    let currentGameId = null;
    let currentAttempt = 0;

    // DOM Elements
    const startGameButton = document.getElementById("start-game-button");
    const guessForm = document.getElementById("guess-form");
    const guessInput = document.getElementById("guess-input");
    const logoutButton = document.getElementById("logout-button");

    const attemptCountText = document.getElementById("attempt-count");
    const gameStatusText = document.getElementById("game-status");
    const errorMessage = document.getElementById("error-message");
    const successMessage = document.getElementById("success-message");
    const startSection = document.getElementById("start-section");

    // Clear alert messages
    function clearMessages() {
        if (errorMessage) {
            errorMessage.style.display = "none";
            errorMessage.textContent = "";
        }
        if (successMessage) {
            successMessage.style.display = "none";
            successMessage.textContent = "";
        }
    }

    // Display error message
    function showError(message) {
        clearMessages();
        if (errorMessage) {
            errorMessage.textContent = message;
            errorMessage.style.display = "block";
        }
    }

    // Display success message
    function showSuccess(message) {
        clearMessages();
        if (successMessage) {
            successMessage.textContent = message;
            successMessage.style.display = "block";
        }
    }

    // Reset the 6x5 game board grid
    function resetBoard() {
        const rows = document.querySelectorAll(".board-row");
        rows.forEach(row => {
            const tiles = row.querySelectorAll(".tile");
            tiles.forEach(tile => {
                tile.textContent = "";
                tile.className = "tile";
            });
        });
    }

    // Update a specific row on the board with guess letters and feedback colors
    function updateRow(rowNumber, guessWord, resultFeedback) {
        const row = document.querySelector(`.board-row[data-row="${rowNumber}"]`);
        if (!row) return;

        const tiles = row.querySelectorAll(".tile");
        for (let i = 0; i < 5; i++) {
            const letter = guessWord.charAt(i).toUpperCase();
            const feedbackChar = resultFeedback.charAt(i);

            tiles[i].textContent = letter;

            // Apply feedback class based on backend response character
            if (feedbackChar === 'G') {
                tiles[i].classList.add("tile-green");
            } else if (feedbackChar === 'Y') {
                tiles[i].classList.add("tile-yellow");
            } else {
                tiles[i].classList.add("tile-grey");
            }
        }
    }

    // Start a New Game
    async function startNewGame() {
        clearMessages();

        try {
            const response = await fetch("/wordsprint/game", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    action: "start"
                })
            });

            let data = {};
            try {
                data = await response.json();
            } catch (e) {
                // Response was not JSON (e.g. Tomcat HTML error page)
            }

            if (response.ok && data.success) {
                currentGameId = data.gameId;
                currentAttempt = 0;

                resetBoard();
                attemptCountText.textContent = "0";
                gameStatusText.textContent = "IN_PROGRESS";

                guessForm.style.display = "flex";
                startSection.style.display = "none";
                guessInput.value = "";
                guessInput.focus();

                showSuccess("Game started! Make your first guess.");
            } else if (response.status === 403) {
                showError(data.message || "Daily limit reached! You can only play a maximum of 3 games per day.");
            } else if (response.status === 400 || response.status === 401) {
                showError(data.message || "Please log in first to play WordSprint.");
                setTimeout(() => window.location.href = "login.html", 1500);
            } else {
                showError(data.message || "Unable to start game. Please log in first.");
            }
        } catch (error) {
            showError("Please log in first to play WordSprint.");
            setTimeout(() => window.location.href = "login.html", 1500);
        }
    }

    // Submit a Word Guess
    async function handleGuessSubmit(event) {
        event.preventDefault();
        clearMessages();

        if (!currentGameId) {
            showError("No active game found. Please click 'Start New Game'.");
            return;
        }

        const guessWord = guessInput.value.trim().toUpperCase();

        if (guessWord.length !== 5) {
            showError("Please enter a valid 5-letter word.");
            return;
        }

        currentAttempt++;

        try {
            const response = await fetch("/wordsprint/guess", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    gameId: currentGameId,
                    guess: guessWord,
                    guessNumber: currentAttempt
                })
            });

            let data = {};
            try {
                data = await response.json();
            } catch (e) {
                // Not JSON
            }

            if (response.ok && data.success) {
                updateRow(currentAttempt, guessWord, data.result);
                attemptCountText.textContent = currentAttempt;
                guessInput.value = "";

                if (data.status === "WON") {
                    gameStatusText.textContent = "WON";
                    showSuccess("🎉 Congratulations! You guessed the secret word!");
                    guessForm.style.display = "none";
                    startSection.style.display = "flex";
                    startGameButton.textContent = "Play Again";
                } else if (data.status === "LOST") {
                    gameStatusText.textContent = "LOST";
                    const target = data.targetWord ? ` The word was: ${data.targetWord}` : "";
                    showError(`❌ Game Over! You've used all 6 attempts.${target}`);
                    guessForm.style.display = "none";
                    startSection.style.display = "flex";
                    startGameButton.textContent = "Try Again";
                } else {
                    guessInput.focus();
                }
            } else {
                currentAttempt--; // Revert attempt count if request failed
                showError(data.message || "Failed to submit guess.");
            }
        } catch (error) {
            currentAttempt--;
            showError("Something went wrong on the server while submitting your guess.");
        }
    }

    // User Logout
    async function handleLogout() {
        try {
            await fetch("/wordsprint/logout", {
                method: "POST"
            });
            window.location.href = "login.html";
        } catch (error) {
            window.location.href = "login.html";
        }
    }

    // Attach Event Listeners
    if (startGameButton) {
        startGameButton.addEventListener("click", startNewGame);
    }

    if (guessForm) {
        guessForm.addEventListener("submit", handleGuessSubmit);
    }

    if (logoutButton) {
        logoutButton.addEventListener("click", handleLogout);
    }

    // Resume Game Handler
    async function resumeGame(gameId) {
        clearMessages();
        try {
            const response = await fetch(`/wordsprint/game?gameId=${gameId}`);
            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }

            const data = await response.json();
            if (response.ok && data.success) {
                currentGameId = data.gameId;
                currentAttempt = 0;
                resetBoard();

                if (data.guesses && data.guesses.length > 0) {
                    data.guesses.forEach(g => {
                        currentAttempt++;
                        updateRow(currentAttempt, g.guessedWord, g.result);
                    });
                }

                attemptCountText.textContent = currentAttempt;
                gameStatusText.textContent = data.status;

                if (data.status === "IN_PROGRESS") {
                    guessForm.style.display = "flex";
                    startSection.style.display = "none";
                    guessInput.value = "";
                    guessInput.focus();
                    showSuccess(`Resumed Game #${currentGameId}. ${6 - currentAttempt} attempts remaining.`);
                } else {
                    guessForm.style.display = "none";
                    startSection.style.display = "flex";
                    showError(`Game #${currentGameId} is already ${data.status}.`);
                }
            } else {
                showError(data.message || "Failed to load game details.");
            }
        } catch (error) {
            showError("Unable to load game details.");
        }
    }

    // Auto-check for ?gameId=X in URL to resume game
    const urlParams = new URLSearchParams(window.location.search);
    const paramGameId = urlParams.get("gameId");
    if (paramGameId) {
        resumeGame(paramGameId);
    }
});
