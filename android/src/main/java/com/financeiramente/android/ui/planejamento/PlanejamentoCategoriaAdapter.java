package com.financeiramente.android.ui.planejamento;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlanejamentoCategoriaAdapter extends ArrayAdapter<Categoria> {

    private final List<PlanejamentoCategoria> itens;
    private final Map<String, Double> limitesEditados;
    private final LimiteChangeListener listener;

    public interface LimiteChangeListener {
        void onLimiteChanged(String categoriaId, double limite);
    }

    public PlanejamentoCategoriaAdapter(@NonNull Context context,
                                         @NonNull List<Categoria> categorias,
                                         @NonNull List<PlanejamentoCategoria> itens,
                                         @NonNull Map<String, Double> limitesEditados,
                                         @NonNull LimiteChangeListener listener) {
        super(context, 0, categorias);
        this.itens = itens;
        this.limitesEditados = limitesEditados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_planejamento_categoria, parent, false);
        }

        Categoria categoria = getItem(position);
        if (categoria == null) return view;

        TextView tvNome = view.findViewById(R.id.tv_categoria_nome_plano);
        TextInputEditText etLimite = view.findViewById(R.id.et_limite);

        tvNome.setText(categoria.getNome());

        // Remove listener anterior para evitar re-trigger
        etLimite.setTag(null);
        etLimite.removeTextChangedListener((TextWatcher) etLimite.getTag(R.id.et_limite));

        // Preenche o limite atual
        double limiteAtual = 0.0;
        if (limitesEditados.containsKey(categoria.getId())) {
            limiteAtual = limitesEditados.get(categoria.getId());
        } else {
            for (PlanejamentoCategoria item : itens) {
                if (item.getCategoriaId().equals(categoria.getId())) {
                    limiteAtual = item.getLimite();
                    break;
                }
            }
        }
        if (limiteAtual > 0) {
            etLimite.setText(String.format(Locale.getDefault(), "%.2f", limiteAtual));
        } else {
            etLimite.getText().clear();
        }

        final String categoriaId = categoria.getId();
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double val = Double.parseDouble(s.toString().replace(",", "."));
                    limitesEditados.put(categoriaId, val);
                    listener.onLimiteChanged(categoriaId, val);
                } catch (NumberFormatException ignored) {
                    // Campo vazio ou inválido — ignora
                }
            }
        };
        etLimite.setTag(R.id.et_limite, watcher);
        etLimite.addTextChangedListener(watcher);

        return view;
    }
}
