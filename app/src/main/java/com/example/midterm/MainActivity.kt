package com.example.midterm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.midterm.screen.AdminHomeScreen
import com.example.midterm.screen.LoginScreen
import com.example.midterm.screen.RegisterScreen
import com.example.midterm.screen.UserHomeScreen
import com.example.midterm.viewmodel.UserViewModel
import com.example.midterm.ui.theme.MidTermTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MidTermTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(userViewModel: UserViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                userViewModel = userViewModel,
                onLoginSuccess = { isAdmin ->
                    if (isAdmin) {
                        navController.navigate("admin_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("user_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                userViewModel = userViewModel,
                onRegisterSuccess = { _, _ ->
                    // Sau khi đăng ký thành công, điều hướng vào màn hình người dùng
                    navController.navigate("user_home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Màn hình Quản trị (Admin)
        composable("admin_home") {
            AdminHomeScreen(
                userViewModel = userViewModel,
                onLogout = {
                    userViewModel.logout() // Xóa trạng thái đăng nhập
                    navController.navigate("login") {
                        // Xóa lịch sử để không quay lại được màn hình Admin bằng nút Back
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        // Màn hình Người dùng (User)
        composable("user_home") {
            val currentUser by userViewModel.currentUser.collectAsState()

            if (currentUser != null) {
                UserHomeScreen(
                    username = currentUser!!.username,
                    currentPassword = currentUser!!.password,
                    role = currentUser!!.role,
                    imageUri = currentUser!!.imageUri, // Truyền đường dẫn ảnh đại diện
                    userViewModel = userViewModel,
                    onLogout = {
                        userViewModel.logout() // Xóa trạng thái đăng nhập
                        navController.navigate("login") {
                            // Xóa lịch sử để không quay lại được màn hình User
                            popUpTo("user_home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}