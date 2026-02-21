package mx.edu.unpa.calificacionesunpa

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mx.edu.unpa.calificacionesunpa.data.repository.LoginRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel  @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {


    fun exitSession(){
        repository.exitSession()
    }
}