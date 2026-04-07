package com.example.midterm.repository

import com.example.midterm.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    suspend fun addUser(user: UserModel): Result<String> {
        return try {
            userCollection.document(user.username).set(user).await()
            Result.success("Thêm user thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<UserModel>> {
        return try {
            val snapshot = userCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(UserModel::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserModel): Result<String> {
        return try {
            userCollection.document(user.username).set(user).await()
            Result.success("Cập nhật thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(username: String): Result<String> {
        return try {
            userCollection.document(username).delete().await()
            Result.success("Xóa thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updatePassword(username: String, newPassword: String): Result<String> {
        return try {
            userCollection.document(username)
                .update("password", newPassword)
                .await()
            Result.success("Cập nhật mật khẩu thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
