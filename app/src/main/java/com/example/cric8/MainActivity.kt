package com.example.cric8
//
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import com.google.firebase.Timestamp
import androidx.compose.runtime.mutableStateListOf
import androidx.credentials.*
import androidx.lifecycle.lifecycleScope
import com.example.cric8.datastore.UserPreferences
import com.example.cric8.ui.theme.Cric8Theme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    companion object {
        const val ACCESS_CODE = "1234" // Your family access code here
        private const val TAG = "MainActivity"
    }
    private val currentMessagesList = mutableStateListOf<ChatMessage>()

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        val prefs = UserPreferences(this)

        lifecycleScope.launch {
            prefs.isLoggedIn.collect { loggedIn ->
                setContent {
                    Cric8Theme {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            if (loggedIn && auth.currentUser != null) {
                                startListeningForMessages() // Start listening here

                                AppNavigation(
                                    onLogout = { logoutUser() },
                                    sendMessage = { text, imageUrl -> sendMessage(text, imageUrl) },
                                    messages = currentMessagesList,
                                    editMessage = { message, newText -> editMessage(message, newText) },
                                    deleteMessage = { message -> deleteMessage(message) }
                                )
                            } else {
                                SplashScreen(
                                    onGoogleSignInClick = { signInWithGoogle() },
                                    onGitHubLoginClick = { launchGitHubLogin() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startListeningForMessages() {
        listenToMessages { messages ->
            currentMessagesList.clear()
            currentMessagesList.addAll(messages)
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                handleSignIn(result.credential)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleCredential.idToken)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val name = user?.displayName ?: ""
                    val email = user?.email ?: ""

                    Log.d("GoogleSignIn", "success: $name <$email>")

                    lifecycleScope.launch {
                        val prefs = UserPreferences(this@MainActivity)
                        prefs.setLoggedIn(true)
                        prefs.saveUserInfo(email, name)
                    }

                    reloadHome()
                } else {
                    Log.w("GoogleSignIn", "failure", task.exception)
                }
            }
    }

    private fun launchGitHubLogin() {
        val provider = OAuthProvider.newBuilder("github.com")
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending
                .addOnSuccessListener { handleGitHubResult(it) }
                .addOnFailureListener { Log.e("GitHubLogin", "Pending error", it) }
        } else {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { handleGitHubResult(it) }
                .addOnFailureListener { Log.e("GitHubLogin", "GitHub login failed", it) }
        }
    }

    private fun handleGitHubResult(result: AuthResult) {
        val user = result.user
        val name = user?.displayName ?: ""
        val email = user?.email ?: ""

        Log.d("GitHubLogin", "success: $name <$email>")

        lifecycleScope.launch {
            val prefs = UserPreferences(this@MainActivity)
            prefs.setLoggedIn(true)
            prefs.saveUserInfo(email, name)
        }
        reloadHome()
    }

    private fun logoutUser() {
        auth.signOut()
        lifecycleScope.launch {
            UserPreferences(this@MainActivity).clear()
        }

        setContent {
            Cric8Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SplashScreen(
                        onGoogleSignInClick = { signInWithGoogle() },
                        onGitHubLoginClick = { launchGitHubLogin() },

                        )
                }
            }
        }
    }
    private fun sendMessage(text: String, imageUrl: String? = null) {
        val user = auth.currentUser ?: return

        val message = ChatMessage(
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "Anonymous",
            timestamp = Timestamp.now(),
            imageUrl = imageUrl
        )

        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d(TAG, "Message sent successfully")


            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error sending message", e)
            }
    }

    fun sendMessageWithImage(text: String, imageUri: Uri) {
        uploadImageToFirebase(
            imageUri = imageUri,
            onUploadSuccess = { imageUrl ->
                sendMessage(text, imageUrl)
            },
            onFailure = { e ->
                Log.e(TAG, "Image upload failed: ${e.message}")
            }
        )
    }

    private fun uploadImageToFirebase(
        imageUri: Uri,
        onUploadSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onUploadSuccess(uri.toString())
                }.addOnFailureListener { e ->
                    onFailure(e)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun editMessage(message: ChatMessage, newText: String) {
        if (message.messageId.isNotBlank()) {
            firestore.collection("messages")
                .document(message.messageId)
                .update("text", newText)
                .addOnSuccessListener { Log.d(TAG, "Message edited") }
                .addOnFailureListener { Log.e(TAG, "Edit failed", it) }
        }
    }

    private fun deleteMessage(message: ChatMessage) {
        if (message.messageId.isNotBlank()) {
            firestore.collection("messages")
                .document(message.messageId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "Message deleted") }
                .addOnFailureListener { Log.e(TAG, "Delete failed", it) }
        }
    }
    private fun reloadHome() {
        startListeningForMessages() // Also start listening here

        setContent {
            Cric8Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(
                        onLogout = { logoutUser() },
                        sendMessage = { text, imageUrl -> sendMessage(text, imageUrl) },
                        messages = currentMessagesList,
                        editMessage = { message, newText -> editMessage(message, newText) },
                        deleteMessage = { message -> deleteMessage(message) }
                    )
                }
            }
        }
    }
    private fun listenToMessages(onMessagesReceived: (List<ChatMessage>) -> Unit) {
        firestore.collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(messageId = doc.id)
                    }
                    onMessagesReceived(messages)
                }
            }
    }
}

