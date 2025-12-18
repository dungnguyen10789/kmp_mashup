package vn.dna.kmp_mashup.data.datasource.user

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.dna.kmp_mashup.data.db.CoreDatabase
import vn.dna.kmp_mashup.data.db.DatabaseDriverFactory
import vn.dna.kmp_mashup.domain.entity.user.UserEntity

/**
 * This class is the single point of interaction with the User table in the local database.
 */
class UserLocalDataSource(driverFactory: DatabaseDriverFactory) {

    private val database = CoreDatabase(driver = driverFactory.createDriver())
    private val queries = database.coreDatabaseQueries

    /**
     * Returns a reactive Flow that emits the current user whenever it changes in the database.
     */
    fun observeUser(): Flow<UserEntity?> {
        return queries.findCurrentUser() // Correct method name
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { user -> UserEntity(
                id = user?.id ?: "",
                username = user?.username ?: "",
                fullName = user?.fullName ?: "",
                email = user?.email ?: "",
                gender = user?.gender?.toInt() ?: 0
            ) }
    }

    /**
     * Inserts or replaces a user in the database.
     */
    fun saveUser(user: UserEntity) {
        queries.insertUser(
            id = user.id,
            username = user.username,
            fullName = user.fullName,
            email = user.email,
            gender = user.gender.toLong()
        )
    }

    /**
     * Deletes all users from the table.
     */
    fun clear() {
        queries.deleteAll()
    }
}
