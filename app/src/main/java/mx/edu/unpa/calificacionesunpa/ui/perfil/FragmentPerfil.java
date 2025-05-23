package mx.edu.unpa.calificacionesunpa.ui.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import mx.edu.unpa.calificacionesunpa.R;

public class FragmentPerfil extends Fragment {

    private TextView tvNombre, tvMatricula, tvCarrera, tvPromedio, tvCodigoBarras;
    private ImageView ivCodigoBarras;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil);
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil);
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil);
        ivCodigoBarras = view.findViewById(R.id.ivBarcode);
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber);

        if (getArguments() != null) {
            String nombre = getArguments().getString("nombre", "");
            String matricula = getArguments().getString("matricula", "");
            String carrera = getArguments().getString("carrera", "");
            String promedio = getArguments().getString("promedio", "");

            tvNombre.setText(nombre);
            tvMatricula.setText(matricula);
            tvCarrera.setText(carrera);
            tvPromedio.setText(promedio);
            tvCodigoBarras.setText(matricula);

            generarCodigoBarras(matricula);
        }

        return view;
    }

    private void generarCodigoBarras(String texto) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(texto, BarcodeFormat.CODE_128, 600, 200);
            ivCodigoBarras.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
