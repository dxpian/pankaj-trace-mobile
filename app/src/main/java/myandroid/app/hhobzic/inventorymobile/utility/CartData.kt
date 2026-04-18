package myandroid.app.hhobzic.inventorymobile.utility

import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Store
const val CART_LOAD = "Loaded"



data class AppState(
    var comp: Company? = null,                              // company to start deliver task
    var comps: MutableList<Company> = mutableListOf(),       // LC  list contained in this delivering. This node is neither start or end node of deliver task
    var dcs: MutableList<DistCenter> = mutableListOf(),     // dcs list contained in this delivering
    var rss: MutableList<Store> = mutableListOf(),          // store list contained in this delivering
    var dc_selected: DistCenter?=null,
    var rs_last: Store?= null,                              // store added last recently
    var rs_arrived: Store?=null,                             // rs store which truck arrived now
    var comp_arrived: Company? = null                       // company arrived
)

interface CartDataListener {
    fun onCartDataSelected(cartData: Cart)
    fun onCartDataDeleted(cartData: Cart)
}


