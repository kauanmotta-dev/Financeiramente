package com.financeiramente.android.ui.configuracoes;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.ui.categorias.CorSelectorAdapter;
import com.financeiramente.android.ui.categorias.IconeSelectorAdapter;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.repository.TagRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagsFragment extends Fragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TagRepository tagRepository;

    private TextInputEditText etNomeTag;
    private ChipGroup cgTags;
    private TextView tvEmptyTags;
    private IconeSelectorAdapter emojiSelectorAdapter;
    private CorSelectorAdapter corSelectorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tags, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_tags).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        tagRepository = AppContext.get(requireContext()).getTagRepository();
        etNomeTag = view.findViewById(R.id.et_nome_tag);
        cgTags = view.findViewById(R.id.cg_tags_existentes);
        tvEmptyTags = view.findViewById(R.id.tv_tags_vazio);
        RecyclerView rvEmojis = view.findViewById(R.id.rv_tags_emojis);
        RecyclerView rvCores = view.findViewById(R.id.rv_tags_cores);

        emojiSelectorAdapter = new IconeSelectorAdapter(
            requireContext(),
            Tag.DEFAULT_EMOJI,
            icone -> { /* seleção mantida no adapter */ });
        rvEmojis.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        rvEmojis.setAdapter(emojiSelectorAdapter);

        corSelectorAdapter = new CorSelectorAdapter(
            requireContext(),
            Tag.DEFAULT_COLOR,
            cor -> { /* seleção mantida no adapter */ });
        rvCores.setLayoutManager(new GridLayoutManager(requireContext(), 8));
        rvCores.setAdapter(corSelectorAdapter);

        MaterialButton btnAdicionar = view.findViewById(R.id.btn_add_tag);
        btnAdicionar.setOnClickListener(v -> criarTag());

        carregarTags();
    }

    private void criarTag() {
        String nome = etNomeTag.getText() != null ? etNomeTag.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.tag_erro_nome), Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                if (tagRepository.buscarPorNome(nome).isPresent()) {
                    postToast(getString(R.string.tag_erro_existente));
                    return;
                }

                String emojiSelecionado = emojiSelectorAdapter != null
                        ? emojiSelectorAdapter.getSelectedIcone()
                        : Tag.DEFAULT_EMOJI;
                String corSelecionada = corSelectorAdapter != null
                        ? corSelectorAdapter.getSelectedCor()
                        : Tag.DEFAULT_COLOR;

                if (emojiSelecionado == null || emojiSelecionado.trim().isEmpty()) {
                    emojiSelecionado = Tag.DEFAULT_EMOJI;
                }
                if (corSelecionada == null || corSelecionada.trim().isEmpty()) {
                    corSelecionada = Tag.DEFAULT_COLOR;
                }

                Tag tag = new Tag(
                        UUID.randomUUID().toString(),
                        nome,
                        emojiSelecionado,
                        corSelecionada,
                        System.currentTimeMillis());
                tagRepository.salvar(tag);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    etNomeTag.setText("");
                    if (emojiSelectorAdapter != null) {
                        emojiSelectorAdapter.setSelectedIcone(Tag.DEFAULT_EMOJI);
                    }
                    if (corSelectorAdapter != null) {
                        corSelectorAdapter.setSelectedCor(Tag.DEFAULT_COLOR);
                    }
                    Toast.makeText(requireContext(), getString(R.string.tag_criada), Toast.LENGTH_SHORT).show();
                });

                carregarTags();
            } catch (Exception e) {
                postToast(e.getMessage() != null ? e.getMessage() : getString(R.string.em_construcao));
            }
        });
    }

    private void carregarTags() {
        executor.execute(() -> {
            List<Tag> tags = tagRepository.listarTodas();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> renderizarTags(tags));
        });
    }

    private void renderizarTags(List<Tag> tags) {
        cgTags.removeAllViews();
        boolean vazio = tags == null || tags.isEmpty();
        tvEmptyTags.setVisibility(vazio ? View.VISIBLE : View.GONE);

        if (vazio) {
            return;
        }

        for (Tag tag : tags) {
            Chip chip = new Chip(requireContext());
            String emoji = (tag.getEmoji() == null || tag.getEmoji().trim().isEmpty())
                    ? Tag.DEFAULT_EMOJI
                    : tag.getEmoji();
            chip.setText(emoji + " " + tag.getNome());
            chip.setClickable(false);
            chip.setCheckable(false);

            String corHex = (tag.getCor() == null || tag.getCor().trim().isEmpty())
                    ? Tag.DEFAULT_COLOR
                    : tag.getCor();
            try {
                int colorInt = Color.parseColor(corHex);
                int bgTint = (colorInt & 0x00FFFFFF) | 0x33000000;
                chip.setChipBackgroundColor(ColorStateList.valueOf(bgTint));
                chip.setChipStrokeWidth(1f);
                chip.setChipStrokeColor(ColorStateList.valueOf(colorInt));
                chip.setTextColor(colorInt);
            } catch (IllegalArgumentException ignored) {
                // Ignora cor inválida e mantém estilo padrão do chip.
            }

            cgTags.addView(chip);
        }
    }

    private void postToast(String msg) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etNomeTag = null;
        cgTags = null;
        tvEmptyTags = null;
        emojiSelectorAdapter = null;
        corSelectorAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
