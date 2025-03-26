package mx.edu.unpa.calificacionesunpa.ui.notificaciones;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import mx.edu.unpa.calificacionesunpa.R;

public class FragmentNotificaciones extends Fragment {

    public FragmentNotificaciones() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout correspondiente al fragmento de notificaciones
        return inflater.inflate(R.layout.fragment_notificaciones, container, false);
    }
}
