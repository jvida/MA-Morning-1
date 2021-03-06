package com.example.cheat

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.WHITE
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import org.jetbrains.anko.toast

class StartActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private var bt = BluetoothAdapter.getDefaultAdapter()
    private var list: ArrayList<String> = ArrayList()
    private val conn : BluetoothConnectivity = BluetoothConnectivity(this, bt)
    private var acceptThread : BluetoothConnectivity.AcceptThread = conn.startAcceptThread()
    private var connectThread : BluetoothConnectivity.ConnectThread? = null
    private var discoverable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        btnRefresh.setOnClickListener {
            refresh()
        }

        btnMakeDiscoverable.setOnClickListener {
            if (discoverable) {
                btnMakeDiscoverable.setText("Make discoverable")
                btnMakeDiscoverable.setBackgroundColor(Color.GREEN)
                btnMakeDiscoverable.setTextColor(WHITE)
                if(acceptThread.isAlive) {
                    acceptThread.cancel()
                    // Todo: figure out why the thread does not terminate on interrupt here...
                    // Throws an exception at the moment - but at least we are expecting it...
                    //connectionThread.interrupt()
                }
            }
            else {
                btnMakeDiscoverable.setText("Cancel making discoverable")
                btnMakeDiscoverable.setBackgroundColor(Color.RED)
                btnMakeDiscoverable.setTextColor(WHITE)
                conn.makeDiscoverable()
                acceptThread = conn.startAcceptThread()
                acceptThread.start()
            }
            discoverable = !discoverable
        }

        btnConnect.setOnClickListener {
            connectThread = conn.startConnectThread(spinnerFoundBTDevices.selectedItem as String)
            connectThread!!.start()
        }

        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT)

        refresh()
    }

    private fun refresh() {
        list = ArrayList()
        list.add("Discovering new devices ...")
        updateSpinner()
        list = conn.refresh()
        updateSpinner()
    }

    private fun updateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        spinnerFoundBTDevices.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (bt != null) {
                    if (bt.isEnabled) {
                        toast("Bluetooth has been enabled")
                    }
                    else {
                        toast("Bluetooth has been disabled")
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth enabling has been canceled")
            }
        }
    }
}
