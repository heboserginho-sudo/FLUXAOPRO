package com.example.data.repository

import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class ProjectFinancialSummary(
    val project: ProjectEntity,
    val budgetedRevenue: Double,
    val actualRevenue: Double,
    val totalCost: Double,
    val netResult: Double,
    val marginPercentage: Double,
    val receivedAmount: Double,
    val pendingAmount: Double,
    val transactions: List<TransactionEntity>
)

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val transactionDao: TransactionDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    fun getProjectsByClient(clientId: Long): Flow<List<ProjectEntity>> = projectDao.getProjectsByClient(clientId)

    fun getProjectTransactions(projectId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByProject(projectId)

    suspend fun insertProject(project: ProjectEntity): Long = projectDao.insertProject(project)

    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)

    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    suspend fun deleteProjectById(id: Long) = projectDao.deleteProjectById(id)
}
