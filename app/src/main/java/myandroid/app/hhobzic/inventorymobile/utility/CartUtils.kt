package myandroid.app.hhobzic.inventorymobile.utility
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.NodeDeliver
import myandroid.app.hhobzic.inventorymobile.api.model.Store

fun hideKeyboardCart(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showToast(context: Context, message: String, durationInMillis: Long = 5000) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
    toast.show()
    Handler(Looper.getMainLooper()).postDelayed({
        toast.cancel()
    }, durationInMillis)
}

fun extendCompanyProperties(comp: Company, compDeliver: NodeDeliver): Company {
    comp.node_id = compDeliver.id
    comp.itemLoaded = compDeliver.itemLoaded
    comp.itemreturned = compDeliver.itemreturned
    comp.timereached = compDeliver.timereached
    comp.timeleave = compDeliver.timeleave
    comp.status = compDeliver.status
    return comp
}

fun extendDistCenterProperties(dc: DistCenter, dcDeliver: NodeDeliver): DistCenter {
    dc.node_id = dcDeliver.id
    dc.itemLoaded = dcDeliver.itemLoaded
    dc.itemreturned = dcDeliver.itemreturned
    dc.timereached = dcDeliver.timereached
    dc.timeleave = dcDeliver.timeleave
    dc.status = dcDeliver.status
    return dc
}

fun extendStoreProperties(store: Store, storeDeliver: NodeDeliver): Store {
    store.node_id = storeDeliver.id
    store.status = storeDeliver.status
    store.itemdelivered = storeDeliver.itemreturned
    store.itemloaded = storeDeliver.itemLoaded
    store.timereached = storeDeliver.timereached
    store.timeleave = storeDeliver.timeleave
    // store.sequence = storeDeliver.sequence
    return store
}


