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
                _message.value = it.message ?: "Lỗi tải danh sách người dùng"
            }
        }
    }

    fun addUser(user: UserModel) {
        viewModelScope.launch {
            val result = repository.addUser(user)
            result.onSuccess {
                _message.value = "Thêm người dùng thành công"
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi khi thêm người dùng"
            }
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            result.onSuccess {
                _message.value = "Cập nhật thông tin thành công"
                // Cập nhật lại currentUser nếu người dùng đang tự sửa thông tin của chính mình
                if (_currentUser.value?.username == user.username) {
                    _currentUser.value = user
                }
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi khi cập nhật thông tin"
            }
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            val result = repository.deleteUser(username)
            result.onSuccess {
                _message.value = "Đã xóa người dùng $username"
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi khi xóa người dùng"
            }
        }
    }

    fun updatePassword(username: String, newPassword: String) {
        viewModelScope.launch {
            val result = repository.updatePassword(username, newPassword)
            result.onSuccess {
                _message.value = "Đổi mật khẩu thành công"
                loadUsers()
            }.onFailure {
                _message.value = it.message ?: "Lỗi khi cập nhật mật khẩu"
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
                        _currentUser.value = user // Đăng nhập thẳng sau khi đăng ký
                        _message.value = "Đăng ký tài khoản thành công"
                        onSuccess()
                    }.onFailure {
                        _message.value = "Đăng ký thất bại, vui lòng thử lại"
                    }
                }
            }.onFailure {
                _message.value = "Không thể kết nối máy chủ"
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
                    _message.value = "Đăng nhập thành công"
                    onSuccess(foundUser)
                } else {
                    _message.value = "Sai tên đăng nhập hoặc mật khẩu"
                }
            }.onFailure {
                _message.value = it.message ?: "Lỗi hệ thống khi đăng nhập"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _message.value = "Đã đăng xuất"
    }
}