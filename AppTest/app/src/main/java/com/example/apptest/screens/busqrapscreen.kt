package com.example.apptest.screens

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.apptest.FirebaseService
import com.example.apptest.location.getCurrentLocation
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaRapidaScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val uid = FirebaseService.auth.currentUser?.uid ?: return
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var usersSearching by remember { mutableStateOf<List<UserSearch>>(emptyList()) }
    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    var selectedUser by remember { mutableStateOf<UserSearch?>(null) }
    var showRequestPopup by remember { mutableStateOf(false) }
    var showResponsePopup by remember { mutableStateOf(false) }
    var matchedUserPhone by remember { mutableStateOf("") }
    var matchedUserName by remember { mutableStateOf("") }

    var showContactDialog by remember { mutableStateOf(false) }
    var selectedUserName by remember { mutableStateOf("") }
    var selectedUserPhone by remember { mutableStateOf("") }

    // Listen to all matchmaking users
    DisposableEffect(Unit) {
        listener = FirebaseService.db.collection("Matchmaking")
            .addSnapshotListener { snap, _ ->
                val currentLoc = userLocation
                if (snap != null && currentLoc != null) {
                    val users = snap.documents.mapNotNull { doc ->
                        val userId = doc.getString("userId") ?: return@mapNotNull null
                        if (userId == uid) return@mapNotNull null

                        val name = doc.getString("userName") ?: return@mapNotNull null
                        val lat = doc.getDouble("lat") ?: return@mapNotNull null
                        val lon = doc.getDouble("lon") ?: return@mapNotNull null

                        val userLoc = Location("").apply {
                            latitude = lat
                            longitude = lon
                        }

                        val distanceKm = currentLoc.distanceTo(userLoc) / 1000
                        UserSearch(userId, name, distanceKm)
                    }
                    usersSearching = users
                }
            }

        onDispose {
            listener?.remove()
            FirebaseService.db.collection("Matchmaking").document(uid).delete()
        }
    }

    // Listen for contact match
    LaunchedEffect(Unit) {
        FirebaseService.db.collection("MeetingConfirmed")
            .addSnapshotListener { snap, _ ->
                snap?.documents?.forEach { doc ->
                    val userA = doc.getString("userA") ?: return@forEach
                    val userB = doc.getString("userB") ?: return@forEach
                    val docRef = doc.reference

                    if (uid == userA || uid == userB) {
                        val otherId = if (uid == userA) userB else userA
                        val seenField = if (uid == userA) "userASeen" else "userBSeen"

                        scope.launch {
                            val otherDoc = FirebaseService.db.collection("Usuarios")
                                .document(otherId).get().await()
                            selectedUserName = otherDoc.getString("name") ?: ""
                            selectedUserPhone = otherDoc.getString("phone") ?: ""
                            showContactDialog = true

                            docRef.update(seenField, true).await()
                            val updated = docRef.get().await()
                            val aSeen = updated.getBoolean("userASeen") == true
                            val bSeen = updated.getBoolean("userBSeen") == true
                            if (aSeen && bSeen) docRef.delete()
                        }
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Búsqueda rápida", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A4ACD))
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        if (isSearching) {
                            FirebaseService.db.collection("Matchmaking").document(uid).delete()
                            isSearching = false
                        } else {
                            val loc = getCurrentLocation(ctx) ?: return@launch
                            userLocation = loc

                            val userDoc = FirebaseService.db.collection("Usuarios").document(uid).get().await()
                            val name = userDoc.getString("name") ?: ""
                            val surname = userDoc.getString("surname1") ?: ""
                            val fullName = "$name $surname".trim()

                            val data = mapOf(
                                "userId" to uid,
                                "userName" to fullName,
                                "lat" to loc.latitude,
                                "lon" to loc.longitude,
                                "gameId" to "general",
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            FirebaseService.db.collection("Matchmaking").document(uid).set(data)
                            isSearching = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSearching) Color.Red else Color(0xFF6A4ACD)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSearching) "Cancelar búsqueda" else "Iniciar búsqueda")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAB8A8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Lista de usuarios:", style = MaterialTheme.typography.titleMedium)
                    if (usersSearching.isEmpty()) {
                        Text("Nadie está buscando aún.")
                    } else {
                        usersSearching.forEach { user ->
                            Text(
                                text = "- ${user.name} (${String.format("%.1f", user.distance)} km)",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUser = user
                                        showRequestPopup = true
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRequestPopup && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showRequestPopup = false },
            title = { Text("¿Quieres reunirte con ${selectedUser!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val request = mapOf(
                            "fromId" to uid,
                            "toId" to selectedUser!!.id,
                            "status" to "pending"
                        )
                        FirebaseService.db.collection("Requests").document(uid).set(request)
                        showRequestPopup = false

                        FirebaseService.db.collection("Requests").document(selectedUser!!.id)
                            .addSnapshotListener { snap, _ ->
                                if (snap != null &&
                                    snap.getString("fromId") == selectedUser!!.id &&
                                    snap.getString("toId") == uid &&
                                    snap.getString("status") == "accepted"
                                ) {
                                    scope.launch {
                                        val userDoc = FirebaseService.db.collection("Usuarios")
                                            .document(selectedUser!!.id).get().await()
                                        matchedUserName = userDoc.getString("name") ?: ""
                                        matchedUserPhone = userDoc.getString("phone") ?: ""
                                        showResponsePopup = true
                                    }
                                }
                            }
                    }
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showRequestPopup = false }) { Text("No") }
            }
        )
    }

    if (showResponsePopup) {
        AlertDialog(
            onDismissRequest = { showResponsePopup = false },
            title = { Text("¡Coincidencia!") },
            text = { Text("Nombre: $matchedUserName\nTeléfono: $matchedUserPhone") },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(matchedUserPhone))
                    showResponsePopup = false
                }) { Text("Copiar número") }
            },
            dismissButton = {
                TextButton(onClick = { showResponsePopup = false }) { Text("Ok") }
            }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("¡Coincidencia!") },
            text = { Text("Nombre: $selectedUserName\nTeléfono: $selectedUserPhone") },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(selectedUserPhone))
                    showContactDialog = false
                }) { Text("Copiar número") }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("Ok") }
            }
        )
    }
}

data class UserSearch(
    val id: String,
    val name: String,
    val distance: Float
)
