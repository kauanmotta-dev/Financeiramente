package com.financeiramente.android.ui.configuracoes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;

public class ConfiguracoesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_categorias).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_configuracoesFragment_to_categoriasFragment));
        view.findViewById(R.id.btn_recorrentes).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_configuracoesFragment_to_recorrentesFragment));
    }
}
