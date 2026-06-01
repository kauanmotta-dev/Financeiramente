package com.financeiramente.android.ui.configuracoes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

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
        view.findViewById(R.id.btn_back_configuracoes).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(
                R.id.nav_dashboard,
                null,
                new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.nav_graph, false)
                    .build()));
        view.findViewById(R.id.btn_categorias).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_configuracoesFragment_to_categoriasFragment));
        view.findViewById(R.id.btn_tags).setOnClickListener(v ->
            Navigation.findNavController(v)
                .navigate(R.id.action_configuracoesFragment_to_tagsFragment));
        view.findViewById(R.id.btn_cartoes).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("sourceScreen", R.id.nav_configuracoes);
            Navigation.findNavController(v)
                .navigate(R.id.action_configuracoesFragment_to_nav_cartoes, args);
        });
    }
}
