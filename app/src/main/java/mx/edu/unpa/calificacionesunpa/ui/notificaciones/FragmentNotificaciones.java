package mx.edu.unpa.calificacionesunpa.ui.notificaciones;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import mx.edu.unpa.calificacionesunpa.R;

import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class FragmentNotificaciones extends Fragment {

    private ArrayList<String> notificaciones;
    private ArrayAdapter<String> adapter;

    public FragmentNotificaciones() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout correspondiente al fragmento de notificaciones
        View rootView = inflater.inflate(R.layout.fragment_notificaciones, container, false);

        // Inicializar el ListView
        ListView lvNotificaciones = rootView.findViewById(R.id.lvNotificaciones);

        // Lista de ejemplo de notificaciones
        notificaciones = new ArrayList<>();
        notificaciones.add("Nueva calificación en Matemáticas: 9.5");
        notificaciones.add("Recordatorio: Examen de Historia mañana.");
        notificaciones.add("Nueva actividad disponible: Proyecto de Biología");
        notificaciones.add("Tarea pendiente en Física: Entregar antes del viernes.");
        notificaciones.add("Aviso importante: Cambio de horario en programación.");

        // Crear un adaptador personalizado para llenar el ListView con las notificaciones
        adapter = new ArrayAdapter<String>(getContext(), R.layout.notification_item, R.id.notification_text, notificaciones) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Inflar el layout personalizado
                View view = super.getView(position, convertView, parent);

                // Configurar el botón de eliminar
                Button btnEliminar = view.findViewById(R.id.btnEliminar);
                btnEliminar.setOnClickListener(v -> {
                    // Eliminar la notificación
                    notificaciones.remove(position);
                    // Notificar al adaptador que la lista ha cambiado
                    adapter.notifyDataSetChanged();
                    // Mostrar un mensaje
                    Toast.makeText(getContext(), "Notificación eliminada", Toast.LENGTH_SHORT).show();
                });

                return view;
            }
        };

        // Asignar el adaptador al ListView
        lvNotificaciones.setAdapter(adapter);

        return rootView;
    }
}
