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
import com.example.aipet.data.model.Pet;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.SPUtils;
import com.example.aipet.util.Constants;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.PetStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 换装页面：从商店风格中选择服装并直接应用到当前角色。
 */
public class DressUpActivity extends BaseActivity {

    private RecyclerView rvOutfits;
    private TextView tvHint;
    private PetStore petStore;
    private Pet activePet;
    private OutfitItem selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dress_up);
        setupScreen(getString(R.string.dress_up_title), true);

        petStore = UtilHub.petStore(this);
        rvOutfits = bind(R.id.rv_dress_up_items);
        tvHint = bind(R.id.tv_dress_up_hint);

        loadActivePet();
        rvOutfits.setLayoutManager(new LinearLayoutManager(this));
        rvOutfits.setAdapter(new OutfitAdapter(buildOutfits(), item -> {
            selectedItem = item;
            tvHint.setText(getString(R.string.dress_up_hint_select) + "：" + item.name);
        }));

        click(R.id.btn_dress_up_apply, v -> applySelectedOutfit());
    }

    private void loadActivePet() {
        long savedId = 0L;
        try {
            savedId = Long.parseLong(SPUtils.getString(this, Constants.KEY_ACTIVE_PET_ID, "0"));
        } catch (Exception ignored) {
        }
        activePet = savedId > 0 ? petStore.getPetById(savedId) : petStore.getFirstPetOrNull();
    }

    private List<OutfitItem> buildOutfits() {
        List<OutfitItem> list = new ArrayList<>();
        list.add(new OutfitItem("default", getString(R.string.store_item_default_name), getString(R.string.store_item_default_desc), R.drawable.ic_outfit));
        list.add(new OutfitItem("hoodie", getString(R.string.store_item_hoodie_name), getString(R.string.store_item_hoodie_desc), R.drawable.ic_outfit));
        list.add(new OutfitItem("formal", getString(R.string.store_item_formal_name), getString(R.string.store_item_formal_desc), R.drawable.ic_outfit));
        return list;
    }

    private void applySelectedOutfit() {
        if (activePet == null) {
            showToast(getString(R.string.dress_up_missing_role));
            return;
        }
        if (selectedItem == null) {
            showToast(getString(R.string.dress_up_hint_select));
            return;
        }

        activePet.setAvatar("outfit:" + selectedItem.id);
        activePet.setAppearance(selectedItem.description);
        petStore.updatePet(activePet);
        showToast(getString(R.string.dress_up_success, selectedItem.name));
        finish();
    }

    private interface OnOutfitSelectedListener {
        void onSelected(OutfitItem item);
    }

    private static class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {
        private final List<OutfitItem> items;
        private final OnOutfitSelectedListener listener;
        private int selectedPosition = RecyclerView.NO_POSITION;

        OutfitAdapter(List<OutfitItem> items, OnOutfitSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_outfit, parent, false);
            return new OutfitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
            OutfitItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvDesc.setText(item.description);
            holder.tvBadge.setText("换装");
            holder.ivIcon.setImageResource(item.iconResId);

            boolean selected = position == selectedPosition;
            holder.itemView.setBackgroundResource(selected ? R.drawable.bg_store_item_selected : R.drawable.bg_store_item);
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

        static class OutfitViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivIcon;
            final TextView tvName;
            final TextView tvDesc;
            final TextView tvBadge;

            OutfitViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_store_item_icon);
                tvName = itemView.findViewById(R.id.tv_store_item_name);
                tvDesc = itemView.findViewById(R.id.tv_store_item_desc);
                tvBadge = itemView.findViewById(R.id.tv_store_item_badge);
            }
        }
    }

    private static class OutfitItem {
        final String id;
        final String name;
        final String description;
        final int iconResId;

        OutfitItem(String id, String name, String description, int iconResId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.iconResId = iconResId;
        }
    }
}
