package mx.tecnm.tepic.ladm_u4_appmaestro

import android.R
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u4_appmaestro.databinding.ActivityMainBinding
import java.io.File
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {
    val identifi: UUID = UUID.fromString("8e3508b8-e39f-11ec-8fea-0242ac120002")
    lateinit var binding : ActivityMainBinding
    val baseRemota = FirebaseFirestore.getInstance()
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val server = Server(this,identifi).start()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root); tengo();
        binding.tomar.setOnClickListener {
            if(!BluetoothAdapter.getDefaultAdapter().isDiscovering){
                Toast.makeText(this,"Visible",Toast.LENGTH_LONG).show()
                val intent = Intent(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
                startActivityForResult(intent,1)
            }
        }
        binding.generar.setOnClickListener {
            var archivo = OutputStreamWriter(openFileOutput("lista.csv", MODE_PRIVATE))
            var cadena =""
            var fechaA= Instant.now()
            val fec = fechaA.atZone(ZoneId.of("America/Mazatlan")).toString()
            val split = fec.split("T")
            val fecha = split[0]
            FirebaseFirestore.getInstance().collection("lista"+"${fecha}")
            .addSnapshotListener { query, error ->
                val arreglo = ArrayList<String>()
                listaID.clear()
                for (documento in query!!) {
                    cadena = "${cadena}"+"${documento.getString("noControl")}," +
                            "${documento.getString("fecha")},${documento.getString("hora")}\n"
                    listaID.add(documento.id.toString())
                }
                arreglo.add(cadena)
                archivo.write(cadena)
                archivo.flush()
                archivo.close()
                println("Write CSV successfully!")
                val sendIntent = Intent()
                val file: File = File(this.getFilesDir(), "lista.csv")
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                    "mx.tecnm.tepic.ladm_u4_appmaestro.provider",file))
                sendIntent.type = "text/csv"
                startActivity(Intent.createChooser(sendIntent, "SHARE"))
            }
        }

        var fechaA= Instant.now()
        val fec = fechaA.atZone(ZoneId.of("America/Mazatlan")).toString()
        val split = fec.split("T")
        val fecha = split[0]
        val hora = split[1]
        FirebaseFirestore.getInstance()
            .collection("lista"+"${fecha}")
            .addSnapshotListener { query, error ->
                if (error!=null){
                    //SI HUBO ERROR!
                    AlertDialog.Builder(this)
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }

                val arreglo = ArrayList<String>()
                listaID.clear()
                for (documento in query!!){
                    var cadena = "No. Control: ${documento.getString("noControl")}\n" +
                            "Fecha:  ${documento.getString("fecha")} \nHora: ${documento.getString("hora")}\n"
                    arreglo.add(cadena)

                    listaID.add(documento.id.toString())
                }
                binding.lista.adapter= ArrayAdapter<String>(this,
                    R.layout.simple_list_item_1, arreglo)
            }

    }

    fun tengo(){

        val permiso = arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!permisos(this, permiso)) {
            ActivityCompat.requestPermissions(this, permiso, 1)
        }
    }
    fun Insertar(pa:String){
        val noControl = pa
        var fechaA= Instant.now()
        val fec = fechaA.atZone(ZoneId.of("America/Mazatlan")).toString()
        val split = fec.split("T")
        val fecha = split[0]
        val hora = split[1]


        val datos = hashMapOf(
            "noControl" to noControl,
            "fecha" to fecha,
            "hora" to hora
        )
        baseRemota.collection("lista"+"${fecha}").add(datos)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Lista Registrada con Exito",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setMessage(it.message)
                    .show()
            }
    }
    fun permisos(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}