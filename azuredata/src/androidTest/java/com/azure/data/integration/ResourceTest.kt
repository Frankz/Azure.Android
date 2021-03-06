package com.azure.data.integration

import android.support.test.InstrumentationRegistry
import android.util.Log
import com.azure.core.log.d
import com.azure.core.log.startLogging
import com.azure.data.AzureData
import com.azure.data.model.*
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class ResourceTest<TResource : Resource>(resourceType: ResourceType,
                                              private val ensureDatabase : Boolean = true,
                                              private val ensureCollection : Boolean = true,
                                              private val ensureDocument : Boolean = false) {

    val databaseId = "AndroidTest${ResourceType.Database.name}"
    val collectionId = "AndroidTest${ResourceType.Collection.name}"
    val documentId = "AndroidTest${ResourceType.Document.name}"
    val createdResourceId = "AndroidTest${resourceType.name}"

    var response: Response<TResource>? = null
    var resourceListResponse: ListResponse<TResource>? = null
    var dataResponse: DataResponse? = null

    var database: Database? = null
    var collection: DocumentCollection? = null
    var document: Document? = null

    @Before
    open fun setUp() {

        startLogging(Log.VERBOSE)

        d{"********* Begin Test Setup *********"}

        if (!AzureData.isConfigured) {
            // Context of the app under test.
            val appContext = InstrumentationRegistry.getTargetContext()


        }

        deleteResources()

        if (ensureDatabase || ensureCollection || ensureDocument) {
            ensureDatabase()
        }

        if (ensureCollection || ensureDocument) {
            ensureCollection()
        }

        if (ensureDocument) {
            ensureDocument()
        }

        d{"********* End Test Setup *********"}
    }

    @After
    open fun tearDown() {

        d{"********* Begin Test Tear Down *********"}

        deleteResources()

        d{"********* End Test Tear Down *********"}
    }

    fun ensureDatabase() : Database {

        var dbResponse: Response<Database>? = null

        AzureData.createDatabase(databaseId) {
            dbResponse = it
        }

        await().until {
            dbResponse != null
        }

        assertResourceResponseSuccess(dbResponse)
        assertEquals(databaseId, dbResponse?.resource?.id)

        database = dbResponse!!.resource!!

        return database!!
    }

    fun ensureCollection() : DocumentCollection {

        var collectionResponse: Response<DocumentCollection>? = null

        AzureData.createCollection(collectionId, databaseId) {
            collectionResponse = it
        }

        await().until {
            collectionResponse != null
        }

        assertResourceResponseSuccess(collectionResponse)
        assertEquals(collectionId, collectionResponse?.resource?.id)

        collection = collectionResponse!!.resource!!

        return collection!!
    }

    private fun ensureDocument() : Document {

        var docResponse: Response<CustomDocument>? = null
        val doc = CustomDocument(documentId)

        AzureData.createDocument(doc, collection!!) {
            docResponse = it
        }

        await().until {
            docResponse != null
        }

        document = docResponse!!.resource!!

        return document!!
    }

    private fun deleteResources() {

        var deleteResponse: DataResponse? = null

        //delete the DB - this should delete all attached resources

        AzureData.deleteDatabase(databaseId) { response ->
            d{"Attempted to delete test database.  Result: ${response.isSuccessful}"}
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }
    }

    private fun assertResponsePopulated(response: Response<*>?) {

        assertNotNull(response)
        assertNotNull(response!!.request)
        assertNotNull(response.response)
        assertNotNull(response.jsonData)
    }

    fun <TResource : Resource> assertListResponseSuccess(response: ListResponse<TResource>?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)
        assertNotNull(response.resource)

        val list = response.resource as ResourceList<*>

        assertTrue(list.isPopuated)

        list.items.forEach { item ->
            assertResourcePropertiesSet(item)
        }
    }

    fun assertDataResponseSuccess(response: DataResponse?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)
    }

    fun assertResourceResponseSuccess(response: Response<*>?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)

        assertResourcePropertiesSet(response.resource as Resource)
    }

    fun assertResponseFailure(response: Response<*>?) {

        assertResponsePopulated(response)
        assertNotNull(response!!.error)
        assertFalse(response.isSuccessful)
        assertTrue(response.isErrored)
    }

    fun assertErrorResponse(response: Response<*>?) {

        assertNotNull(response)
        assertNotNull(response!!.error)
        assertFalse(response.isSuccessful)
        assertTrue(response.isErrored)
    }

    private fun assertResourcePropertiesSet(resource: Resource) {

        assertNotNull(resource.id)
        assertNotNull(resource.resourceId)
        assertNotNull(resource.selfLink)
        assertNotNull(resource.altLink)
        assertNotNull(resource.etag)
        assertNotNull(resource.timestamp)
    }

    fun resetResponse() {

        response = null
    }
}