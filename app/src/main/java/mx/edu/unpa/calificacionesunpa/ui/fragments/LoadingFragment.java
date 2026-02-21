package mx.edu.unpa.calificacionesunpa.ui.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

import mx.edu.unpa.calificacionesunpa.R;

public class LoadingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        LottieAnimationView lottie = view.findViewById(R.id.lottieLoading);
        lottie.playAnimation(); // Iniciar animación automáticamente
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LottieAnimationView lottie = getView().findViewById(R.id.lottieLoading);
        if (lottie != null) {
            lottie.cancelAnimation(); // Detener animación al destruir
        }
    }
}