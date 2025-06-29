package com.example.apptest

/* ---------- imports ---------- */
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import com.example.apptest.screens.NaipesScreen
import com.example.apptest.screens.TablerosScreen
import com.example.apptest.screens.OtrosScreen

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import kotlin.system.exitProcess
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.apptest.screens.GameScreen
import com.example.apptest.screens.BusquedaRapidaScreen
import com.example.apptest.screens.MatchResultScreen
import com.example.apptest.screens.MatchmakingStatus


/* ---------- tiny helper ---------- */
object FirebaseService {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val db: FirebaseFirestore by lazy { Firebase.firestore }
}

/* ---------- activity ---------- */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)           // ← one-time Firebase init
        setContent { MyApp()
            }
    }
}

/* ---------- root composable ---------- */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "naipes") {
        composable("naipes") { NaipesScreen(navController) }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameScreen(navController, gameId)
        }
    }
}
@Composable
fun MyApp() {
    MaterialTheme {
        val nav = rememberNavController()
        val start = if (FirebaseService.auth.currentUser == null) "main" else "categories"

        NavHost(navController = nav, startDestination = start) {
            composable("main")        { MainScreen(nav) }
            composable("login")       { LoginScreen(nav) }
            composable("register")    { RegisterScreen(nav) }
            composable("categories")  { CategoriasScreen(nav) }
            composable("naipes")      { NaipesScreen(nav) }
            composable("game/{gameId}") { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                GameScreen(nav, gameId)
            }

            composable("tableros")    { TablerosScreen(nav) }
            composable("otros")       { OtrosScreen(nav) }
            composable("busrapida")       { BusquedaRapidaScreen(nav) }
            composable(
                "match_result/{name}/{surname1}/{phone}",
                arguments = listOf(
                    navArgument("name") { type = NavType.StringType },
                    navArgument("surname1") { type = NavType.StringType },
                    navArgument("phone") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                MatchResultScreen(
                    nav = nav,
                    name = backStackEntry.arguments?.getString("name") ?: "",
                    surname1 = backStackEntry.arguments?.getString("surname1") ?: "",
                    phone = backStackEntry.arguments?.getString("phone") ?: ""
                )
            }



        }
    }
}

/* ---------- screens ---------- */

@Composable
fun MainScreen(nav: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Grand\n\nPass", fontSize = 60.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(35.dp))

        Button(
            onClick = { nav.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Bigger height
        ) {
            Text("Iniciar Sesión", fontSize = 30.sp) // Bigger font
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { nav.navigate("register") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Registrarse", fontSize = 30.sp)
        }
    }
}

/* ---- LOGIN (Firebase) ---- */
@Composable
fun LoginScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var email     by rememberSaveable { mutableStateOf("") }
    var password  by rememberSaveable { mutableStateOf("") }
    var busy      by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar\nSesión", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(ctx, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                busy = true
                FirebaseService.auth
                    .signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        busy = false
                        if (task.isSuccessful) {
                            nav.navigate("categories") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val errorMessage = when {
                                task.exception?.message?.contains("no user record", ignoreCase = true) == true ->
                                    "No existe una cuenta con ese correo"
                                task.exception?.message?.contains("password is invalid", ignoreCase = true) == true ->
                                    "La contraseña es incorrecta"
                                task.exception?.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                                    "El correo electrónico no tiene un formato válido"
                                else -> "Error al iniciar sesión"
                            }

                            Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show()

                        }
                    }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (busy) "Validando…" else "Iniciar")
        }


        Spacer(Modifier.height(8.dp))
        Button(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Salir")
        }
    }
}

/* ---- REGISTER (Firebase Auth + Firestore) ---- */

@Composable
fun RegisterScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val labels = listOf(
        "Nombre", "Primer Apellido", "Segundo Apellido",
        "Correo:", "Teléfono:",
        "Contraseña:", "Confirme contraseña:"
    )
    val inputs = remember { labels.map { mutableStateOf("") } }

    val day = remember { mutableStateOf("") }
    val month = remember { mutableStateOf("") }
    val year = remember { mutableStateOf("") }

    val dayFocus = remember { FocusRequester() }
    val monthFocus = remember { FocusRequester() }
    val yearFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Registro", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        labels.forEachIndexed { i, lbl ->
            if (lbl != "Correo:" && lbl != "Teléfono:" && i < 3) {
                OutlinedTextField(
                    value = inputs[i].value,
                    onValueChange = {
                        if (it.all { c -> c.isLetter() }) {
                            inputs[i].value = it
                        }
                    },
                    label = { Text(lbl) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            } else if (lbl == "Correo:") {
                OutlinedTextField(
                    value = inputs[i].value,
                    onValueChange = { inputs[i].value = it },
                    label = { Text(lbl) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            } else if (lbl == "Teléfono:") {
                Text("Teléfono", modifier = Modifier.align(Alignment.Start))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("+56", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    OutlinedTextField(
                        value = inputs[i].value,
                        onValueChange = {
                            if (it.length <= 9 && it.all { c -> c.isDigit() }) {
                                inputs[i].value = it
                            }
                        },
                        placeholder = { Text("987654321") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                val isPassword = lbl.contains("Contraseña", true)
                OutlinedTextField(
                    value = inputs[i].value,
                    onValueChange = { inputs[i].value = it },
                    label = { Text(lbl) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Fecha de nacimiento:", modifier = Modifier.align(Alignment.Start))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = day.value,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        day.value = it
                        if (it.length == 2) monthFocus.requestFocus()
                    }
                },
                label = { Text("DD(Día)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(end = 4.dp).focusRequester(dayFocus)
            )
            OutlinedTextField(
                value = month.value,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        month.value = it
                        if (it.length == 2) yearFocus.requestFocus()
                    }
                },
                label = { Text("MM(Mes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp).focusRequester(monthFocus)
            )
            OutlinedTextField(
                value = year.value,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        year.value = it
                    }
                },
                label = { Text("AAAA(Año)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(start = 4.dp).focusRequester(yearFocus)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val allFilled = inputs.all { it.value.trim().isNotEmpty() } &&
                        day.value.isNotEmpty() && month.value.isNotEmpty() && year.value.isNotEmpty()

                val pwd = inputs[5].value
                val confirm = inputs[6].value
                val email = inputs[3].value.trim()
                val name = inputs[0].value.trim()
                val surname1 = inputs[1].value.trim()
                val surname2 = inputs[2].value.trim()

                val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$")

                if (!name.matches(nameRegex) || !surname1.matches(nameRegex) || !surname2.matches(nameRegex)) {
                    Toast.makeText(ctx, "Nombre y apellidos solo deben contener letras", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!allFilled) {
                    Toast.makeText(ctx, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (inputs[4].value.length != 9) {
                    Toast.makeText(ctx, "El número debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (pwd != confirm) {
                    Toast.makeText(ctx, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val d = day.value.toIntOrNull()
                val m = month.value.toIntOrNull()
                val y = year.value.toIntOrNull()

                if (d == null || m == null || y == null || d !in 1..31 || m !in 1..12 || y > 2007) {
                    Toast.makeText(ctx, "Fecha inválida. Debes ser mayor de 18 años", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val maxDaysInMonth = when (m) {
                    4, 6, 9, 11 -> 30
                    2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
                    else -> 31
                }
                if (d > maxDaysInMonth) {
                    Toast.makeText(ctx, "Día inválido para el mes seleccionado", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val birth = "${day.value.padStart(2, '0')}/${month.value.padStart(2, '0')}/${year.value}"

                FirebaseService.auth
                    .createUserWithEmailAndPassword(email, pwd)
                    .addOnSuccessListener { result ->
                        val uid = result.user!!.uid
                        val userMap = mapOf(
                            "uid"       to uid,
                            "name"      to name,
                            "surname1"  to surname1,
                            "surname2"  to surname2,
                            "birth"     to birth,
                            "phone"     to "+56${inputs[4].value.trim()}",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        FirebaseService.db.collection("Usuarios")
                            .document(uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(ctx, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                nav.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                    }
                    .addOnFailureListener {
                        val errorMsg = when {
                            it.message?.contains("The email address is badly formatted", ignoreCase = true) == true ->
                                "El correo electrónico no tiene un formato válido"
                            it.message?.contains("email address is already in use", ignoreCase = true) == true ->
                                "El correo electrónico ya está en uso"
                            else -> it.localizedMessage ?: "Error al registrar"
                        }
                        Toast.makeText(ctx, errorMsg, Toast.LENGTH_LONG).show()
                    }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
    }
}

/* ---- the rest of your original screens (unchanged) ---- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(nav: NavHostController) {
    val uid = FirebaseService.auth.currentUser?.uid
    var userName by remember { mutableStateOf("Usuario") }
    var showDialog by remember { mutableStateOf(false) }

    // 🔙 Handle system back button
    BackHandler(enabled = true) {
        showDialog = true
    }

    // Load name from Firestore
    LaunchedEffect(uid) {
        uid?.let {
            FirebaseService.db.collection("Usuarios").document(it).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "Usuario"
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bienvenido, $userName", fontSize = 30.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A4ACD),
                    titleContentColor = Color.White
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .border(
                                width = 3.dp,
                                color = Color(0xFF8B0000),
                                shape = RoundedCornerShape(20)
                            )
                            .background(Color.Red, shape = RoundedCornerShape(20))
                    ) {
                        IconButton(
                            onClick = { showDialog = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar app",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)

                            )
                        }
                    }
                }


            )
        }
    ) { paddingValues ->

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("¿Cerrar la app?") },
                text = { Text("¿Estás seguro de que quieres cerrar la aplicación?") },
                confirmButton = {
                    TextButton(onClick = { exitProcess(0) }) {
                        Text("Cerrar app")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        )  {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Categorías",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAB8A8)),
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = "Bienvenido a Grand pass, seleccione una de las categorías que usted esté interesado en buscar",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            CategoryItem("Naipes", R.drawable.cards, Color(0xFF6A4ACD)) {
                nav.navigate("naipes")
            }
            CategoryItem("Tableros", R.drawable.tablero, Color(0xFF6A4ACD)) {
                nav.navigate("tableros")
            }
            CategoryItem("Otros", R.drawable.dominos, Color(0xFF6A4ACD)) {
                nav.navigate("otros")
            }
            CategoryItem("Búsqueda rápida", R.drawable.search, Color(0xFF6A4ACD)) {
                nav.navigate("busrapida")

            }
            CategoryItem("Cerrar sesión", R.drawable.cross, Color.Red) {
                FirebaseService.auth.signOut()
                nav.navigate("main") {
                    popUpTo("categories") { inclusive = true }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    title: String,
    iconRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit
    ) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // ⬅️ Increase height
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(70.dp) // ⬅️ Increase icon size
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = title,
                fontSize = 30.sp, // ⬅️ Increase text size
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}







