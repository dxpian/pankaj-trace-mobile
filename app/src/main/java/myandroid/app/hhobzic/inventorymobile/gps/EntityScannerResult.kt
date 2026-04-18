package myandroid.app.hhobzic.inventorymobile.gps

import com.google.android.gms.maps.model.LatLng
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_TYPE
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon

sealed class GpsEntity {
    data class dc(val dc: DistCenterBrief) : GpsEntity()
    data class rs(val rs: StoreBrief) : GpsEntity()
    data class cmp(val cmp: CompanyBrief): GpsEntity()
    data class park(val park: ParkingBrief): GpsEntity()

    fun getId(): String{
        return when(this){
            is dc->{dc.id}
            is rs->{rs.id}
            is cmp->{cmp.id}
            is park->{park.company}
        }
    }

    fun getNodeType(): Number{
        return when(this){
            is dc->{NODE_TYPE.DC.code}
            is cmp->{NODE_TYPE.COMPANY.code}
            is rs->{NODE_TYPE.STORE.code}
            is park->{NODE_TYPE.PARKING.code}
            else ->{NODE_TYPE.UNK.code}
        }
    }

    fun getParkId(): String{
        return when(this){
            is  park -> {park.id }
            else->""
        }
    }
    fun getName(): String{
        return when(this){
            is dc->{dc.name}
            is rs->{rs.name}
            is cmp->{cmp.name}
            is park->{park.name}
        }
    }
    fun getLatlng(): LatLng {
        return when(this){
            is dc->{dc.latLng}
            is rs ->{rs.latLng}
            is cmp ->{cmp.latLng}
            is park->{park.latLng!!}

        }
    }
    // check if geofencing data is required
    fun needGeodata():Boolean{
        return when(this){
            is dc->{
                dc.polygon == null
            }
            is rs ->{
                rs.polygon == null
            }
            is cmp->{
                cmp.polygon == null
            }else->{    // no need polygon for parking type
                false
            }
        }
    }

    fun haveGeoData(): Boolean{
        return when(this){
            is dc->{
                dc.polygon?.isNotEmpty()==true
            }
            is rs ->{
                rs.polygon?.isNotEmpty() == true
            }
            is cmp->{
                cmp.polygon?.isNotEmpty() == true
            }else->{    // no need polygon for parking type
                false
            }
        }
    }
    fun pointInGeoarea(point: LatLng): Boolean{
        val nodeType = getNodeType()
        return when(this){
            is dc->{
                if(dc.polygon.isNullOrEmpty())
                    return false
                return GeoFenceUtil.isPointInPolygon(point, dc.polygon)
            }
            is rs->{
                if(rs.polygon.isNullOrEmpty())
                    return false
                return GeoFenceUtil.isPointInPolygon(point, rs.polygon)
            }
            is cmp->{
                if(cmp.polygon.isNullOrEmpty())
                    return false
                return GeoFenceUtil.isPointInPolygon(point, cmp.polygon)
            }
            else->{
                return false
            }
        }
    }

    fun copy(): GpsEntity{
        return when(this){
            is dc->{
                copy(dc = dc.copy())
            }
            is rs->{
               copy(rs = rs.copy())
            }
            is cmp->{
                copy(cmp = cmp.copy())
            }
            is park -> {
                copy(park = park.copy())
            }
        }
    }



}

interface EntityScannerResult {
    suspend fun onFoundBeacon(beacons: ArrayList<Beacon>?, entity: GpsEntity?)
}