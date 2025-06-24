package mx.edu.unpa.calificacionesunpa.ui.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.Locale;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.service.PromedioCalculatorService;
import mx.edu.unpa.calificacionesunpa.ui.changePass.ChangePassword;


public class FragmentPerfil extends Fragment {

    private TextView tvNombre, tvMatricula, tvCarrera, tvPromedio, tvCodigoBarras;
    private ImageView ivCodigoBarras;
    private PromedioCalculatorService promedioCalculatorService;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle("Perfil del Alumno");


        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvMatricula = view.findViewById(R.id.tvMatriculaPerfil);
        tvCarrera = view.findViewById(R.id.tvCarreraPerfil);
        tvPromedio = view.findViewById(R.id.tvPromedioPerfil);
        ivCodigoBarras = view.findViewById(R.id.ivBarcode);
        tvCodigoBarras = view.findViewById(R.id.tvBarcodeNumber);

        TextView btnCambiarPass = view.findViewById(R.id.cambiarPass);
        btnCambiarPass.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Click detectado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), ChangePassword.class);
            startActivity(intent);
        });

        promedioCalculatorService = PromedioCalculatorService.INSTANCE;
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
        tvPromedio.setText(String.format("%.1f", promedioCalculatorService.getPromedioGeneral()));
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