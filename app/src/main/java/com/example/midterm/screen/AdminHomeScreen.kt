package com.example.midterm.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.midterm.model.UserModel
import com.example.midterm.viewmodel.UserViewModel
import java.io.File
import java.io.FileOutputStream

fun saveAdminImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    userViewModel: UserViewModel = viewModel(),
    onLogout: () -> Unit // Callback xử lý đăng xuất
) {
    val context = LocalContext.current // Thêm context để lưu file
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var tempSelectedUri by remember { mutableStateOf<Uri?>(null) }

    val userList by userViewModel.userList.collectAsState()
    val message by userViewModel.message.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            tempSelectedUri = it
            imageUri = it.toString()
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý người dùng") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    username = ""
                    password = ""
                    role = ""
                    imageUri = ""
                    tempSelectedUri = null // Reset biến tạm
                    isEditing = false
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Thêm người dùng mới")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Danh sách người dùng",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(userList) { user ->

                    val isAdmin = user.role == "admin"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (user.imageUri.isNotBlank()) {
                                    AsyncImage(
                                        model = user.imageUri,
                                        contentDescription = "Ảnh đại diện",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Ảnh mặc định",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isAdmin)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Text(
                                            text = if (isAdmin) "👑 ADMIN" else "👤 USER",
                                            modifier = Modifier.padding(
                                                horizontal = 14.dp,
                                                vertical = 6.dp
                                            ),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }

                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tài khoản hệ thống",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (!isAdmin) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            username = user.username
                                            password = user.password
                                            role = user.role
                                            imageUri = user.imageUri
                                            tempSelectedUri = null // Reset biến tạm khi bắt đầu sửa
                                            isEditing = true
                                            showDialog = true
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Sửa")
                                    }

                                    Button(
                                        onClick = {
                                            userViewModel.deleteUser(user.username)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Xóa")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    tempSelectedUri = null
                },
                confirmButton = {},
                title = {
                    Text(if (isEditing) "Chỉnh sửa người dùng" else "Thêm người dùng mới")
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    // Sửa: Dùng PickVisualMediaRequest (Giống UserHomeScreen)
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri.isNotBlank()) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Ảnh mặc định",
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Nhấn vào ảnh để thay đổi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tên đăng nhập") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            readOnly = isEditing
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Chọn vai trò",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { role = "admin" }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = role == "admin",
                                        onClick = { role = "admin" }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Admin")
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { role = "user" }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = role == "user",
                                        onClick = { role = "user" }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("User")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (username.isNotBlank() && password.isNotBlank() && role.isNotBlank()) {

                                        // Sửa: Xử lý lưu ảnh cục bộ trước khi truyền vào UserModel
                                        val finalPath = if (tempSelectedUri != null) {
                                            saveAdminImageToInternalStorage(context, tempSelectedUri!!)
                                        } else {
                                            imageUri
                                        }

                                        val user = UserModel(
                                            username = username,
                                            password = password,
                                            role = role,
                                            imageUri = finalPath
                                        )

                                        if (isEditing) {
                                            userViewModel.updateUser(user)
                                        } else {
                                            userViewModel.addUser(user)
                                        }

                                        showDialog = false
                                        tempSelectedUri = null // Clear biến tạm
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(if (isEditing) "Cập nhật" else "Lưu lại")
                            }

                            OutlinedButton(
                                onClick = {
                                    showDialog = false
                                    tempSelectedUri = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Hủy")
                            }
                        }
                    }
                }
            )
        }
    }
}