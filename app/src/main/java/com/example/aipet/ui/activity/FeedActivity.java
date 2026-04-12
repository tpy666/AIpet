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
 * 喂食页：图标 + 文字说明的食物选择。
 */
public class FeedActivity extends BaseActivity {

    public static final String EXTRA_FOOD_NAME = "food_name";
    public static final String EXTRA_FOOD_AFFECTION_DELTA = "food_affection_delta";

    private RecyclerView rvFoods;
    private TextView tvFoodHint;
    private FoodItem selectedFood;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupScreen(getString(R.string.feed_title), true);

        rvFoods = bind(R.id.rv_food_items);
        tvFoodHint = bind(R.id.tv_food_selection_hint);

        List<FoodItem> foods = buildFoods();
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(new FoodAdapter(foods, item -> {
            selectedFood = item;
            tvFoodHint.setText(getString(R.string.feed_selected_format, item.name, item.affectionDelta));
        }));

        click(R.id.btn_feed_confirm, v -> confirmFeed());
    }

    private List<FoodItem> buildFoods() {
        List<FoodItem> items = new ArrayList<>();
        items.add(new FoodItem(
                getString(R.string.food_item_biscuit_name),
                getString(R.string.food_item_biscuit_desc),
                R.drawable.ic_feed,
                4
        ));
        items.add(new FoodItem(
                getString(R.string.food_item_fruit_name),
                getString(R.string.food_item_fruit_desc),
                R.drawable.ic_feed,
                5
        ));
        items.add(new FoodItem(
                getString(R.string.food_item_energy_name),
                getString(R.string.food_item_energy_desc),
                R.drawable.ic_feed,
                6
        ));
        return items;
    }

    private void confirmFeed() {
        if (selectedFood == null) {
            showToast(getString(R.string.feed_toast_select_first));
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_FOOD_NAME, selectedFood.name);
        data.putExtra(EXTRA_FOOD_AFFECTION_DELTA, selectedFood.affectionDelta);
        setResult(RESULT_OK, data);
        finish();
    }

    private interface OnFoodSelectedListener {
        void onSelected(FoodItem item);
    }

    private static class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

        private final List<FoodItem> items;
        private final OnFoodSelectedListener listener;
        private int selectedPosition = RecyclerView.NO_POSITION;

        FoodAdapter(List<FoodItem> items, OnFoodSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_option, parent, false);
            return new FoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            FoodItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvDesc.setText(item.description);
            holder.tvDelta.setText(holder.itemView.getContext().getString(R.string.affection_plus_format, item.affectionDelta));
            holder.ivIcon.setImageResource(item.iconRes);

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

        static class FoodViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivIcon;
            final TextView tvName;
            final TextView tvDesc;
            final TextView tvDelta;

            FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_food_icon);
                tvName = itemView.findViewById(R.id.tv_food_name);
                tvDesc = itemView.findViewById(R.id.tv_food_desc);
                tvDelta = itemView.findViewById(R.id.tv_food_delta);
            }
        }
    }

    private static class FoodItem {
        final String name;
        final String description;
        final int iconRes;
        final int affectionDelta;

        FoodItem(String name, String description, int iconRes, int affectionDelta) {
            this.name = name;
            this.description = description;
            this.iconRes = iconRes;
            this.affectionDelta = affectionDelta;
        }
    }
}
