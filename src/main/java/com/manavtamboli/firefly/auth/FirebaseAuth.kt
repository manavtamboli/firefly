package com.manavtamboli.firefly.auth

import com.google.firebase.auth.FirebaseAuth

suspend fun FirebaseAuth.awaitIdToken(forceRefresh : Boolean = false) = currentUser?.awaitIdToken(forceRefresh)