package com.example.midterm.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.midterm.model.UserModel
import com.example.midterm.viewmodel.UserViewModel
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(context: Context, uri: Uri): String {
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
fun UserHomeScreen(
    username: String,
    currentPassword: String,
    role: String,
    imageUri: String,
    userViewModel: UserViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    // State hiển thị trên màn hình chính
    var displayUsername by remember { mutableStateOf(username) }
    var userImageUri by remember { mutableStateOf(imageUri) }
    val message by userViewModel.message.collectAsState()

    // State cho Dialog chỉnh sửa
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf(username) }
    var editPassword by remember { mutableStateOf(currentPassword) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var tempSelectedUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia() // Cái này sẽ mở giao diện chọn ảnh chuyên nghiệp
    ) { uri: Uri? ->
        uri?.let { tempSelectedUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hồ sơ cá nhân") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PHẦN HIỂN THỊ THÔNG TIN ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (userImageUri.isNotBlank()) {
                    AsyncImage(
                        model = userImageUri,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = displayUsername,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = role.uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- CÁC NÚT BẤM ---
            Button(
                onClick = {
                    editUsername = displayUsername
                    editPassword = currentPassword
                    isEditDialogOpen = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chỉnh sửa hồ sơ")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Đăng xuất")
            }

            if (message.isNotEmpty()) {
                Text(text = message, modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.primary)
            }

            // --- DIALOG CHỈNH SỬA ---
            if (isEditDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isEditDialogOpen = false },
                    title = { Text("Cập nhật thông tin") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            OutlinedButton(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (tempSelectedUri != null) "Đã chọn ảnh mới" else "Thay đổi ảnh đại diện")
                            }

                            // 2. Sửa Tên
                            OutlinedTextField(
                                value = editUsername,
                                onValueChange = { editUsername = it },
                                label = { Text("Tên người dùng") },
                                modifier = Modifier.fillMaxWidth()
                            )


                            OutlinedTextField(
                                value = editPassword,
                                onValueChange = { editPassword = it },
                                label = { Text("Mật khẩu mới") },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(imageVector = icon, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            // Xử lý lưu ảnh cục bộ nếu có chọn ảnh mới
                            val finalPath = if (tempSelectedUri != null) {
                                saveImageToInternalStorage(context, tempSelectedUri!!)
                            } else {
                                userImageUri
                            }

                            // Cập nhật trạng thái và ViewModel
                            displayUsername = editUsername
                            userImageUri = finalPath

                            val updatedUser = UserModel(editUsername, editPassword, role, finalPath)
                            userViewModel.updateUser(updatedUser)

                            isEditDialogOpen = false
                            tempSelectedUri = null // Reset ảnh tạm
                        }) {
                            Text("Lưu thay đổi")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            isEditDialogOpen = false
                            tempSelectedUri = null
                        }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}