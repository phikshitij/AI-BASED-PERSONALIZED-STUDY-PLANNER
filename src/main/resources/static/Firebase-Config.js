// Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "",
  authDomain: "",
  projectId: "studymateai-582c7",
  storageBucket: "",
  messagingSenderId: "",
  appId: "",
  measurementId: ""
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Get auth and firestore instances
const auth = firebase.auth();
const db = firebase.firestore();

// Auth provider
const googleProvider = new firebase.auth.GoogleAuthProvider();
// Remove all custom scopes and parameters to simplify

// Function to sign in with Google
function signInWithGoogle() {
  // Check if running on a supported protocol
  if (window.location.protocol === 'file:') {
    console.error('Google login failed: Not supported on file:// protocol');
    // Return a rejected promise with a clear error
    return Promise.reject({
      code: 'auth/operation-not-supported-in-this-environment',
      message: 'Google Sign-In requires HTTP/HTTPS protocol. Please run the app on a web server.'
    });
  }
  
  // Use only popup for simplicity
  return auth.signInWithPopup(googleProvider)
    .then((result) => {
      // The signed-in user info
      const user = result.user;
      // Store user data in Firestore
      storeUserData(user);
      return user;
    })
    .catch((error) => {
      console.error('Google sign-in error:', error);
      throw error;
    });
}

// Function to sign up with email and password
function signUpWithEmailPassword(email, password, username) {
  return auth.createUserWithEmailAndPassword(email, password)
    .then((userCredential) => {
      // Signed up
      const user = userCredential.user;
      
      // Update user profile with username
      return user.updateProfile({
        displayName: username
      }).then(() => {
        // Store user data in Firestore
        storeUserData(user, { username });
        return user;
      });
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      console.error('Email/password sign-up error:', errorCode, errorMessage);
      throw error;
    });
}

// Function to sign in with email and password
function signInWithEmailPassword(email, password) {
  return auth.signInWithEmailAndPassword(email, password)
    .then((userCredential) => {
      // Signed in
      const user = userCredential.user;
      return user;
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      console.error('Email/password sign-in error:', errorCode, errorMessage);
      throw error;
    });
}

// Function to send password reset email
function sendPasswordResetEmail(email) {
  return auth.sendPasswordResetEmail(email)
    .then(() => {
      // Password reset email sent
      return true;
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      console.error('Password reset error:', errorCode, errorMessage);
      throw error;
    });
}

// Function to sign out
function signOut() {
  return auth.signOut()
    .then(() => {
      // Sign-out successful
      return true;
    })
    .catch((error) => {
      // An error happened
      console.error('Sign-out error:', error);
      throw error;
    });
}

// Function to store user data in Firestore
function storeUserData(user, additionalData = {}) {
  const userData = {
    uid: user.uid,
    email: user.email,
    displayName: user.displayName || additionalData.username || '',
    photoURL: user.photoURL || '',
    lastLogin: firebase.firestore.FieldValue.serverTimestamp(),
    ...additionalData
  };
  
  // Check if user document exists
  return db.collection('users').doc(user.uid).get()
    .then((doc) => {
      if (doc.exists) {
        // Update existing user
        return db.collection('users').doc(user.uid).update({
          lastLogin: firebase.firestore.FieldValue.serverTimestamp(),
          ...additionalData
        });
      } else {
        // Create new user
        userData.createdAt = firebase.firestore.FieldValue.serverTimestamp();
        return db.collection('users').doc(user.uid).set(userData);
      }
    })
    .catch((error) => {
      console.error('Error storing user data:', error);
    });
} 
