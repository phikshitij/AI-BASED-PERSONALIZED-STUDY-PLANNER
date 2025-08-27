// Variable Declaration
const loginBtn = document.querySelector("#login");
const registerBtn = document.querySelector("#register");
const loginForm = document.querySelector(".login-form");
const registerForm = document.querySelector(".register-form");
const usernameBox = document.querySelector(".username-box");
const forgotPassword = document.querySelector("#forgotPassword");
const rememberMeContainer = document.querySelector("#rememberMeContainer");
const rememberMeCheckbox = document.querySelector("#rememberMe");

// Form elements
const loginSubmitBtn = document.querySelector(".login-form .input-submit");
const registerSubmitBtn = document.querySelector(".register-form .input-submit");
const loginEmail = document.querySelector(".login-form .input-field[placeholder='Email']");
const loginPassword = document.querySelector(".login-form .input-field[placeholder='Password']");
const registerUsername = document.querySelector(".register-form .input-field[placeholder='Username']");
const registerEmail = document.querySelector(".register-form .input-field[placeholder='Email']");
const registerPassword = document.querySelector(".register-form .input-field[placeholder='Password']");

const googleLoginBtn = document.querySelector(".login-form .google-btn");
const googleRegisterBtn = document.querySelector(".register-form .google-btn");
const colContainer = document.querySelector(".col-2");

// Function to handle login button click (UI switch)
loginBtn.addEventListener("click", () => {
    loginBtn.style.backgroundColor = "#a855f7";
    registerBtn.style.backgroundColor = "rgba(255, 255, 255, 0.2)";
    loginForm.style.left = "50%";
    registerForm.style.left = "-50%";
    loginForm.style.opacity = "1";
    registerForm.style.opacity = "0";
    colContainer.style.borderRadius = "0 30% 20% 0";
    usernameBox.style.display = "none";
    forgotPassword.style.display = "flex";
    rememberMeContainer.style.display = "flex";
});

registerBtn.addEventListener("click", () => {
    registerBtn.style.backgroundColor = "#a855f7";
    loginBtn.style.backgroundColor = "rgba(255, 255, 255, 0.2)";
    loginForm.style.left = "150%";
    registerForm.style.left = "50%";
    loginForm.style.opacity = "0";
    registerForm.style.opacity = "1";
    colContainer.style.borderRadius = "0 20% 30% 0";
    usernameBox.style.display = "block";
    forgotPassword.style.display = "flex";
    rememberMeContainer.style.display = "none";
});

// Redirect function
function redirectToOnboarding() {
    window.location.href = 'Onboarding.html';
}

// Check if user has saved credentials
document.addEventListener('DOMContentLoaded', () => {
    firebase.auth().onAuthStateChanged((user) => {
        if (user) {
            // Store userId in localStorage for use in all API requests
            localStorage.setItem('userId', user.uid);
            console.log('User is logged in, userId set:', user.uid);
        } else {
            localStorage.removeItem('userId');
            console.log('User is logged out');
        }
    });
});


// Login submission
loginSubmitBtn.addEventListener('click', (e) => {
    e.preventDefault();
    const email = loginEmail.value.trim();
    const password = loginPassword.value.trim();
    if (!email || !password) {
        alert('Please fill in all fields');
        return;
    }
    signInWithEmailPassword(email, password)
        .then((userCredential) => {
            // Store userId in localStorage
            if (userCredential && userCredential.user && userCredential.user.uid) {
                localStorage.setItem('userId', userCredential.user.uid);
                console.log('Login: userId set:', userCredential.user.uid);
            }
            redirectToOnboarding();
        })
        .catch((error) => {
            alert(`Login failed: ${error.message}`);
        });
});

// Registration submission
registerSubmitBtn.addEventListener('click', (e) => {
    e.preventDefault();
    const username = registerUsername.value.trim();
    const email = registerEmail.value.trim();
    const password = registerPassword.value.trim();
    if (!username || !email || !password) {
        alert('Please fill in all fields');
        return;
    }
    signUpWithEmailPassword(email, password, username)
        .then((userCredential) => {
            // Store userId in localStorage
            if (userCredential && userCredential.user && userCredential.user.uid) {
                localStorage.setItem('userId', userCredential.user.uid);
                console.log('Registration: userId set:', userCredential.user.uid);
            }
            alert(`Registration successful`);
            redirectToOnboarding();
        })
        .catch((error) => {
            alert(`Registration failed: ${error.message}`);
        });
});

// Google login
googleLoginBtn.addEventListener('click', () => {
    signInWithGoogle()
        .then(() => {
            redirectToOnboarding();
        })
        .catch((error) => {
            alert(`Google login failed: ${error.message}`);
        });
});

// Google register
googleRegisterBtn.addEventListener('click', () => {
    signInWithGoogle()
        .then(() => {
            redirectToOnboarding();
        })
        .catch((error) => {
            alert(`Google login failed: ${error.message}`);
        });
});
