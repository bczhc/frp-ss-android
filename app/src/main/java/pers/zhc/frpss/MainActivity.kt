package pers.zhc.frpss

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pers.zhc.frpss.databinding.ActivityMainBinding
import pers.zhc.frpss.databinding.SettingsMenuBinding
import pers.zhc.util.IOUtils
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.system.exitProcess

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
    private val frpConfigFile by lazy { File(filesDir, "frpc.ini") }
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
            runOnUiThread { frpButton.isEnabled = false }
            executeFrp(frpLogET)
        }
        ssButton.setOnClickListener {
            runOnUiThread { ssButton.isEnabled = false }
            executeSS(ssLogET)
        }
    }

    private fun executeProcessOutputToET(cmd: Array<String>, et: EditText) {
        val process = Runtime.getRuntime().exec(cmd)
        val inputStream = process.inputStream
        val errorStream = process.errorStream

        val writeLinesThread = { stream: InputStream ->
            Thread {
                val scanner = Scanner(stream, "UTF-8")
                while (scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    runOnUiThread {
                        et.append(line)
                        et.append("\n")
                    }
                }
            }.start()
        }
        writeLinesThread(inputStream)
        writeLinesThread(errorStream)

        process.waitFor()
    }

    private fun executeFrp(logET: EditText) {
        Thread {
            frpConfigFile.writeText(Configs.generateFrpConfig())

            executeProcessOutputToET(
                arrayOf(
                    frpcBin.path,
                    "-c", frpConfigFile.path
                ), logET
            )
        }.start()
    }

    private fun executeSS(logET: EditText) {
        Thread {
            executeProcessOutputToET(
                arrayOf(
                    ssserverBin.path,
                    "--server-addr", "0.0.0.0:${Configs.SS_PORT}",
                    "--encrypt-method", Configs.SS_ENCRYPTION,
                    "--password", Configs.SS_PASSWORD
                ), logET
            )
        }.start()
    }

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
            R.id.exit -> {
                exitProcess(0)
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
        val frpRemotePortET = bindings.frpRemotePortEt.also { it.setText(Configs.FRP_REMOTE_PORT.toString()) }

        val saveConfigs = {
            Configs.SS_PORT = ssPortET.text.toString().toUShort()
            Configs.SS_ENCRYPTION = ssEncryptionET.text.toString()
            Configs.SS_PASSWORD = ssPasswordET.text.toString()
            Configs.FRP_SERVER_ADDRESS = frpServerAddrET.text.toString()
            Configs.FRP_SERVER_PORT = frpServerPortET.text.toString().toUShort()
            Configs.FRP_REMOTE_PORT = frpRemotePortET.text.toString().toUShort()
        }

        return MaterialAlertDialogBuilder(this)
            .setView(bindings.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                saveConfigs()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener { saveConfigs() }
            .create()
    }
}
