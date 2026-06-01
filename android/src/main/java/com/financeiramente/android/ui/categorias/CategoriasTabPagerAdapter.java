package com.financeiramente.android.ui.categorias;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CategoriasTabPagerAdapter extends FragmentStateAdapter {

    private final Fragment parentFragment;

    public CategoriasTabPagerAdapter(@NonNull Fragment parentFragment) {
        super(parentFragment);
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return CategoriasPorTipoFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
