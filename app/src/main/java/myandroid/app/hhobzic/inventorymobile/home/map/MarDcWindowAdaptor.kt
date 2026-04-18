package myandroid.app.hhobzic.inventorymobile.home.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_STATUS
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverEntity


class MarDcWindowAdaptor(val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {

        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        // Inflate your custom info window layout
        // Set the title and snippet text
        val entity = marker.tag as DeliverEntity? ?: return null

        val view = LayoutInflater.from(context).inflate(R.layout.map_mark_window, null)
        when(entity){
            is DeliverEntity.distCenter ->{
                val dc = entity.dc
                view.findViewById<TextView>(R.id.title).setText(dc.name)
                view.findViewById<TextView>(R.id.address).setText(dc.address)
                if(dc.status == NODE_STATUS.NO_DELIVERED.code){ //
                    view.findViewById<TextView>(R.id.time).visibility = View.GONE
                    view.findViewById<TextView>(R.id.cart).visibility = View.GONE
                }
                else{ // already reached
                    view.findViewById<TextView>(R.id.time).setText(context.getString(R.string.map_dc_leave, dc.timeleave))
                    view.findViewById<TextView>(R.id.cart).setText(context.getString(R.string.map_dc_cart, dc.itemLoaded))
                }
            }
            is DeliverEntity.company ->{
                val cmp = entity.cmp
                view.findViewById<TextView>(R.id.title).setText(cmp.name)
                view.findViewById<TextView>(R.id.address).setText(cmp.address)
                if(cmp.status == NODE_STATUS.NO_DELIVERED.code){ //
                    view.findViewById<TextView>(R.id.time).visibility = View.GONE
                    view.findViewById<TextView>(R.id.cart).visibility = View.GONE
                }
                else{ // already reached
                    view.findViewById<TextView>(R.id.time).setText(context.getString(R.string.map_dc_leave, cmp.timeleave))
                    view.findViewById<TextView>(R.id.cart).setText(context.getString(R.string.map_dc_cart, cmp.itemLoaded))
                }
            }
            is DeliverEntity.store -> {
                val store = entity.store
                view.findViewById<TextView>(R.id.title).setText(store.name)
                view.findViewById<TextView>(R.id.address).setText(store.address)
                if(store.status == NODE_STATUS.NO_DELIVERED.code){ //
                    view.findViewById<TextView>(R.id.time).setText(context.getString(R.string.map_store_unreached))
                    view.findViewById<TextView>(R.id.cart).visibility = View.GONE
                }
                else{ // already reached
                    view.findViewById<TextView>(R.id.time).setText(context.getString(R.string.map_store_reached, store.timereached))
                    view.findViewById<TextView>(R.id.cart).setText(context.getString(R.string.map_store_cart, store.itemdelivered, store.itemloaded!!))
                }
            }

        }
        return view
    }
}
