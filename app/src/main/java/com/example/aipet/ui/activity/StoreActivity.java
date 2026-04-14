package com.example.aipet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 商店页：展示可购买的一次性好感道具。
 */
public class StoreActivity extends BaseActivity {

    public static final String EXTRA_STORE_ITEM_ID = "store_item_id";
    public static final String EXTRA_STORE_ITEM_NAME = "store_item_name";
    public static final String EXTRA_STORE_AFFECTION_DELTA = "store_affection_delta";

    private RecyclerView rvStoreItems;
    private TextView tvSelectionHint;
    private StoreItem selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        setupScreen(getString(R.string.store_title), true);

        rvStoreItems = bind(R.id.rv_store_items);
        tvSelectionHint = bind(R.id.tv_store_selection_hint);

        List<StoreItem> items = buildConsumableItems();
        rvStoreItems.setLayoutManager(new LinearLayoutManager(this));
        rvStoreItems.setAdapter(new StoreItemAdapter(items, item -> {
            selectedItem = item;
            tvSelectionHint.setText(getString(R.string.store_selected_format, item.name, item.affectionDelta));
        }));

        click(R.id.btn_store_confirm, v -> confirmSelection());
    }

        private List<StoreItem> buildConsumableItems() {
        List<StoreItem> items = new ArrayList<>();
        items.add(new StoreItem(
            "biscuit",
            getString(R.string.food_item_biscuit_name),
            getString(R.string.food_item_biscuit_desc),
            R.drawable.ic_feed,
            3
        ));
        items.add(new StoreItem(
            "fruit",
            getString(R.string.food_item_fruit_name),
            getString(R.string.food_item_fruit_desc),
            R.drawable.ic_feed,
            4
        ));
        items.add(new StoreItem(
            "energy",
            getString(R.string.food_item_energy_name),
            getString(R.string.food_item_energy_desc),
            R.drawable.ic_feed,
            6
        ));
        items.add(new StoreItem(
            "daily_clean",
            getString(R.string.store_item_daily_clean_name),
            getString(R.string.store_item_daily_clean_desc),
            R.drawable.ic_shop,
            2
        ));
        items.add(new StoreItem(
            "daily_toy",
            getString(R.string.store_item_daily_toy_name),
            getString(R.string.store_item_daily_toy_desc),
            R.drawable.ic_shop,
            5
        ));
        return items;
    }

    private void confirmSelection() {
        if (selectedItem == null) {
            showToast(getString(R.string.store_toast_select_first));
            return;
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_STORE_ITEM_ID, selectedItem.id);
        result.putExtra(EXTRA_STORE_ITEM_NAME, selectedItem.name);
        result.putExtra(EXTRA_STORE_AFFECTION_DELTA, selectedItem.affectionDelta);
        setResult(RESULT_OK, result);
        finish();
    }

    private interface OnStoreItemSelectedListener {
        void onSelected(StoreItem item);
    }

    private static class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.StoreItemViewHolder> {

        private final List<StoreItem> items;
        private final OnStoreItemSelectedListener listener;
        private int selectedPosition = RecyclerView.NO_POSITION;

        StoreItemAdapter(List<StoreItem> items, OnStoreItemSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public StoreItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_outfit, parent, false);
            return new StoreItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StoreItemViewHolder holder, int position) {
            StoreItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvDesc.setText(item.description);
            holder.tvBadge.setText(holder.itemView.getContext().getString(R.string.affection_plus_format, item.affectionDelta));
            holder.ivIcon.setImageResource(item.iconRes);

            boolean selected = position == selectedPosition;
            holder.itemView.setBackgroundResource(selected ? R.drawable.bg_store_item_selected : R.drawable.bg_store_item);
            holder.itemView.setElevation(selected ? 8f : 2f);

            holder.itemView.setOnClickListener(v -> {
                int old = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                if (old != RecyclerView.NO_POSITION) {
                    notifyItemChanged(old);
                }
                notifyItemChanged(selectedPosition);
                if (listener != null && selectedPosition != RecyclerView.NO_POSITION) {
                    listener.onSelected(items.get(selectedPosition));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class StoreItemViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivIcon;
            final TextView tvName;
            final TextView tvDesc;
            final TextView tvBadge;

            StoreItemViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_store_item_icon);
                tvName = itemView.findViewById(R.id.tv_store_item_name);
                tvDesc = itemView.findViewById(R.id.tv_store_item_desc);
                tvBadge = itemView.findViewById(R.id.tv_store_item_badge);
            }
        }
    }

    private static class StoreItem {
        final String id;
        final String name;
        final String description;
        final int iconRes;
        final int affectionDelta;

        StoreItem(String id, String name, String description, int iconRes, int affectionDelta) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.iconRes = iconRes;
            this.affectionDelta = affectionDelta;
        }
    }
}
