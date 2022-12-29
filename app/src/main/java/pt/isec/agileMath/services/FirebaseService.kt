package pt.isec.agileMath.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseService {

    companion object {
        private var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("agile_math")

        fun save(key: String, value: Any): String {
            val id: String = this.reference.push().key!!
            this.reference.child(key).child(id).setValue(value)

            return id
        }

        fun get(key: String, id: String) : DataSnapshot{
            var result : DataSnapshot = this.reference.child(key).child(id).get().result

            return result
        }

/*        fun update(key: String, id: String, value: Any): String{

        }*/
    }
}