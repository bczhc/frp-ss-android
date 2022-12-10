package pers.zhc.frpss

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pers.zhc.frpss.databinding.ActivityMainBinding
import pers.zhc.frpss.databinding.SettingsMenuBinding
import pers.zhc.util.IOUtils
import java.io.File
import java.util.zip.GZIPInputStream

class MainActivity : AppCompatActivity() {
    private val binDir by lazy {
        File(filesDir, "bin").also {
            if (!it.exists()) {
                it.mkdir()
            }
        }
    }
    private val frpcBin by lazy { File(binDir, "frpc") }
    private val ssserverBin by lazy { File(binDir, "ssserver") }
    private val dialogs = object {
        val settings by lazy { createSettingsDialog() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        extractBinFiles()

        val bindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val frpLogET = bindings.frpLogEt
        val ssLogET = bindings.ssLogEt
        val frpButton = bindings.frpStartBtn
        val ssButton = bindings.ssStartBtn

        frpButton.setOnClickListener {

        }
    }

//    private fun execute(binFile: File, logET: EditText) {
//        Thread {
//            Runtime.getRuntime().exec()
//        }.start()
//    }

    private fun extractBinFiles() {
        decompressRawFile(R.raw.frpc_gzip, frpcBin)
        decompressRawFile(R.raw.ssserver_gzip, ssserverBin)

        frpcBin.setExecutable(true)
        ssserverBin.setExecutable(true)
    }

    private fun decompressRawFile(@RawRes raw: Int, file: File) {
        val rawIS = resources.openRawResource(raw)
        val fileOS = file.outputStream()

        val gzipIS = GZIPInputStream(rawIS)
        IOUtils.streamWrite(gzipIS, fileOS)
        gzipIS.close()
        rawIS.close()
        fileOS.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                showSettingsDialog()
            }
        }
        return true
    }

    private fun showSettingsDialog() {
        dialogs.settings.show()
    }

    private fun createSettingsDialog(): AlertDialog {
        val bindings = SettingsMenuBinding.inflate(layoutInflater)
        val ssPortET = bindings.ssPortEt.also { it.setText(Configs.SS_PORT.toString()) }
        val ssEncryptionET = bindings.ssEncryptionEt.also { it.setText(Configs.SS_ENCRYPTION) }
        val ssPasswordET = bindings.ssPasswordEt.also { it.setText(Configs.SS_PASSWORD) }
        val frpServerPortET = bindings.frpServerPortEt.also { it.setText(Configs.FRP_SERVER_PORT.toString()) }
        val frpServerAddrET = bindings.frpServerAddressEt.also { it.setText(Configs.FRP_SERVER_ADDRESS) }

        return MaterialAlertDialogBuilder(this)
            .setView(bindings.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Configs.SS_PORT = ssPortET.text.toString().toUShort()
                Configs.SS_ENCRYPTION = ssEncryptionET.text.toString()
                Configs.SS_PASSWORD = ssPasswordET.text.toString()
                Configs.FRP_SERVER_ADDRESS = frpServerAddrET.text.toString()
                Configs.FRP_SERVER_PORT = frpServerPortET.text.toString().toUShort()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}
