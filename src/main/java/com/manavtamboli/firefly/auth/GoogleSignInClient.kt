package com.manavtamboli.firefly.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.manavtamboli.firefly.await


/**
 * Builder function for [GoogleSignInClient].
 * */
inline fun GoogleSignInClient(context: Context, crossinline block: GoogleSignInOptions.Builder.() -> Unit): GoogleSignInClient {
    return GoogleSignIn.getClient(context, GoogleSignInOptions(block))
}

/**
 * Builder function for [GoogleSignInClient].
 * */
inline fun GoogleSignInClient(activity : Activity, crossinline block: GoogleSignInOptions.Builder.() -> Unit) : GoogleSignInClient {
    return GoogleSignIn.getClient(activity, GoogleSignInOptions(block))
}

/**
 * Suspends sign out operation.
 * */
suspend fun GoogleSignInClient.suspendSignOut() {
    signOut().await()
}