package myandroid.app.hhobzic.inventorymobile.websocket

// SocketIOClient.kt
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import java.net.URISyntaxException
import com.google.gson.Gson
import myandroid.app.hhobzic.inventorymobile.api.model.Notification


interface WebSocketCallback {
    fun onNotificationReceived(notification: Notification)
}

class SocketIOClient(private val serverUrl: String, private val callback: WebSocketCallback) {
    private val TAG = "SocketIOClient"
    private lateinit var mSocket: Socket
    private val gson = Gson()
    private var connected = false
    // Method to connect to the server with a user ID
    fun connect(userId: String) {
        try {
            val options = IO.Options()
            options.query = "userId=$userId" // Pass the user ID as a query parameter
            mSocket = IO.socket(serverUrl, options)

            mSocket.on(Socket.EVENT_CONNECT, onConnect)
            mSocket.on("notification", onNotification)
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Error initializing socket: ${e.message}")
        }
    }

    // Method to disconnect from the server
    fun disconnect() {
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off(AppMacro.NOTIFY_EVENT_NAME, onNotification)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
    }

    fun isConnected(): Boolean{
        return this.connected
    }
    // Callback for connection success
    private val onConnect = Emitter.Listener {
        //Log.d(TAG, "Connected to the server")
        this.connected = true
    }

    // Callback for receiving notifications
    private val onNotification = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            val data = args[0]
            try {
                val message = data as String
                //Log.d(TAG, "Received notification: $message")
                val notification = gson.fromJson(message, Notification::class.java)
                // Call the callback function
                callback.onNotificationReceived(notification)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification: ${e.message}")
            }
        }
    }

    // Callback for disconnection
    private val onDisconnect = Emitter.Listener {
        //Log.d(TAG, "Disconnected from the server")
        this.connected = false
    }

    // Callback for connection errors
    private val onConnectError = Emitter.Listener { args ->
        Log.e(TAG, "Connection error: ${args[0]}")
    }


}
