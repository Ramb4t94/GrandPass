package com.example.apptest.screens
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
@Composable
fun MatchResultScreen(
    nav: NavHostController,
    name: String,
    surname1: String,
    phone: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Tienes una coincidencia!", fontSize = 30.sp, color = Color(0xFF6A4ACD))
        Spacer(modifier = Modifier.height(20.dp))
        Text("Nombre: $name", fontSize = 24.sp)
        Text("Apellido: $surname1", fontSize = 24.sp)
        Text("Teléfono: $phone", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(30.dp))

        val clipboard = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Button(
            onClick = {
                val clip = ClipData.newPlainText("phone", phone)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Número copiado", Toast.LENGTH_SHORT).show()
                nav.popBackStack() // ✅ go back to previous screen
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text("Copiar y volver al inicio")
        }

    }
}
