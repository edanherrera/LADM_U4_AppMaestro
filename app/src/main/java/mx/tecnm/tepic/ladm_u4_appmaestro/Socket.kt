package mx.tecnm.tepic.ladm_u4_appmaestro

import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AlertDialog
import java.io.IOException

class Socket (private val activity: MainActivity, private val socket: BluetoothSocket): Thread() {
    private val inputStream = this.socket.inputStream
    private val outputStream = this.socket.outputStream
    private val mmBuffer: ByteArray = ByteArray(1024)

    override fun run() {
        try {
                try{
                    inputStream.read(mmBuffer)
                }catch(e: IOException){
                    activity.runOnUiThread {
                        AlertDialog.Builder(activity)
                            .setTitle("Error al registrar asistencia")
                            .setMessage("Intente de nuevo por favor")
                            .show()
                    }
                    return
                }
            val text = String(mmBuffer)
            activity.Insertar(text)
            } catch (e: Exception) {
            } finally {
                inputStream.close()
                outputStream.close()
                socket.close()
            }


    }
}