// profile.js - Handles User Profile View, Profile Edit Form, and Full Game History

document.addEventListener("DOMContentLoaded", function () {
    const profileUname = document.getElementById("profile-uname");
    const profileRole = document.getElementById("profile-role");
    const profileCreated = document.getElementById("profile-created");

    const editProfileForm = document.getElementById("edit-profile-form");
    const editUsernameInput = document.getElementById("edit-username");
    const editPasswordInput = document.getElementById("edit-password");

    const allGamesList = document.getElementById("all-games-list");
    const logoutButton = document.getElementById("logout-button");

    const errorMessage = document.getElementById("error-message");
    const successMessage = document.getElementById("success-message");

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

    function showError(msg) {
        clearMessages();
        if (errorMessage) {
            errorMessage.textContent = msg;
            errorMessage.style.display = "block";
        }
    }

    function showSuccess(msg) {
        clearMessages();
        if (successMessage) {
            successMessage.textContent = msg;
            successMessage.style.display = "block";
        }
    }

    async function loadProfile() {
        try {
            const response = await fetch("/wordsprint/profile");
            if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }

            const data = await response.json();
            if (!response.ok || !data.success) {
                showError(data.message || "Failed to load profile.");
                return;
            }

            profileUname.textContent = data.username;
            profileRole.textContent = data.role;
            profileCreated.textContent = new Date(data.createdAt).toLocaleDateString();

            // Render All Games History
            if (!data.games || data.games.length === 0) {
                allGamesList.innerHTML = `<p style="color: #6c757d;">No games played yet.</p>`;
            } else {
                let html = `
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Game ID</th>
                                <th>Started At</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                `;

                data.games.forEach(g => {
                    const startedDate = new Date(g.startedAt).toLocaleString();
                    let badgeClass = "badge-progress";
                    if (g.status === "WON") badgeClass = "badge-won";
                    if (g.status === "LOST") badgeClass = "badge-lost";

                    let actionHtml = "-";
                    if (g.status === "IN_PROGRESS") {
                        actionHtml = `<a href="game.html?gameId=${g.gameId}" class="btn btn-primary btn-sm">▶ Resume</a>`;
                    }

                    html += `
                        <tr>
                            <td>#${g.gameId}</td>
                            <td>${startedDate}</td>
                            <td><span class="badge ${badgeClass}">${g.status}</span></td>
                            <td>${actionHtml}</td>
                        </tr>
                    `;
                });

                html += `</tbody></table>`;
                allGamesList.innerHTML = html;
            }

        } catch (error) {
            showError("Unable to load profile data.");
        }
    }

    if (editProfileForm) {
        editProfileForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            clearMessages();

            const newUsername = editUsernameInput.value.trim();
            const newPassword = editPasswordInput.value.trim();

            if (!newUsername && !newPassword) {
                showError("Please enter a new username or password to update.");
                return;
            }

            try {
                const response = await fetch("/wordsprint/profile", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: new URLSearchParams({
                        username: newUsername,
                        password: newPassword
                    })
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showSuccess(data.message || "Profile updated successfully!");
                    editUsernameInput.value = "";
                    editPasswordInput.value = "";
                    loadProfile();
                } else {
                    showError(data.message || "Failed to update profile.");
                }
            } catch (error) {
                showError("Something went wrong on the server while updating profile.");
            }
        });
    }

    if (logoutButton) {
        logoutButton.addEventListener("click", async function () {
            try {
                await fetch("/wordsprint/logout", { method: "POST" });
            } finally {
                window.location.href = "login.html";
            }
        });
    }

    loadProfile();
});
