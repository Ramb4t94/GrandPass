package com.example.apptest

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val nombre: String,
    val apellido1: String,
    val apellido2: String,
    val nacimiento: String,
    val email: String,
    val telefono: String,
    val password: String
)
