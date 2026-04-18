package myandroid.app.hhobzic.inventorymobile.home.cart

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_STATUS

import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger

sealed class DeliverEntity{
    data class distCenter(val dc: DistCenter): DeliverEntity()
    data class store(val store: Store): DeliverEntity()
    data class company(val cmp: Company): DeliverEntity()
}

class EntityEventType {
    companion object Constant {
        const val UPDATE_DC = 1
        const val UPDATE_RS = 2
        const val REMOVE_DC = 3
        const val REMOVE_RS = 4
        const val UPDATE_CMP = 5        // update company
        const val REMOVE_CMP = 6        // remove company
        const val INIT_TRIP = 7         // init a new trip since there is no previous task
        const val INIT_TRIP_NOTIFY = 8  // only remove entities without calling init function
    }
}

class EntityEvent(
    val entity_type: Int, // EntityEventType
    val entity: Any
)

class DeliverEntityAdaptor(private var items: MutableList<DeliverEntity>,
                           private val onNodeClicked: (DeliverEntity)->Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG = "DeliverEntityAdaptor"
    companion object {
        const val VIEW_TYPE_DC = 1
        const val VIEW_TYPE_STORE = 2
        const val VIEW_TYPE_COMPANY = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DeliverEntity.distCenter -> VIEW_TYPE_DC
            is DeliverEntity.store -> VIEW_TYPE_STORE
            is DeliverEntity.company -> VIEW_TYPE_COMPANY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DC -> ViewHolderDist(LayoutInflater.from(parent.context).inflate(R.layout.deliver_dc_item, parent, false))
            VIEW_TYPE_STORE -> ViewHolderStore(LayoutInflater.from(parent.context).inflate(R.layout.deliver_store_item, parent, false))
            VIEW_TYPE_COMPANY -> ViewHolderCompany(LayoutInflater.from(parent.context).inflate(R.layout.deliver_company_item, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderDist -> holder.bind(items[position] as DeliverEntity.distCenter)
            is ViewHolderStore -> holder.bind(items[position] as DeliverEntity.store)
            is ViewHolderCompany -> holder.bind(items[position] as DeliverEntity.company)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolderCompany(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMore = itemView.findViewById<ImageView>(R.id.showMoreControl)
        val layoutClickable = listOf(
            itemView.findViewById<LinearLayout>(R.id.layoutDetail),
            itemView.findViewById<LinearLayout>(R.id.layoutStatus)
        )
        init {
            showMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // listener.onButtonClick(position)
                    var item = items[position] as DeliverEntity.company
                    item.cmp.expanded = !item.cmp.expanded
                    items[position] = item
                    notifyItemChanged(position)
                }
            }
            for(clickableRgn in layoutClickable){
                clickableRgn.setOnClickListener(){
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        var item = items[position] as DeliverEntity.company
                        onNodeClicked(item)
                    }
                }
            }
        }
        fun bind(item: DeliverEntity.company) {
            itemView.findViewById<TextView>(R.id.labelTitle).setText(itemView.context.getString(R.string.company))
            itemView.findViewById<TextView>(R.id.itemName).setText(item.cmp.name)
            itemView.findViewById<TextView>(R.id.itemAddress).setText(item.cmp.address)
            itemView.findViewById<TextView>(R.id.checkedTime).setText(item.cmp.timereached)

            if( item.cmp.status!! < NODE_STATUS.LEAVE.code){ // isn' completed
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_incomplete)
            }
            else{
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_completed)
            }
            when(item.cmp.status){
                NODE_STATUS.NO_DELIVERED.code->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_cmp_noreach)
                }
                NODE_STATUS.REACHED.code->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_cmp_reach)
                }
                NODE_STATUS.LEAVE.code->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_cmp_leave)
                }
                else ->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText("")
                }
            }

            if(!item.cmp.expanded){
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_more_24)
            }
            else{
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_less_24)

                // value for picked time
                itemView.findViewById<TextView>(R.id.detailPickuptime).setText(item.cmp.timereached)
                if(item.cmp.status == NODE_STATUS.LEAVE.code){
                    itemView.findViewById<TextView>(R.id.detailLeavetime).setText(item.cmp.timeleave)
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.VISIBLE
                }
                else{
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.GONE
                }
            }
        }
    }

    inner class ViewHolderDist(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMore = itemView.findViewById<ImageView>(R.id.showMoreControl)
        val layoutClickable = listOf(
            itemView.findViewById<LinearLayout>(R.id.layoutDetail),
            itemView.findViewById<LinearLayout>(R.id.layoutStatus)
        )
        init {
            showMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // listener.onButtonClick(position)
                    var item = items[position] as DeliverEntity.distCenter
                    item.dc.expanded = !item.dc.expanded
                    items[position] = item
                    notifyItemChanged(position)
                }
            }
            for(clickableRgn in layoutClickable){
                clickableRgn.setOnClickListener(){
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        var item = items[position] as DeliverEntity.distCenter
                        onNodeClicked(item)
                    }
                }
            }
        }
        fun bind(item: DeliverEntity.distCenter) {
            itemView.findViewById<TextView>(R.id.itemName).setText(item.dc.name)
            itemView.findViewById<TextView>(R.id.itemCode).setText(item.dc.code)
            itemView.findViewById<TextView>(R.id.itemAddress).setText(item.dc.address)
            itemView.findViewById<TextView>(R.id.checkedTime).setText(item.dc.timereached)

            if(item.dc.status!! < NODE_STATUS.LEAVE.code){ // isn' completed
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_incomplete)
            }
            else{
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_completed)
            }
            when(item.dc.status){
                NODE_STATUS.REACHED.code->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_dc_reach)
                }
                NODE_STATUS.LEAVE.code->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_dc_leave)
                }
                else ->{
                    itemView.findViewById<TextView>(R.id.textStatus).setText("")
                }
            }

            if(!item.dc.expanded){
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_more_24)


            }
            else{
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_less_24)
                // value for item loaded
                itemView.findViewById<TextView>(R.id.detailItemLoaded).setText(item.dc.itemLoaded.toString())
                // value for item returned
                itemView.findViewById<TextView>(R.id.detailItemReturned).setText(item.dc.itemreturned.toString())

                // value for picked time
                itemView.findViewById<TextView>(R.id.detailPickuptime).setText(item.dc.timereached)
                if(item.dc.status == NODE_STATUS.LEAVE.code){
                    itemView.findViewById<TextView>(R.id.detailLeavetime).setText(item.dc.timeleave)
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.VISIBLE
                }
                else{
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.GONE
                }
            }
        }
    }

    inner class ViewHolderStore(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMore = itemView.findViewById<ImageView>(R.id.showMoreControl)
        val layoutClickable = listOf(
            itemView.findViewById<LinearLayout>(R.id.layoutDetail),
            itemView.findViewById<LinearLayout>(R.id.layoutStatus)
        )
        init {
            showMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val position = adapterPosition
                    // listener.onButtonClick(position)
                    var item = items[position] as DeliverEntity.store
                    item.store.expanded = !item.store.expanded
                    items[position] = item
                    notifyItemChanged(position)
                }
            }
            for(clickableRgn in layoutClickable){
                clickableRgn.setOnClickListener(){
                    val position = adapterPosition
                    var item = items[position] as DeliverEntity.store
                    onNodeClicked(item)
                }
            }
        }
        fun bind(item: DeliverEntity.store) {
            val backColor = ContextCompat.getColor(itemView.context, R.color.node_store_back)
            itemView.setBackgroundColor( backColor )
            itemView.findViewById<TextView>(R.id.labelTitle).setText(R.string.node_store_title)
            itemView.findViewById<TextView>(R.id.itemName).setText(item.store.name)
            itemView.findViewById<TextView>(R.id.itemCode).setText(item.store.code)
            itemView.findViewById<TextView>(R.id.itemAddress).setText(item.store.address)
            itemView.findViewById<TextView>(R.id.checkedTime).setText(item.store.timereached)
            Log.d(TAG, "binding new store item: ${item.store}")
            val storeStatus = item.store.status
            if( storeStatus == NODE_STATUS.NO_DELIVERED.code){ // isn' completed
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_red)
                itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_store_init)
            }
            else if(storeStatus == NODE_STATUS.REACHED.code){
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_incomplete)
                itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_store_deliver)
            }
            else if(storeStatus == NODE_STATUS.LEAVE.code){
                itemView.findViewById<ImageView>(R.id.entityStatus).setImageResource(R.drawable.dotcircle_completed)
                itemView.findViewById<TextView>(R.id.textStatus).setText(R.string.status_name_store_leave)
            }
            else{
                itemView.findViewById<TextView>(R.id.textStatus).setText("")
            }

            if(!item.store.expanded){
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_more_24)
            }
            else{
                itemView.findViewById<ImageView>(R.id.showMoreControl).setImageResource(R.drawable.baseline_expand_less_24)
                itemView.findViewById<LinearLayout>(R.id.layoutDetailInformation).visibility = View.VISIBLE
                // value for item loaded
                itemView.findViewById<TextView>(R.id.detailItemDelivered).setText(item.store.itemdelivered.toString())

                // value for item returned
                itemView.findViewById<TextView>(R.id.detailItemPicked).setText(item.store.itemloaded.toString())

                // value for deliver time
                if(!item.store.timereached.isNullOrEmpty()) {
                    itemView.findViewById<TextView>(R.id.detailDelivertime)
                        .setText(item.store.timereached)
                    itemView.findViewById<LinearLayout>(R.id.layDetailDeliver).visibility = View.VISIBLE
                }
                else {
                    itemView.findViewById<LinearLayout>(R.id.layDetailDeliver).visibility =
                        View.GONE
                }
                // value for leave time
                if(!item.store.timeleave.isNullOrEmpty()){
                    itemView.findViewById<TextView>(R.id.detailLeavetime).setText(item.store.timeleave)
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.VISIBLE
                }
                else{
                    itemView.findViewById<LinearLayout>(R.id.layDetailLeave).visibility = View.GONE
                }
            }
        }
    }

    fun setItems(dcs: MutableList<DistCenter>, stores: MutableList<Store>){
        //Log.d(TAG, "Set the initial node items: ${dcs.size}, ${stores.size}")
        var newData: MutableList<DeliverEntity> = mutableListOf()
        for(dc in dcs){
            newData.add(DeliverEntity.distCenter(dc.copy()))
        }
        for(store in stores){
            newData.add(DeliverEntity.store(store.copy()))
        }
        items = newData
        sortData()
        this.notifyDataSetChanged()
    }

    fun updateEntity(entityEvent: EntityEvent){
        Log.d(TAG, "Update new entity: ${entityEvent.entity_type}, ${entityEvent.entity}")
        if(entityEvent.entity_type == EntityEventType.UPDATE_DC){
            updateDc(entityEvent.entity as DistCenter)
        }
        else if(entityEvent.entity_type == EntityEventType.UPDATE_RS){
            updateStore(entityEvent.entity as Store)
        }
        else if(entityEvent.entity_type == EntityEventType.REMOVE_DC){
            removeDc(entityEvent.entity as DistCenter)
        }
        else if(entityEvent.entity_type == EntityEventType.REMOVE_RS){
            removeStore(entityEvent.entity as Store)
        }
        else if(entityEvent.entity_type == EntityEventType.UPDATE_CMP){
            updateCompany(entityEvent.entity as Company)
        }
        else if(entityEvent.entity_type == EntityEventType.REMOVE_CMP){
            updateCompany(entityEvent.entity as Company)
        }


    }

    fun clear(){
        items.clear()
        notifyDataSetChanged()
        Log.d(TAG, "Item is removed in nodeadaptor: $items")
    }

    fun getItems(): List<DeliverEntity>{
        return items.toList()
    }

    private fun updateDc(dc: DistCenter){
        Log.d(TAG, "Status of distribution center is update: ${items.size}, $dc")
        var itemFound = false
        var pos = items.indexOfFirst {
            it is DeliverEntity.distCenter && it.dc.node_id == dc.node_id }
        AppLogger.d(TAG, "Success to find the updated logger: $pos")
        if(pos >= 0){
            Handler(Looper.getMainLooper()).post {
                items[pos] = DeliverEntity.distCenter(dc.copy())
                notifyItemChanged(pos)
            }
            Log.d(TAG, "Success to update distCenter: ${pos}: ${items[pos]}")
        }
        else{
            Handler(Looper.getMainLooper()).post{
                items.add(DeliverEntity.distCenter(dc.copy()))
                sortData()
                val newIndex = items.indexOfFirst {
                    it is DeliverEntity.distCenter && it.dc.node_id == dc.node_id
                }
                if (newIndex >= 0) {
                    notifyItemInserted(newIndex)
                }
            }
        }
    }

    private fun removeDc(dc: DistCenter){
        AppLogger.d(TAG, "Distribution center is removed $dc")
        val pos = items.indexOfFirst {
            it is DeliverEntity.distCenter && it.dc.node_id == dc.node_id
        }
        if (pos >= 0) {
            Handler(Looper.getMainLooper()).post {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    private fun updateCompany(company: Company){
        AppLogger.d(TAG, "Status of company is update: $company")
        var itemFound = false
        val pos = items.indexOfFirst {
            it is DeliverEntity.company && it.cmp.node_id == company.node_id
        }
        AppLogger.d(TAG, "The matched company index: $pos")
        if(pos >= 0){
            Handler(Looper.getMainLooper()).post {
                items[pos] = DeliverEntity.company(company.copy())
                this.notifyItemChanged(pos)
            }
            Log.d(TAG, "Success to update distCenter: ${pos}: ${items[pos]}")
        }
        else{
            Handler(Looper.getMainLooper()).post {
                items.add(DeliverEntity.company(company.copy()))
                sortData()
                val posNew = this.items.indexOfFirst {
                    it is DeliverEntity.company && it.cmp.node_id == company.node_id
                }
                if(posNew >= 0){
                    this.notifyItemInserted(posNew)
                    AppLogger.d(TAG, "Add new company item. Inserted item index: $posNew: ${this.items[posNew]}")
                }else{
                    AppLogger.d(TAG, "Failed to add new company")
                }
            }
        }
    }

    private fun removeCompany(company: Company){
        AppLogger.d(TAG, "Removing store from adaptor $company")
        val pos = items.indexOfFirst {
            it is DeliverEntity.company && it.cmp.node_id == company.node_id
        }
        if(pos >= 0){
            Handler(Looper.getMainLooper()).post {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    private fun updateStore(store: Store){
        AppLogger.d(TAG, "Status of retail store is update. current items: ${items.size} input store $store: ")
        AppLogger.d(TAG, "Current node items: $items")
        val pos = items.indexOfFirst {
            it is DeliverEntity.store && it.store.node_id == store.node_id
        }
        if(pos >= 0){
            Handler(Looper.getMainLooper()).post {
                items[pos] = DeliverEntity.store(store.copy())
                this.notifyItemChanged(pos)
            }
            AppLogger.d(TAG, "Success to update store: $pos: ${items[pos]}")
        }else{
            Handler(Looper.getMainLooper()).post{
                items.add(DeliverEntity.store(store.copy()))
                sortData()
                val posNew = items.indexOfFirst {
                    it is DeliverEntity.store && it.store.node_id == store.node_id
                }
                if(posNew >= 0){
                    notifyItemInserted(posNew)
                }
                AppLogger.d(TAG, "Success to insert new store: $posNew")
            }
        }
    }

    private fun removeStore(store: Store){
        AppLogger.d(TAG, "Remove store from node list: $store")
        val pos = items.indexOfFirst {
            it is DeliverEntity.store && it.store.node_id == store.node_id
        }
        if(pos >= 0){
            Handler(Looper.getMainLooper()).post {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    private fun sortData(){
        items = items.sortedByDescending  { entity ->
            when (entity) {
                is DeliverEntity.distCenter -> entity.dc.timereached
                is DeliverEntity.store -> getMaxDate(entity.store.timereached)
                is DeliverEntity.company->entity.cmp.timereached
            }
        }.toMutableList()
    }

    private fun getMaxDate(time: String?): String{
        if(time.isNullOrEmpty()){
            return "2100"
        }
        return time
    }
}
