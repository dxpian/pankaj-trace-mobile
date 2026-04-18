package myandroid.app.hhobzic.inventorymobile.home.cart

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.Cart


class CartEventType{
    companion object Constant{
        const val UPDATE_CART = 1
        const val REMOVE_CART = 1
    }
}

class CartEvent(
    val eventType: Int, // CartEventType
    val cart: Cart?
)

class CartViewerAdaptor(private var items:MutableList<Cart>,
                        private val onCartClicked: (Cart)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val TAG = "CartViewerAdaptor"


    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderCart(LayoutInflater.from(parent.context).inflate(R.layout.cart_view_item, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderCart -> holder.bind(items[position] as Cart)
        }
    }


    override fun getItemCount(): Int = items.size

    inner class ViewHolderCart(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutMain = itemView.findViewById<LinearLayout>(R.id.layoutMain)
        init {
            layoutMain.setOnClickListener(){
                val position = adapterPosition
                var cart = items[position] as Cart
                onCartClicked(cart)
            }
        }
        fun bind(item: Cart) {
            itemView.findViewById<TextView>(R.id.cartIndex).setText((adapterPosition + 1).toString())
            itemView.findViewById<TextView>(R.id.cartMac).setText(item.iot)
        }
    }

    fun setItems(carts: MutableList<Cart>){
        //Log.d(TAG, "set the cart items: ${carts.size}")
        items.clear()
        var newData: MutableList<Cart> = mutableListOf()
        for(cart in carts){
            items.add(cart.copy())
        }
        sortData()
        this.notifyDataSetChanged()
    }

    fun updateCart(cartEvent: CartEvent){
        //Log.d(TAG, "Update Cart: ${cartEvent.eventType}: ${cartEvent.cart}")
        if(cartEvent.eventType == CartEventType.UPDATE_CART){

            updateCart(cartEvent.cart as Cart?)
        }
        else if(cartEvent.eventType == CartEventType.REMOVE_CART){
            removeCart(cartEvent.cart as Cart?)
        }
    }

    fun clear(){
        items.clear()
        notifyDataSetChanged()
        //Log.d(TAG, "Item is removed in cartAdaptor: $items")
    }

    fun getItems(): List<Cart>{
        return items.toList()
    }

    private fun updateCart(cart: Cart?){
        if(cart == null){
            Log.w(TAG, "Invalid cart was added to cart adaptor")
            return
        }
        //Log.d(TAG, "update cart in cart adaptor: ${items.size}, $cart")
        var itemFound = false
        for(pos in 0 until itemCount){
            val c = items[pos]
            if(c.iot != cart.iot) {
                continue
            }
            //Log.d(TAG, "Success to update distCenter: ${pos}: ${items[pos]}")
            items[pos] = cart.copy()
            this.notifyItemChanged(pos)
            itemFound = true
            break
        }
        if(!itemFound){ // add new item
            //Log.d(TAG, "New item added: $cart")
            items.add(cart.copy())
            sortData()
            notifyItemInserted(items.size - 1)
        }
    }

    private fun removeCart(cart: Cart?){
        //Log.d(TAG, "removing cart from cart Adaptor $cart")
        if(cart == null){
            Log.w(TAG, "invalid cart was tried to remove from cartAdaptor")
            return
        }
        var itemFound = false
        for(pos in 0 until itemCount){
            val c = items[pos]
            if(c.iot != cart.iot)
                continue

            items.removeAt(pos)
            this.notifyItemRemoved(pos)
            //Log.d(TAG, "Success to remove cart from CartAdaptor")
            break
        }
    }

    private fun sortData(){
        items = items.sortedBy { cart ->
            cart.iot
        }.toMutableList()
    }
}