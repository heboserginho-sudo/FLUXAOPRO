package com.example.data.repository

import com.example.data.local.dao.ClientDao
import com.example.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchClients(query)

    suspend fun getClientById(id: Long): ClientEntity? = clientDao.getClientById(id)

    suspend fun getAllClientsList(): List<ClientEntity> = clientDao.getAllClientsList()

    suspend fun insertClient(client: ClientEntity): Long = clientDao.insertClient(client)

    suspend fun updateClient(client: ClientEntity) = clientDao.updateClient(client)

    suspend fun deleteClient(client: ClientEntity) = clientDao.deleteClient(client)

    suspend fun deleteClientById(id: Long) = clientDao.deleteClientById(id)
}
