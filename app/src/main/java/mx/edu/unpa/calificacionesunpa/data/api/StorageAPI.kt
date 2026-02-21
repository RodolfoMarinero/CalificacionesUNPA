package mx.edu.unpa.calificacionesunpa.data.api

import mx.edu.unpa.calificacionesunpa.models.FileResponse
import mx.edu.unpa.calificacionesunpa.models.PdfListResponse
import mx.edu.unpa.calificacionesunpa.models.UploadRequest
import mx.edu.unpa.calificacionesunpa.models.UploadResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StorageApi {

    @POST("storage/upload")
    suspend fun uploadFile(@Body request: UploadRequest): UploadResponse

    @GET("storage/{id}")
    suspend fun getFile(@Path("id") fileId: String): FileResponse

    @GET("storage/selected")
    suspend fun getSeleccionado(): FileResponse?

    @GET("storage/pdf/list")
    suspend fun fetchPdfList(): PdfListResponse

    @PUT("storage/{id}/select")
    suspend fun selectPdf(@Path("id") pdfId: String): UploadResponse

    // ✅ Nuevo método para resetear PDFs seleccionados
    @PUT("storage/reset-selected")
    suspend fun resetSelected(): UploadResponse
}
