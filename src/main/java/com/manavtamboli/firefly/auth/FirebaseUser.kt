package com.manavtamboli.firefly.auth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.manavtamboli.firefly.await

suspend fun FirebaseUser.awaitIdToken(forceRefresh : Boolean = false) : GetTokenResult = getIdToken(forceRefresh).await()