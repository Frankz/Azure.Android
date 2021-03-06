package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.core.log.d
import com.azure.data.*
import com.azure.data.model.Database
import com.azure.data.model.ResourceType
import com.azure.data.model.User
import com.azure.data.service.DataResponse
import com.azure.data.service.Response
import junit.framework.Assert.assertEquals
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class UserTests : ResourceTest<User>(ResourceType.User, true, false) {

    @Before
    override fun setUp() {
        super.setUp()

        deleteTestUser()
    }

    @After
    override fun tearDown() {

        deleteTestUser()

        super.tearDown()
    }

    private fun deleteTestUser(id: String = createdResourceId) {

        var deleteResponse: DataResponse? = null

        AzureData.deleteUser(id, databaseId) { response ->
            d{"Attempted to delete test user.  Result: ${response.isSuccessful}"}
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }
    }

    private fun createNewUser(db: Database? = null) : User {

        var userResponse: Response<User>? = null

        if (db == null) {
            AzureData.createUser(createdResourceId, databaseId) {
                userResponse = it
            }
        }
        else {
            db.createUser(createdResourceId) {
                userResponse = it
            }
        }

        await().until {
            userResponse != null
        }

        assertResourceResponseSuccess(userResponse)
        assertEquals(createdResourceId, userResponse?.resource?.id)

        return userResponse!!.resource!!
    }

    @Test
    fun createUser() {

        createNewUser()
    }

    @Test
    fun createUserInDatabase() {

        createNewUser(database)
    }

    @Test
    fun listUsers() {

        //make sure we have at least one user
        createNewUser()

        AzureData.getUsers(databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
    }

    @Test
    fun listUsersInDatabase() {

        //make sure we have at least one user
        createNewUser()

        database?.getUsers {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
    }

    @Test
    fun getUser() {

        createNewUser()

        AzureData.getUser(createdResourceId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun getUserInDatabase() {

        createNewUser()

        database?.getUser(createdResourceId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    //region Deletes

    @Test
    fun deleteUserById() {

        createNewUser()

        AzureData.deleteUser(createdResourceId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserFromDatabaseById() {

        val user = createNewUser()

        AzureData.deleteUser(user, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserFromDatabase() {

        val user = createNewUser()

        database?.deleteUser(user) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserInDatabaseById() {

        createNewUser()

        database?.deleteUser(createdResourceId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUser() {

        val user = createNewUser()

        user.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion

    @Test
    fun replaceUser() {

        val replaceUserId = "Updated_$createdResourceId"
        val user = createNewUser()

        AzureData.replaceUser(user.id, replaceUserId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(replaceUserId, response?.resource?.id)
        assertNotEquals(createdResourceId, response?.resource?.id)

        deleteTestUser(replaceUserId)
    }

    @Test
    fun replaceUserInDatabase() {

        val replaceUserId = "Updated_$createdResourceId"
        val user = createNewUser()

        database?.replaceUser(user.id, replaceUserId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(replaceUserId, response?.resource?.id)
        assertNotEquals(createdResourceId, response?.resource?.id)

        deleteTestUser(replaceUserId)
    }

    @Test
    fun refreshUser() {

        val user = createNewUser()

        user.refresh {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }
}