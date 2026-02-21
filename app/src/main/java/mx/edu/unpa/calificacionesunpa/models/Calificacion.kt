import com.google.gson.annotations.SerializedName

class Calificacion() {
    var parcial1: Double? = null
    var parcial2: Double? = null
    var parcial3: Double? = null
    var ordinario: Double? = null
    @SerializedName("pfinal")
    var pFinal: Double? = null
    var extra1: Double? = null
    var extra2: Double? = null
    var especial : Double? = null
}
