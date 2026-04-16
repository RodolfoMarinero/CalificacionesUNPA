package mx.edu.unpa.calificacionesunpa.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mx.edu.unpa.calificacionesunpa.R;

public class LoadingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Simplemente inflamos la vista con el nuevo diseño estático
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }
}