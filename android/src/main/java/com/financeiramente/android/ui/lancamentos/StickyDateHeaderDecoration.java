package com.financeiramente.android.ui.lancamentos;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;

/**
 * ItemDecoration that draws sticky date headers over the RecyclerView.
 *
 * The decoration finds the nearest header item above the first visible child and
 * renders it pinned at the top of the list. When the next header is scrolling into
 * view it pushes the current sticky header up to avoid overlap.
 */
public class StickyDateHeaderDecoration extends RecyclerView.ItemDecoration {

    private final LancamentoAdapter adapter;
    private View stickyHeaderView;

    public StickyDateHeaderDecoration(LancamentoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {
        if (parent.getChildCount() == 0) return;

        // Find the topmost visible child
        View topChild = parent.getChildAt(0);
        int topPosition = parent.getChildAdapterPosition(topChild);
        if (topPosition == RecyclerView.NO_ID) return;

        // Walk backwards to find the header for the topmost visible item
        int headerPos = getHeaderPositionForItem(topPosition);
        if (headerPos < 0) return;

        // Ensure the sticky view is inflated and bound
        if (stickyHeaderView == null) {
            stickyHeaderView = inflateHeaderView(parent);
        }
        bindHeaderView(stickyHeaderView, headerPos);
        fixLayoutSize(parent, stickyHeaderView);

        // Determine if the next header is pushing this one up
        int contactPoint = stickyHeaderView.getBottom();
        View childInContact = getChildInContact(parent, contactPoint, headerPos);

        float translateY = 0f;
        if (childInContact != null) {
            int nextHeaderPos = parent.getChildAdapterPosition(childInContact);
            if (nextHeaderPos != RecyclerView.NO_ID && adapter.isHeader(nextHeaderPos)) {
                translateY = childInContact.getTop() - stickyHeaderView.getHeight();
            }
        }

        canvas.save();
        canvas.translate(0, translateY);
        stickyHeaderView.draw(canvas);
        canvas.restore();
    }

    /** Walk backwards from position to find the nearest header index. */
    private int getHeaderPositionForItem(int fromPosition) {
        for (int i = fromPosition; i >= 0; i--) {
            if (adapter.isHeader(i)) return i;
        }
        return -1;
    }

    /** Returns the child view whose top is at or just below contactPoint. */
    private View getChildInContact(RecyclerView parent, int contactPoint, int currentHeaderPos) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getBottom() > contactPoint && child.getTop() <= contactPoint) {
                int adapterPos = parent.getChildAdapterPosition(child);
                if (adapterPos != currentHeaderPos) return child;
            }
        }
        return null;
    }

    private View inflateHeaderView(RecyclerView parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_header, parent, false);
    }

    private void bindHeaderView(View view, int headerPosition) {
        // Create a temporary HeaderViewHolder for binding
        LancamentoAdapter.HeaderViewHolder holder = new LancamentoAdapter.HeaderViewHolder(view);
        adapter.onBindViewHolder(holder, headerPosition);
    }

    private void fixLayoutSize(RecyclerView parent, View view) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        int childWidth = ViewGroup.getChildMeasureSpec(
                widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams() != null
                        ? view.getLayoutParams().width : ViewGroup.LayoutParams.MATCH_PARENT);
        int childHeight = ViewGroup.getChildMeasureSpec(
                heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams() != null
                        ? view.getLayoutParams().height : ViewGroup.LayoutParams.WRAP_CONTENT);

        view.measure(childWidth, childHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }
}
