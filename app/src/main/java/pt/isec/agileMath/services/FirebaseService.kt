package pt.isec.agileMath.services

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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

        suspend fun get(key: String, id: String): MutableMap<String, Any>? {
            val collection: CollectionReference = getInstance().collection(key)

            val task = collection.document(id).get().await()

            return task.data
        }

        fun list(key:String): Task<QuerySnapshot> {
            val collection: CollectionReference = getInstance().collection(key)

            return collection.get()
        }

    }
}