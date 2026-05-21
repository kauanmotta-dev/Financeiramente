package com.financeiramente.android.ui.lancamentos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;

/**
 * Swipe actions for the lancamentos list:
 *   • Swipe LEFT  → Delete  (red background + trash icon)
 *   • Swipe RIGHT → Edit    (blue background + pencil icon)
 *
 * Headers are not swipeable.
 */
public class LancamentoSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface SwipeListener {
        void onSwipeDelete(int adapterPosition);
        void onSwipeEdit(int adapterPosition);
    }

    private final LancamentoAdapter adapter;
    private final SwipeListener listener;

    private final int colorDelete;
    private final int colorEdit;
    private final Drawable iconDelete;
    private final Drawable iconEdit;
    private final Paint backgroundPaint = new Paint();

    private static final int ICON_MARGIN_DP = 24;

    public LancamentoSwipeCallback(Context context,
                                   LancamentoAdapter adapter,
                                   SwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter  = adapter;
        this.listener = listener;

        colorDelete = ContextCompat.getColor(context, R.color.vermelho_error);
        colorEdit   = ContextCompat.getColor(context, R.color.azul_primary);
        iconDelete  = ContextCompat.getDrawable(context, R.drawable.ic_swipe_delete);
        iconEdit    = ContextCompat.getDrawable(context, R.drawable.ic_swipe_edit);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // no drag-and-drop
    }

    /** Disable swipe on date headers. */
    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
        int pos = viewHolder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_ID && adapter.isHeader(pos)) return 0;
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_ID) return;

        if (direction == ItemTouchHelper.LEFT) {
            listener.onSwipeDelete(pos);
        } else {
            listener.onSwipeEdit(pos);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        View itemView = viewHolder.itemView;
        float density = itemView.getContext().getResources().getDisplayMetrics().density;
        int iconMargin = (int) (ICON_MARGIN_DP * density);
        int iconSize = (int) (24 * density);

        int itemTop    = itemView.getTop();
        int itemBottom = itemView.getBottom();
        int itemHeight = itemBottom - itemTop;
        int iconTop    = itemTop + (itemHeight - iconSize) / 2;
        int iconBottom = iconTop + iconSize;

        if (dX < 0) {
            // Swipe LEFT → delete (red)
            backgroundPaint.setColor(colorDelete);
            c.drawRect(itemView.getRight() + dX, itemTop, itemView.getRight(), itemBottom, backgroundPaint);

            if (iconDelete != null) {
                int iconLeft  = itemView.getRight() - iconMargin - iconSize;
                int iconRight = itemView.getRight() - iconMargin;
                iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                iconDelete.draw(c);
            }
        } else if (dX > 0) {
            // Swipe RIGHT → edit (blue)
            backgroundPaint.setColor(colorEdit);
            c.drawRect(itemView.getLeft(), itemTop, itemView.getLeft() + dX, itemBottom, backgroundPaint);

            if (iconEdit != null) {
                int iconLeft  = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + iconSize;
                iconEdit.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                iconEdit.draw(c);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
