package myandroid.app.hhobzic.inventorymobile.home.profile

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.Notification
import myandroid.app.hhobzic.inventorymobile.api.model.NotifyType
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverEntity



enum class NAVIGATION_TYPE{
    HOME,
    LOCATION,
    PROFILE
}

interface NotifyEventCallback {
    fun onNotifyRemove(notification: Notification)
    fun onNotifyRead(notification: Notification)
    fun onNotifyView(notification: Notification)
    fun onNavigateOtherPage(page: NAVIGATION_TYPE)
    fun onNotifyItemChanged(itemCount: Int)
}

enum class NotifyEventType(val type: Int) {
        UPDATE_NOTIFY(1),
        REMOVE_NOTIFY(2),
        REMOVE_ALL_NOTIFY(3)
}

class NotifyEvent(
    val event_type: NotifyEventType, // EntityEventType
    val notify: Notification?
)



class NotifyEventAdaptor(private var context: Context,  private var items: MutableList<Notification>,
                           private val eventCallback: NotifyEventCallback): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG = "DeliverEntityAdaptor"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  NotifyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notify_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotifyViewHolder -> holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class NotifyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeIconMode  = itemView.findViewById<ImageView>(R.id.removeNotify)
        val bellIcon = itemView.findViewById<ImageView>(R.id.iconNotify)
        val layoutMain = itemView.findViewById<LinearLayout>(R.id.layoutMain)
        val iconLink = itemView.findViewById<TextView>(R.id.notifyLink)
        init {
            // view main content
            layoutMain.setOnClickListener(){
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    var item = items[position]
                    val msgView = itemView.findViewById<TextView>(R.id.notifyContent)
                    if(msgView.ellipsize != null){ // it is collapsed now. expand it
                        msgView.maxLines = Integer.MAX_VALUE
                        msgView.ellipsize = null
                        if(!item.read) { // mark as read
                            eventCallback.onNotifyRead(item)
                            item.read = true
                        }
                    }
                    else{ // already expanded. collapse it
                        msgView.ellipsize = TextUtils.TruncateAt.END
                        msgView.maxLines = 1
                    }
                    notifyItemChanged(position)
                }
            }

            iconLink.setOnClickListener(){
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    var item = items[position]
                    when(item.type){
                        NotifyType.CART->{eventCallback.onNavigateOtherPage(NAVIGATION_TYPE.HOME)}
                        NotifyType.ROUTE->{eventCallback.onNavigateOtherPage(NAVIGATION_TYPE.LOCATION)}
                        NotifyType.USER->{eventCallback.onNavigateOtherPage(NAVIGATION_TYPE.PROFILE)}
                        NotifyType.DELIVER->{eventCallback.onNavigateOtherPage(NAVIGATION_TYPE.HOME)}
                        else->{
                            Log.d(TAG, "Invalid notifycation type: ${item.type}")
                        }
                    }
                }
            }

            // remove notification
            removeIconMode.setOnClickListener(){
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // listener.onButtonClick(position)
                    var item = items[position]
                    eventCallback.onNotifyRemove(item)
                }
            }

            // mark as unread
            bellIcon.setOnClickListener(){
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // listener.onButtonClick(position)
                    var item = items[position]
                    if(!item.read)
                        eventCallback.onNotifyRead(item)

                }
            }


        }
        fun bind(item: Notification) {
            itemView.findViewById<TextView>(R.id.notifyTime).setText(item.time)
            itemView.findViewById<TextView>(R.id.notifyContent).setText(item.message)
            val itemBell = itemView.findViewById<ImageView>(R.id.iconNotify)
            val itemLink = itemView.findViewById<TextView>(R.id.notifyLink)
            if(!item.read) // no read yet
                itemBell.setImageResource(R.drawable.bell)
            else
                itemBell.setImageResource(R.mipmap.bell_read)
            // set link type
            when(item.type){
                NotifyType.CART->{
                    itemLink.setText(context.getString(R.string.view_trip))
                }
                NotifyType.ROUTE->{
                    itemLink.setText(context.getString(R.string.view_route))
                }
                NotifyType.USER->{
                    itemLink.setText(context.getString(R.string.view_profile))
                }
                NotifyType.DELIVER->{
                    itemLink.setText(context.getString(R.string.view_cart))
                }
                else->{
                    itemLink.setText("")
                }

            }

        }
    }


    private fun updateNotify(notify: Notification){
        //Log.d(TAG, "Add new notification: ${items.size}, $notify")
        val notifySame = items.find { it.id == notify.id }
        if(notifySame == null){
            items.add(notify.copy())
            sortData()
            notifyItemInserted(items.size - 1)
        }
        eventCallback.onNotifyItemChanged(itemCount)
    }

    private fun removeNotify(notify: Notification){
        //Log.d(TAG, "Remove notification $notify")

        for(pos in 0 until itemCount){
            val item = items[pos]
            if(item.id != notify.id)
                continue
            items.removeAt(pos)
            this.notifyItemRemoved(pos)
            //Log.d(TAG, "Success to remove notify")
            break
        }
    }

    fun updateNode(entityEvent: NotifyEvent){
        when(entityEvent.event_type){
            NotifyEventType.UPDATE_NOTIFY ->{
                updateNotify(entityEvent.notify!!)
            }
            NotifyEventType.REMOVE_NOTIFY ->{
                removeNotify(entityEvent.notify!!)
            }
            NotifyEventType.REMOVE_ALL_NOTIFY ->{
                clear()
            }
            else->{
                //Log.d(TAG, "Invalid notification event type: ${entityEvent.event_type}")
            }
        }
    }

    fun clear(){
        items.clear()
        notifyDataSetChanged()
        eventCallback.onNotifyItemChanged(itemCount)
        //Log.d(TAG, "Notify adaptor is cleared: $items")
    }

    private fun sortData(){
        items = items.sortedBy { entity ->entity.time}.toMutableList()
    }

}
