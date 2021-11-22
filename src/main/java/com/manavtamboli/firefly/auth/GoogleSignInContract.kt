package com.manavtamboli.firefly.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

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