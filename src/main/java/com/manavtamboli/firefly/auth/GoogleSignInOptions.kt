package com.manavtamboli.firefly.auth

import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * Builder function for [GoogleSignInOptions].
 *
 * Usage
 * ```
 * val options = GoogleSignInOptions {
 *      requestEmail()
 * }
 * ```
 * */
inline fun GoogleSignInOptions(crossinline block : GoogleSignInOptions.Builder.() -> Unit): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .apply(block)
        .build()
}