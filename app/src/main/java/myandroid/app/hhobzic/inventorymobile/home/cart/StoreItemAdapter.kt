package myandroid.app.hhobzic.inventorymobile.home.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.Store

class StoreItemAdapter(private val context: Context,
                       private val dataSource: MutableList<Store>,
                       private val onStoreRemoved: (Store) -> Unit
    ) : BaseAdapter() {
    private val TAG = "StoreItemAdapter"
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_store_item, parent, false)

        val indexView = rowView.findViewById<TextView>(R.id.index)
        val nameTextView = rowView.findViewById<TextView>(R.id.name)
        val addrView = rowView.findViewById<TextView>(R.id.subinfo)
        val codeView = rowView.findViewById<TextView>(R.id.code)
        val imgTrash = rowView.findViewById<ImageView>(R.id.imgTrash)

        val storeItem = getItem(position) as Store
        imgTrash.setOnClickListener {
            removeStore(storeItem)
            onStoreRemoved(storeItem)
        }
        nameTextView.text = storeItem.name
        codeView.text = storeItem.code
        addrView.text = storeItem.address
        indexView.text = "${position + 1}."

        return rowView
    }

    fun addStore(store: Store) {
        if (!dataSource.any { it.id == store.id }) {
            dataSource.add(store)
        }
        //Log.d(TAG, "Current source size: ${dataSource}")
        notifyDataSetChanged()
    }

    fun removeStore(store: Store){
        dataSource.remove(store)
        notifyDataSetChanged()
    }
}