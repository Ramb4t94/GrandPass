package com.example.apptest.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.apptest.FirebaseService
import com.example.apptest.location.getCurrentLocation
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
enum class MatchmakingStatus {
    IDLE, STARTING, SEARCHING
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(nav: NavHostController, gameId: String) {

    val ctx = LocalContext.current
    val uid = FirebaseService.auth.currentUser?.uid ?: return
    val scope = rememberCoroutineScope()

    var gameName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var numJugadores by remember { mutableStateOf("") }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var usersSearching by remember { mutableStateOf<List<Triple<String, Float, String>>>(emptyList()) }
    var matchmakingStatus by remember { mutableStateOf(MatchmakingStatus.IDLE) }

    var showRequestDialog by remember { mutableStateOf(false) }
    var showResponseDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf("") }
    var selectedUserName by remember { mutableStateOf("") }
    var selectedUserPhone by remember { mutableStateOf("") }

    val collections = listOf("NaipesJuegos", "TablerosJuegos", "OtrosJuegos")


    LaunchedEffect(gameId) {
        for (collection in collections) {
            val doc = FirebaseService.db.collection(collection).document(gameId).get().await()
            if (doc.exists()) {
                gameName = doc.getString("name") ?: ""
                description = doc.getString("description") ?: ""
                rules = doc.getString("rules") ?: ""
                numJugadores = doc.getString("num_jugadores") ?: ""

                break
            }
        }
    }

    // Listen to matchmaking
    DisposableEffect(Unit) {
        val listener = FirebaseService.db.collection("Matchmaking")
            .whereEqualTo("gameId", gameId)
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
                        Triple(name, distanceKm, userId)
                    }
                    usersSearching = users
                }
            }

        onDispose {
            listener.remove()
            FirebaseService.db.collection("Matchmaking").document(uid).delete()
        }
    }

    fun sendMeetingRequest(targetId: String, targetName: String) {
        FirebaseService.db.collection("MeetingRequests").document(targetId).set(mapOf("from" to uid))
        Toast.makeText(ctx, "Solicitud enviada a $targetName", Toast.LENGTH_SHORT).show()
    }

    // Listen for incoming requests
    LaunchedEffect(Unit) {
        FirebaseService.db.collection("MeetingRequests").document(uid)
            .addSnapshotListener { snap, _ ->
                val fromId = snap?.getString("from") ?: return@addSnapshotListener
                scope.launch {
                    val userDoc = FirebaseService.db.collection("Usuarios").document(fromId).get().await()
                    selectedUserId = fromId
                    selectedUserName = userDoc.getString("name") ?: ""
                    selectedUserPhone = userDoc.getString("phone") ?: ""
                    showResponseDialog = true
                }
            }
    }

    // Listen for confirmed matches
    LaunchedEffect(Unit) {
        FirebaseService.db.collection("MeetingConfirmed")
            .addSnapshotListener { snap, _ ->
                snap?.documents?.forEach { doc ->
                    val userA = doc.getString("userA") ?: return@forEach
                    val userB = doc.getString("userB") ?: return@forEach

                    if (uid == userA || uid == userB) {
                        val otherId = if (uid == userA) userB else userA
                        val seenField = if (uid == userA) "userASeen" else "userBSeen"
                        val docRef = doc.reference

                        // scope.launch ensures you're inside a coroutine when awaiting
                        scope.launch {
                            try {
                                val otherDoc = FirebaseService.db.collection("Usuarios").document(otherId).get().await()
                                val name = otherDoc.getString("name") ?: ""
                                val surname1 = otherDoc.getString("surname1") ?: ""
                                val phone = otherDoc.getString("phone") ?: ""

                                // 🧼 Stop matchmaking
                                FirebaseService.db.collection("Matchmaking").document(uid).delete().await()

                                // ✅ Navigate to result screen
                                nav.navigate("match_result/${name}/${surname1}/${phone}")

                                // ✅ Mark as seen
                                docRef.update(seenField, true).await()

                                // ✅ Cleanup
                                val updated = docRef.get().await()
                                val aSeen = updated.getBoolean("userASeen") == true
                                val bSeen = updated.getBoolean("userBSeen") == true
                                if (aSeen && bSeen) {
                                    docRef.delete()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
    }




    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = gameName, color = Color.White, fontSize = 30.sp) },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A4ACD))
        )

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        )  {

            Button(
                onClick = {
                    scope.launch {
                        when (matchmakingStatus) {
                            MatchmakingStatus.SEARCHING -> {
                                FirebaseService.db.collection("Matchmaking").document(uid).delete()
                                matchmakingStatus = MatchmakingStatus.IDLE
                            }

                            MatchmakingStatus.IDLE -> {
                                matchmakingStatus = MatchmakingStatus.STARTING

                                val loc = getCurrentLocation(ctx)
                                if (loc == null) {
                                    Toast.makeText(ctx, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                                    matchmakingStatus = MatchmakingStatus.IDLE
                                    return@launch
                                }

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
                                    "gameId" to gameId,
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )

                                FirebaseService.db.collection("Matchmaking").document(uid).set(data)
                                matchmakingStatus = MatchmakingStatus.SEARCHING
                            }

                            MatchmakingStatus.STARTING -> {} // Do nothing if in progress
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (matchmakingStatus) {
                        MatchmakingStatus.IDLE -> Color(0xFF6A4ACD)        // purple
                        MatchmakingStatus.STARTING -> Color.DarkGray           // in progress
                        MatchmakingStatus.SEARCHING -> Color.Red           // cancel
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = matchmakingStatus != MatchmakingStatus.STARTING
            ) {
                Text(
                    text = when (matchmakingStatus) {
                        MatchmakingStatus.IDLE -> "Iniciar búsqueda"
                        MatchmakingStatus.STARTING -> "Iniciando búsqueda..."
                        MatchmakingStatus.SEARCHING -> "Cancelar búsqueda"
                    },
                    fontSize = 25.sp,
                    color = Color.White
                )
            }


            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA8C2EA))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Lista de usuarios:", style = MaterialTheme.typography.titleMedium,fontSize = 30.sp)
                    if (usersSearching.isEmpty()) {
                        Text("Nadie está buscando aún.", fontSize = 20.sp)
                    } else {
                        usersSearching.forEach { (name, distance, id) ->
                            Button(
                                onClick = {
                                    selectedUserId = id
                                    selectedUserName = name
                                    showRequestDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(
                                    0xFF6A4ACD
                                )
                                ) // light purple
                            ) {
                                Text(
                                    "$name (${String.format("%.1f", distance)} km)",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                        }
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAB8A8))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Descripción:", style = MaterialTheme.typography.titleMedium,fontSize = 30.sp)
                    Text(description,fontSize = 22.sp)

                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAB8A8))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Número de jugadores:", style = MaterialTheme.typography.titleMedium,fontSize = 30.sp)
                    Text(numJugadores,fontSize = 22.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAB8A8))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Reglas:", style = MaterialTheme.typography.titleMedium,fontSize = 30.sp)
                    Text(rules,fontSize = 22.sp)
                }
            }


        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    sendMeetingRequest(selectedUserId, selectedUserName)
                    showRequestDialog = false
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) { Text("No") }
            },
            title = { Text("¿Deseas reunirte con $selectedUserName?") },
        )
    }

    if (showResponseDialog) {
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showResponseDialog = false
                    FirebaseService.db.collection("MeetingConfirmed").add(
                        mapOf("userA" to uid, "userB" to selectedUserId)
                    )
                    FirebaseService.db.collection("MeetingRequests").document(uid).delete()
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = {
                    FirebaseService.db.collection("MeetingRequests").document(uid).delete()
                    showResponseDialog = false
                }) { Text("No") }
            },
            title = { Text("¿$selectedUserName quiere reunirse contigo?") },
        )
    }


}
