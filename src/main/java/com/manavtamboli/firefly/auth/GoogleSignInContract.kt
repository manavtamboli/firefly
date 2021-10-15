package com.manavtamboli.firefly.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleSignInContract : ActivityResultContract<GoogleSignInClient, Result<GoogleSignInAccount>>() {
    override fun createIntent(context: Context, input: GoogleSignInClient): Intent {
        return input.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(intent)
            .runCatching {
                getResult(ApiException::class.java)
            }
    }
}

inline fun GoogleSignInOptions(crossinline block : GoogleSignInOptions.Builder.() -> Unit): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .apply(block)
            .build()
}

inline fun GoogleSignInClient(context: Context, crossinline block: GoogleSignInOptions.Builder.() -> Unit): GoogleSignInClient {
    return GoogleSignIn.getClient(context, GoogleSignInOptions(block))
}

inline fun GoogleSignInClient(activity : Activity, crossinline block: GoogleSignInOptions.Builder.() -> Unit) : GoogleSignInClient {
    return GoogleSignIn.getClient(activity, GoogleSignInOptions(block))
}

suspend fun GoogleSignInClient.suspendSignOut() = suspendCancellableCoroutine<Unit> { continuation ->
    signOut()
        .addOnSuccessListener { continuation.resume(Unit) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}