package pt.isec.agileMath.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    val instance: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {

        private fun getInstance(): FirebaseFirestore {
            return FirebaseService().instance
        }

        suspend fun save(key: String, value: Any): Any {
            val collection: CollectionReference = getInstance().collection(key)

            val task = collection.add(value).await()

            return task.id
        }

        /*fun get(key: String, id: String) : DataSnapshot{}*/

/*        fun update(key: String, id: String, value: Any): String{

        }*/
    }
}