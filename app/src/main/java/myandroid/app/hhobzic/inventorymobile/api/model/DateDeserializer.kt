package myandroid.app.hhobzic.inventorymobile.api.model
import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class DateDeserializer : JsonDeserializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        return dateFormat.parse(json.asString) ?: throw JsonParseException("Unparseable date: ${json.asString}")
    }
}