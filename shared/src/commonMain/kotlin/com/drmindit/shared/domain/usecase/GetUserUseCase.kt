package com.drmindit.shared.domain.usecase

import com.drmindit.shared.domain.repository.UserRepository

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<com.drmindit.shared.domain.model.User?> {
        return userRepository.getCurrentUser()
    }
}
