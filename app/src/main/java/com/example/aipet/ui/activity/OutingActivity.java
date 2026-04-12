package com.example.aipet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 外出页：选择不同外出地点。
 */
public class OutingActivity extends BaseActivity {

    public static final String EXTRA_OUTING_PLACE = "outing_place";
    public static final String EXTRA_OUTING_AFFECTION_DELTA = "outing_affection_delta";

    private RecyclerView rvPlaces;
    private TextView tvHint;
    private PlaceItem selectedPlace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outing);
        setupScreen(getString(R.string.outing_title), true);

        rvPlaces = bind(R.id.rv_outing_places);
        tvHint = bind(R.id.tv_outing_selection_hint);

        List<PlaceItem> places = buildPlaces();
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        rvPlaces.setAdapter(new OutingAdapter(places, item -> {
            selectedPlace = item;
            tvHint.setText(getString(R.string.outing_selected_format, item.name, item.affectionDelta));
        }));

        click(R.id.btn_outing_confirm, v -> confirmOuting());
    }

    private List<PlaceItem> buildPlaces() {
        List<PlaceItem> places = new ArrayList<>();
        places.add(new PlaceItem(
                getString(R.string.outing_place_park_name),
                getString(R.string.outing_place_park_desc),
                4
        ));
        places.add(new PlaceItem(
                getString(R.string.outing_place_seaside_name),
                getString(R.string.outing_place_seaside_desc),
                5
        ));
        places.add(new PlaceItem(
                getString(R.string.outing_place_street_name),
                getString(R.string.outing_place_street_desc),
                6
        ));
        return places;
    }

    private void confirmOuting() {
        if (selectedPlace == null) {
            showToast(getString(R.string.outing_toast_select_first));
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_OUTING_PLACE, selectedPlace.name);
        data.putExtra(EXTRA_OUTING_AFFECTION_DELTA, selectedPlace.affectionDelta);
        setResult(RESULT_OK, data);
        finish();
    }

    private interface OnPlaceSelectedListener {
        void onSelected(PlaceItem item);
    }

    private static class OutingAdapter extends RecyclerView.Adapter<OutingAdapter.OutingViewHolder> {

        private final List<PlaceItem> items;
        private final OnPlaceSelectedListener listener;
        private int selectedPosition = RecyclerView.NO_POSITION;

        OutingAdapter(List<PlaceItem> items, OnPlaceSelectedListener listener) {
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
            PlaceItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvDesc.setText(item.desc);
            holder.tvDelta.setText(holder.itemView.getContext().getString(R.string.affection_plus_format, item.affectionDelta));

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

        static class OutingViewHolder extends RecyclerView.ViewHolder {
            final TextView tvName;
            final TextView tvDesc;
            final TextView tvDelta;

            OutingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_outing_place_name);
                tvDesc = itemView.findViewById(R.id.tv_outing_place_desc);
                tvDelta = itemView.findViewById(R.id.tv_outing_place_delta);
            }
        }
    }

    private static class PlaceItem {
        final String name;
        final String desc;
        final int affectionDelta;

        PlaceItem(String name, String desc, int affectionDelta) {
            this.name = name;
            this.desc = desc;
            this.affectionDelta = affectionDelta;
        }
    }
}
