package illyan.jay.view_model

import android.app.Activity
import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import illyan.jay.ui.login.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.Before
import timber.log.Timber

@ExperimentalCoroutinesApi
class LoginViewModelTest: ViewModelTest() {
    private lateinit var mockPresenter: LoginPresenter
    private lateinit var viewModel: LoginViewModel
    private lateinit var mockTree: Timber.Tree

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = LoginViewModel(mockPresenter)
        mockTree = mockk()
        Timber.plant(mockTree)
    }

    @After
    fun destroyEach() {
        Timber.uproot(mockTree)
    }

    @Test
    fun `Refresh sets state to logged in when user is logged in`() {
        every { mockPresenter.isUserLoggedIn() } returns true

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.refresh()
            stateObserver.assertObserved(Initial, Loading, LoggedIn)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
    }

    @Test
    fun `Refresh sets state to logged out when user is logged out`() {
        every { mockPresenter.isUserLoggedIn() } returns false

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.refresh()
            stateObserver.assertObserved(Initial, Loading, LoggedOut)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
    }

    @Test
    fun `Load sets state to logged in when user is logged in`() {
        every { mockPresenter.isUserLoggedIn() } returns true

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, LoggedIn)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
    }

    @Test
    fun `Load sets state to logged out when user is not logged in`() {
        every { mockPresenter.isUserLoggedIn() } returns false

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, LoggedOut)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
    }

    @Test
    fun `Load changes state to logged in when user logs in`() {
        every { mockPresenter.isUserLoggedIn() } returns false
        every { mockPresenter.addAuthStateListener(any()) } answers {
            firstArg<(Boolean) -> Unit>().invoke(true)
        }

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, LoggedOut, LoggedIn)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
        verify(exactly = 1) { mockPresenter.addAuthStateListener(any()) }

        verifySequence {
            mockPresenter.isUserLoggedIn()
            mockPresenter.addAuthStateListener(any())
        }
    }

    @Test
    fun `Load changes state to logged out when user logs out`() {
        every { mockPresenter.isUserLoggedIn() } returns true
        every { mockPresenter.addAuthStateListener(any()) } answers {
            firstArg<(Boolean) -> Unit>().invoke(false)
        }

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, LoggedIn, LoggedOut)
        }

        verify(exactly = 1) { mockPresenter.isUserLoggedIn() }
        verify(exactly = 1) { mockPresenter.addAuthStateListener(any()) }

        verifySequence {
            mockPresenter.isUserLoggedIn()
            mockPresenter.addAuthStateListener(any())
        }
    }

    @Test
    fun `On try login sets state to logging in`() {
        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.onTryLogin()
            stateObserver.assertObserved(Initial, LoggingIn)
        }
    }

    @Test
    fun `Google sign in succeeds when auth is successful`() = runTest {
        val mockActivity: Activity = mockk()

        val mockAccount: GoogleSignInAccount = mockk()
        every { mockAccount.idToken } returns "MOCK_TOKEN"

        val mockSignInTask: Task<GoogleSignInAccount> = mockk()
        every { mockSignInTask.getResult(ApiException::class.java) } returns mockAccount

        val mockCredential: AuthCredential = mockk()

        val mockAuthResultTask: Task<AuthResult> = mockk()
        every { mockAuthResultTask.addOnCompleteListener(mockActivity, any() ) } answers {
            secondArg<OnCompleteListener<AuthResult>>().onComplete(mockAuthResultTask)
            mockAuthResultTask
        }
        every { mockAuthResultTask.isSuccessful } returns true

        mockkStatic(GoogleAuthProvider::class)
        every { GoogleAuthProvider.getCredential("MOCK_TOKEN", null) } returns mockCredential

        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockk(relaxed = true)
        every { Firebase.auth.signInWithCredential(mockCredential) } returns mockAuthResultTask

        viewModel.handleGoogleSignInResult(mockActivity, mockSignInTask)

        verify(exactly = 1) { GoogleAuthProvider.getCredential("MOCK_TOKEN", null) }
        verify(exactly = 1) { Firebase.auth.signInWithCredential(mockCredential) }
        verify(exactly = 1) { mockAuthResultTask.addOnCompleteListener(mockActivity, any()) }
        verify(exactly = 1) { mockTree.i("signInWithCredential:success") }

        verifyOrder {
            GoogleAuthProvider.getCredential("MOCK_TOKEN", null)
            Firebase.auth.signInWithCredential(mockCredential)
            mockAuthResultTask.addOnCompleteListener(mockActivity, any())
            mockTree.i("signInWithCredential:success")
        }
    }

    @Test
    fun `Google sign in fails when auth is unsuccessful`() = runTest {
        val mockActivity: Activity = mockk()
        val mockException: Exception = mockk()

        val mockAccount: GoogleSignInAccount = mockk()
        every { mockAccount.idToken } returns "MOCK_TOKEN"

        val mockSignInTask: Task<GoogleSignInAccount> = mockk()
        every { mockSignInTask.getResult(ApiException::class.java) } returns mockAccount

        val mockCredential: AuthCredential = mockk()

        val mockAuthResultTask: Task<AuthResult> = mockk()
        every { mockAuthResultTask.addOnCompleteListener(mockActivity, any() ) } answers {
            secondArg<OnCompleteListener<AuthResult>>().onComplete(mockAuthResultTask)
            mockAuthResultTask
        }
        every { mockAuthResultTask.isSuccessful } returns false
        every { mockAuthResultTask.exception } returns mockException

        mockkStatic(GoogleAuthProvider::class)
        every { GoogleAuthProvider.getCredential("MOCK_TOKEN", null) } returns mockCredential

        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockk(relaxed = true)
        every { Firebase.auth.signInWithCredential(mockCredential) } returns mockAuthResultTask

        viewModel.handleGoogleSignInResult(mockActivity, mockSignInTask)

        verify(exactly = 1) { GoogleAuthProvider.getCredential("MOCK_TOKEN", null) }
        verify(exactly = 1) { Firebase.auth.signInWithCredential(mockCredential) }
        verify(exactly = 1) { mockAuthResultTask.addOnCompleteListener(mockActivity, any()) }
        verify(exactly = 1) { mockTree.e(t = mockException, message = "signInWithCredential:failure") }

        verifyOrder {
            GoogleAuthProvider.getCredential("MOCK_TOKEN", null)
            Firebase.auth.signInWithCredential(mockCredential)
            mockAuthResultTask.addOnCompleteListener(mockActivity, any())
            mockTree.e(t = mockException, message = "signInWithCredential:failure")
        }
    }
}
