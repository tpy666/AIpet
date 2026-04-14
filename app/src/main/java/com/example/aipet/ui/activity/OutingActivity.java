package com.example.aipet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.store.OutingBackgroundStore;
import com.example.aipet.util.SceneImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 外出页：选择不同外出地点。
 */
public class OutingActivity extends BaseActivity {

    private static final String SECTION_OUTING = "outing";
    public static final String EXTRA_OUTING_PLACE = "outing_place";
    public static final String EXTRA_OUTING_ENVIRONMENT = "outing_environment";
    public static final String EXTRA_OUTING_IMAGE_URL = "outing_image_url";
    public static final String EXTRA_OUTING_AFFECTION_DELTA = "outing_affection_delta";

    private RecyclerView rvPlaces;
    private TextView tvHint;
    private Button btnCustomBackground;
    private OutingBackgroundStore.Entry selectedPlace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outing);
        setupScreen(getString(R.string.outing_title), true);

        rvPlaces = bind(R.id.rv_outing_places);
        tvHint = bind(R.id.tv_outing_selection_hint);
        btnCustomBackground = bind(R.id.btn_outing_custom_background);

        List<OutingBackgroundStore.Entry> places = buildPlaces();
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        rvPlaces.setAdapter(new OutingAdapter(places, item -> {
            selectedPlace = item;
            tvHint.setText(getString(R.string.outing_selected_format, item.placeName, item.affectionDelta));
        }));

        btnCustomBackground.setOnClickListener(v -> navigateTo(UiNavigator.toSettings(this, SECTION_OUTING)));
        click(R.id.btn_outing_confirm, v -> confirmOuting());
    }

    private List<OutingBackgroundStore.Entry> buildPlaces() {
        List<OutingBackgroundStore.Entry> places = new ArrayList<>();
        places.add(new OutingBackgroundStore.Entry(
                -1L,
                getString(R.string.outing_place_park_name),
                getString(R.string.outing_place_park_desc),
            "res:outside_city_park",
                4
        ));
        places.add(new OutingBackgroundStore.Entry(
                -2L,
                getString(R.string.outing_place_seaside_name),
                getString(R.string.outing_place_seaside_desc),
            "res:outside_seaside_boardwalk",
                5
        ));
        places.add(new OutingBackgroundStore.Entry(
                -3L,
                getString(R.string.outing_place_street_name),
                getString(R.string.outing_place_street_desc),
            "res:outside_pet_district",
                6
        ));
        places.addAll(new OutingBackgroundStore(this).loadAll());
        return places;
    }

    private void confirmOuting() {
        if (selectedPlace == null) {
            showToast(getString(R.string.outing_toast_select_first));
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_OUTING_PLACE, selectedPlace.placeName);
        data.putExtra(EXTRA_OUTING_ENVIRONMENT, selectedPlace.environment);
        data.putExtra(EXTRA_OUTING_IMAGE_URL, selectedPlace.imageUrl);
        data.putExtra(EXTRA_OUTING_AFFECTION_DELTA, selectedPlace.affectionDelta);
        setResult(RESULT_OK, data);
        finish();
    }

    private interface OnPlaceSelectedListener {
        void onSelected(OutingBackgroundStore.Entry item);
    }

    private static class OutingAdapter extends RecyclerView.Adapter<OutingAdapter.OutingViewHolder> {

        private final List<OutingBackgroundStore.Entry> items;
        private final OnPlaceSelectedListener listener;
        private int selectedPosition = RecyclerView.NO_POSITION;

        OutingAdapter(List<OutingBackgroundStore.Entry> items, OnPlaceSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OutingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outing_place, parent, false);
            return new OutingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OutingViewHolder holder, int position) {
            OutingBackgroundStore.Entry item = items.get(position);
            holder.tvName.setText(item.placeName);
            holder.tvDesc.setText(item.environment);
            holder.tvDelta.setText(holder.itemView.getContext().getString(R.string.affection_plus_format, item.affectionDelta));
            bindPreview(holder, item);

            boolean selected = position == selectedPosition;
            holder.itemView.setBackgroundResource(selected ? R.drawable.bg_store_item_selected : R.drawable.bg_store_item);
            holder.itemView.setOnClickListener(v -> {
                int old = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                if (old != RecyclerView.NO_POSITION) {
                    notifyItemChanged(old);
                }
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(selectedPosition);
                }
                if (listener != null && selectedPosition != RecyclerView.NO_POSITION) {
                    listener.onSelected(items.get(selectedPosition));
                }
            });
        }

        private void bindPreview(@NonNull OutingViewHolder holder, @NonNull OutingBackgroundStore.Entry item) {
            if (item.imageUrl == null || item.imageUrl.trim().isEmpty()) {
                holder.ivPreview.setVisibility(View.GONE);
                holder.ivPreview.setImageDrawable(null);
                return;
            }
            holder.ivPreview.setVisibility(View.VISIBLE);
            SceneImageLoader.loadInto(holder.ivPreview, item.imageUrl.trim(), 0);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OutingViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivPreview;
            final TextView tvName;
            final TextView tvDesc;
            final TextView tvDelta;

            OutingViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPreview = itemView.findViewById(R.id.iv_outing_place_preview);
                tvName = itemView.findViewById(R.id.tv_outing_place_name);
                tvDesc = itemView.findViewById(R.id.tv_outing_place_desc);
                tvDelta = itemView.findViewById(R.id.tv_outing_place_delta);
            }
        }
    }
}
