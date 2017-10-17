package org.feedhenry.mcp.mcp_demo.helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import org.feedhenry.mcp.mcp_demo.adapter.ShoppingItemAdapter;
import org.feedhenry.mcp.mcp_demo.model.ShoppingItem;


public class SwipeTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final OnItemSwipeListener listener;

    public SwipeTouchHelper(OnItemSwipeListener listener) {
        super(0, ItemTouchHelper.RIGHT);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
        ShoppingItemAdapter.ShoppingItemViewHolder v =
                (ShoppingItemAdapter.ShoppingItemViewHolder) viewHolder;
        listener.onItemSwipe(v.getItem());
    }

    public interface OnItemSwipeListener {
        public void onItemSwipe(ShoppingItem item);
    }

}