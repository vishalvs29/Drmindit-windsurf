package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.Program
import com.drmindit.shared.domain.model.ProgramProgress
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    suspend fun getPrograms(): Result<List<Program>>
    suspend fun getProgramById(programId: String): Result<Program?>
    suspend fun startProgram(programId: String, userId: String): Result<ProgramProgress>
    suspend fun updateProgramProgress(progress: ProgramProgress): Result<Unit>
    suspend fun getProgramProgress(programId: String, userId: String): Result<ProgramProgress?>
    suspend fun completeProgramDay(programId: String, userId: String, day: Int): Result<Unit>
    fun observeProgramProgress(programId: String, userId: String): Flow<ProgramProgress?>
}
