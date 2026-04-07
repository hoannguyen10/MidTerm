package com.example.midterm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.midterm.model.UserModel
import com.example.midterm.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _userList = MutableStateFlow<List<UserModel>>(emptyList())
    val userList: StateFlow<List<UserModel>> = _userList

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser

    fun loadUsers() {
        viewModelScope.launch {
            val result = repository.getUsers()
            result.onSuccess {
                _userList.value = it
            }.onFailure {
                _message.value = it.message ?: "Lỗi tải dữ liệu"
            }
        }
    }

    fun addUser(user: UserModel) {
        viewModelScope.launch {
            val result = repository.addUser(user)
            result.onSuccess {
                _message.value = it
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi thêm user"
            }
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            result.onSuccess {
                _message.value = it
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi cập nhật"
            }
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            val result = repository.deleteUser(username)
            result.onSuccess {
                _message.value = it
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi xóa user"
            }
        }
    }

    fun updatePassword(username: String, newPassword: String) {
        viewModelScope.launch {
            val result = repository.updatePassword(username, newPassword)
            result.onSuccess {
                _message.value = it
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi cập nhật mật khẩu"
            }
        }
    }

    fun register(user: UserModel, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.getUsers()
            result.onSuccess { users ->
                val exists = users.any { it.username == user.username }
                if (exists) {
                    _message.value = "Tên đăng nhập đã tồn tại"
                } else {
                    repository.addUser(user).onSuccess {
                        _currentUser.value = user // Cập nhật user hiện tại để vào thẳng Home
                        _message.value = "Đăng ký thành công"
                        onSuccess()
                    }
                }
            }
        }
    }

    fun login(username: String, password: String, onSuccess: (UserModel) -> Unit) {
        viewModelScope.launch {
            val result = repository.getUsers()
            result.onSuccess { users ->
                val foundUser = users.find { it.username == username && it.password == password }
                if (foundUser != null) {
                    _currentUser.value = foundUser
                    _message.value = "Login successful"
                    onSuccess(foundUser)
                } else {
                    _message.value = "Wrong username or password"
                }
            }.onFailure {
                _message.value = it.message ?: "Lỗi đăng nhập"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _message.value = "Logged out"
    }
}