package com.example.cric8

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController? = null,
    sendMessage: (String, String?) -> Unit,
    messages: List<ChatMessage>,
    onLogout: () -> Unit,
    editMessage: (ChatMessage, String) -> Unit,
    deleteMessage: (ChatMessage) -> Unit
) {
    var message by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(
                uri = it,
                onSuccess = { imageUrl ->
                    sendMessage("", imageUrl)
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size)
                    }
                },
                onError = {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var messageToEdit by remember { mutableStateOf<ChatMessage?>(null) }
    var newEditText by remember { mutableStateOf(TextFieldValue("")) }

    if (showEditDialog && messageToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Message") },
            text = {
                TextField(value = newEditText, onValueChange = { newEditText = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    messageToEdit?.let { editMessage(it, newEditText.text) }
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text("Chat", fontSize = 20.sp, color = Color.White)
            },
            navigationIcon = {
                navController?.let {
                    IconButton(onClick = { it.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            },
            actions = {
                TextButton(onClick = onLogout) {
                    Text("Logout", color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6366F1))
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    currentUserId = currentUserId,
                    onEdit = { message ->
                        messageToEdit = message
                        newEditText = TextFieldValue(message.text)
                        showEditDialog = true
                    },
                    onDelete = deleteMessage
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                launcher.launch("image/*")
            }) {
                Icon(Icons.Default.Place, contentDescription = "Pick Image")
            }

            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )

            IconButton(onClick = {
                if (message.text.isNotBlank()) {
                    sendMessage(message.text, null)
                    message = TextFieldValue("")
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size)
                    }
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

fun uploadImageToFirebaseStorage(
    uri: Uri,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val fileRef = FirebaseStorage.getInstance().reference
        .child("chat_images/${System.currentTimeMillis()}.jpg")

    fileRef.putFile(uri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }.addOnFailureListener { onError(it) }
        }
        .addOnFailureListener { onError(it) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    currentUserId: String?,
    onEdit: (ChatMessage) -> Unit,
    onDelete: (ChatMessage) -> Unit
) {
    val isSentByUser = message.senderId == currentUserId
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start
    ) {
        Column {
            Surface(
                color = if (isSentByUser) Color(0xFFDCF8C6) else Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { if (isSentByUser) showMenu = true }
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Show sender name for others
                    if (!isSentByUser) {
                        Text(
                            text = message.senderName,
                            color = Color(0xFF555555),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    message.imageUrl?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Sent image",
                            modifier = Modifier
                                .width(200.dp)
                                .height(200.dp)
                        )
                    }
                    if (message.text.isNotBlank()) {
                        Text(message.text, color = Color.Black)
                    }

                    val time = remember(message.timestamp) {
                        message.timestamp?.toDate()?.let {
                            android.text.format.DateFormat.format("hh:mm a", it).toString()
                        } ?: ""
                    }
                    Text(
                        time,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        onEdit(message)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(message)
                        showMenu = false
                    }
                )
            }
        }
    }
}