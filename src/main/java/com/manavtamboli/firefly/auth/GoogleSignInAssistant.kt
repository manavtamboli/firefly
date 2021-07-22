package com.manavtamboli.firefly.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

abstract class GoogleSignInAssistant private constructor(@StringRes private val tokenResId : Int, private val onResult : OnSignInResult) {

    abstract val context : Context
    abstract val launcher : ActivityResultLauncher<GoogleSignInClient>

    private val auth get() = Firebase.auth
    private val googleSignInClient by lazy {
        return@lazy GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(tokenResId))
                .requestEmail()
                .build()
        )
    }

    protected val contract = object : ActivityResultContract<GoogleSignInClient, Result<GoogleSignInAccount>>() {
        override fun createIntent(context: Context, input: GoogleSignInClient) = input.signInIntent
        override fun parseResult(resultCode: Int, intent: Intent?): Result<GoogleSignInAccount> {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            return kotlin.runCatching {
                try {
                    val acc = task.getResult(ApiException::class.java)
                    if (acc != null) return@runCatching acc else throw AuthException.Unknown("Google sign in client returned null.")
                } catch(e : ApiException) {
                    throw if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) AuthException.Cancelled else e
                }
            }
        }
    }
    protected val contractCallback = ActivityResultCallback<Result<GoogleSignInAccount>> {
        when {
            it == null -> onResult.onResult(Result.failure(AuthException.Unknown("Activity result contract returned null.")))
            it.isFailure -> onResult.onResult(Result.failure(it.exceptionOrNull()!!))
            it.isSuccess -> {
                val acc = it.getOrThrow()
                acc.idToken?.let { id -> firebaseAuthWithGoogle(id) }
                    ?: onResult.onResult(Result.failure(AuthException.InvalidIdToken))
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken : String)  {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnFailureListener { onResult.onResult(Result.failure(it)) }
            .addOnSuccessListener {
                val res = it.user?.let { user -> Result.success(user) } ?: Result.failure(AuthException.Unknown("Firebase User is null after successful sign in."))
                onResult.onResult(res)
            }
    }

    fun signIn(){
        auth.currentUser?.let {
            onResult.onResult(Result.success(it))
        } ?: launcher.launch(googleSignInClient)
    }
    fun signOut(){
        auth.signOut()
        googleSignInClient.signOut()
    }

    sealed class AuthException : Exception(){
        object InvalidIdToken : AuthException()
        object Cancelled : AuthException()
        data class Unknown(val details : String) : AuthException()
    }

    fun interface OnSignInResult {
        fun onResult(result : Result<FirebaseUser>)
    }

    companion object {
        fun ComponentActivity.GoogleSignInAssistant(@StringRes tokenResId : Int, onResult: OnSignInResult) =
            object : GoogleSignInAssistant(tokenResId, onResult) {
                override val context get() = this@GoogleSignInAssistant
                override val launcher = registerForActivityResult(contract, contractCallback)
            }

        fun Fragment.GoogleSignInAssistant(@StringRes tokenResId : Int, onResult: OnSignInResult) =
            object : GoogleSignInAssistant(tokenResId, onResult) {
                override val context get() = requireContext()
                override val launcher = registerForActivityResult(contract, contractCallback)
            }
    }
}