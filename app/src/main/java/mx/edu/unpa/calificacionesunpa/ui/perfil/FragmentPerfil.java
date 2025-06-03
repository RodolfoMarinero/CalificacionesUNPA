package mx.edu.unpa.calificacionesunpa.ui.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import mx.edu.unpa.calificacionesunpa.R;
import mx.edu.unpa.calificacionesunpa.providers.AuthGoogleProvider;
import mx.edu.unpa.calificacionesunpa.providers.AuthProvider;

public class FragmentPerfil extends Fragment {

    private TextView tvNombre, tvMatricula, tvCarrera, tvPromedio, tvCodigoBarras;
    private ImageView ivCodigoBarras;
    private static final int RC_SIGN_IN = 9001;
    private AuthGoogleProvider authGoogleProvider;

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

        SignInButton btnGoogleSignIn = view.findViewById(R.id.tvGoogle);
        authGoogleProvider = new AuthGoogleProvider(requireActivity());

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = authGoogleProvider.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                authGoogleProvider.firebaseAuthWithGoogle(account)
                        .addOnCompleteListener(requireActivity(), task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseUser user = authGoogleProvider.getCurrentUser();
                                Toast.makeText(getContext(), "Bienvenido: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Error en autenticación", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

}
