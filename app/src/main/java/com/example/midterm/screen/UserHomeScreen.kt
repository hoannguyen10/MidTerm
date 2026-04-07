package com.example.midterm.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.midterm.model.UserModel
import com.example.midterm.viewmodel.UserViewModel

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
    // State lưu trữ thông tin hiển thị trên màn hình
    var displayUsername by remember { mutableStateOf(username) }
    var userImageUri by remember { mutableStateOf(imageUri) }
    val message by userViewModel.message.collectAsState()

    // State cho Dialog chỉnh sửa
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf(username) }
    var editPassword by remember { mutableStateOf(currentPassword) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Launcher để chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { userImageUri = it.toString() }
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
            // 1. Ảnh đại diện (Chỉ xem)
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

            // 2. Hiển thị Tên (Chỉ xem)
            Text(
                text = displayUsername,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // 3. Hiển thị Role
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

            // Nút mở Dialog Chỉnh sửa
            Button(
                onClick = {
                    editUsername = displayUsername
                    isEditDialogOpen = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chỉnh sửa thông tin")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút Đăng xuất
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Đăng xuất")
            }

            if (message.isNotEmpty()) {
                Text(text = message, modifier = Modifier.padding(top = 16.dp))
            }

            // --- DIALOG CHỈNH SỬA TỔNG HỢP ---
            if (isEditDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isEditDialogOpen = false },
                    title = { Text("Cập nhật hồ sơ") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Sửa Ảnh
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Thay đổi ảnh đại diện")
                            }

                            // Sửa Tên
                            OutlinedTextField(
                                value = editUsername,
                                onValueChange = { editUsername = it },
                                label = { Text("Tên người dùng mới") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Sửa Mật khẩu (Có ẩn/hiện)
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
                            displayUsername = editUsername
                            val updatedUser = UserModel(editUsername, editPassword, role, userImageUri)
                            userViewModel.updateUser(updatedUser)
                            isEditDialogOpen = false
                        }) {
                            Text("Lưu tất cả")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditDialogOpen = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}