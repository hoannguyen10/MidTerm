package com.example.midterm.repository

import com.example.midterm.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    // Thêm người dùng mới
    suspend fun addUser(user: UserModel): Result<String> {
        return try {
            userCollection.document(user.username).set(user).await()
            Result.success("Thêm người dùng thành công")
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi thêm người dùng: ${e.message}"))
        }
    }

    // Lấy danh sách tất cả người dùng
    suspend fun getUsers(): Result<List<UserModel>> {
        return try {
            val snapshot = userCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách người dùng"))
        }
    }

    // Cập nhật toàn bộ thông tin người dùng (bao gồm cả ảnh và tên hiển thị)
    suspend fun updateUser(user: UserModel): Result<String> {
        return try {
            userCollection.document(user.username).set(user).await()
            Result.success("Cập nhật thông tin thành công")
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi cập nhật thông tin: ${e.message}"))
        }
    }

    // Xóa người dùng dựa trên tên đăng nhập
    suspend fun deleteUser(username: String): Result<String> {
        return try {
            userCollection.document(username).delete().await()
            Result.success("Xóa người dùng thành công")
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi xóa người dùng"))
        }
    }

    // Chỉ cập nhật mật khẩu
    suspend fun updatePassword(username: String, newPassword: String): Result<String> {
        return try {
            userCollection.document(username)
                .update("password", newPassword)
                .await()
            Result.success("Cập nhật mật khẩu thành công")
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi khi cập nhật mật khẩu"))
        }
    }
}