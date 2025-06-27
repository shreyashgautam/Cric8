package com.example.cric8

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    onGoogleSignInClick: () -> Unit,
    onGitHubLoginClick: () -> Unit,

) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD54F)) // Yellow background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            // Top Image
            Image(
                painter = painterResource(id = R.drawable.criblogo), // your splash image
                contentDescription = "Cricket Illustration",
                modifier = Modifier
                    .padding(top = 87.dp, start = 50.dp)
                    .width(378.dp)
                    .height(358.dp)
            )

            Box(
                modifier = Modifier
                    .width(513.dp)
                    .height(553.dp)
                    .clip(RoundedTopCornersShape(190.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 70.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Welcome to CRIC8",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create an account to save your team,\nclub & league preferences.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    GoogleSignInButton(onClick = onGoogleSignInClick)

                    Spacer(modifier = Modifier.height(16.dp))

                    GitHubSignInButton(onClick = onGitHubLoginClick)




                }
            }
        }
    }
}

fun RoundedTopCornersShape(radius: Dp) = RoundedCornerShape(
    topStart = CornerSize(radius),
    topEnd = CornerSize(radius),
    bottomStart = CornerSize(0.dp),
    bottomEnd = CornerSize(0.dp)
)

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(50))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Continue with Google",
                color = Color.Black
            )
        }
    }
}

@Composable
fun GitHubSignInButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(50))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Continue with GitHub",
                color = Color.White
            )
        }
    }
}

