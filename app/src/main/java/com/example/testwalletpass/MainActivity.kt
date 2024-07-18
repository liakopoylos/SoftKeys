package com.example.testwalletpass

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testwalletpass.ui.theme.TestWalletPassTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import java.security.SecureRandom
import java.util.Base64

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    //private val WEB_CLIENT_ID = "363198339123-ef1bou44mo739ge4ra36o7dt4mnf6ean.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign-In
        val webClientId = BuildConfig.WEB_CLIENT_ID
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .setNonce(generateNonce(16))
            .build()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            TestWalletPassTheme {
                var account by remember { mutableStateOf<GoogleSignInAccount?>(null) }

                val signInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleSignInResult(task) { signedInAccount ->
                        account = signedInAccount
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (account == null) {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding),
                            onSignInClick = { signInLauncher.launch(googleSignInClient.signInIntent) }
                        )
                    } else {
                        Text(
                            text = "Hello ${account?.displayName}!",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, onSignInSuccess: (GoogleSignInAccount) -> Unit) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            onSignInSuccess(account)
        } catch (e: ApiException) {
            // Sign in failed, handle appropriately
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onSignInClick: () -> Unit) {
    Button(onClick = onSignInClick) {
        Text(text = "Sign in with Google")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestWalletPassTheme {
        Greeting("Android", onSignInClick = {})
    }
}


fun generateNonce(size: Int): String {
    val randomBytes = ByteArray(size)
    SecureRandom().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}