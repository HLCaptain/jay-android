package illyan.jay.ui.login

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.BuildConfig
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginPresenter: LoginPresenter
) : RainbowCakeViewModel<LoginViewState>(Loading) {
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    lateinit var googleSignInClient: GoogleSignInClient

    fun load(fragment: Fragment) = execute {
        viewState = LoginReady(loginPresenter.isUserLoggedIn())
        loginPresenter.addAuthStateListener {
            viewState = LoginReady(it)
        }

        resultLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(fragment.requireActivity(), task)
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.default_web_client_id)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(fragment.requireActivity(), gso)
    }

    fun tryGoogleSignIn() = execute {
        resultLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleSignInResult(
        activity: Activity,
        completedTask: Task<GoogleSignInAccount>
    ) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let {
                firebaseAuthWithCredential(
                    activity,
                    GoogleAuthProvider.getCredential(it, null)
                )
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.v("signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithCredential(activity: Activity, credential: AuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.v("signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.v(task.exception, "signInWithCredential:failure")
                }
            }
    }
}