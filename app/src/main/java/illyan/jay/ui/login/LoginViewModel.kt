package illyan.jay.ui.login

import android.app.Activity
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val loginPresenter: LoginPresenter
) : RainbowCakeViewModel<LoginViewState>(Initial) {

	fun refresh() = executeNonBlocking {
		viewState = Loading
		viewState = if (loginPresenter.isUserLoggedIn()) LoggedIn else LoggedOut
	}

	fun load() = executeNonBlocking {
		viewState = if (loginPresenter.isUserLoggedIn()) LoggedIn else LoggedOut
		loginPresenter.addAuthStateListener {
			viewState = if (it) LoggedIn else LoggedOut
		}
	}

	fun onTryLogin() = executeNonBlocking {
		viewState = LoggingIn
	}

	fun handleGoogleSignInResult(
		activity: Activity,
		completedTask: Task<GoogleSignInAccount>
	) = executeNonBlocking {
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
			Timber.e(
				e,
				"signInResult:failed code=" + e.statusCode +
						"\nused api key: " +
						Firebase.remoteConfig["default_web_client_id"].asString()
							.substring(0, 4) + "..."
			)
			// Refreshing state should be called every time a custom login attempt is either successful or failed.
			refresh()
		}
	}

	private fun firebaseAuthWithCredential(activity: Activity, credential: AuthCredential) =
		executeNonBlocking {
			Firebase.auth.signInWithCredential(credential)
				.addOnCompleteListener(activity) { task ->
					if (task.isSuccessful) {
						// Sign in success, update UI with the signed-in user's information
						Timber.i("signInWithCredential:success")
					} else {
						// If sign in fails, display a message to the user.
						Timber.e(task.exception, "signInWithCredential:failure")
					}
					// Refreshing state should be called every time a custom login attempt is either successful or failed.
					refresh()
				}
		}
}