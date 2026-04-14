package com.example.aipet.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.ui.avatar.AvatarDiffStore;
import com.example.aipet.util.SceneImageLoader;
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
    private Button btnImportLocal;
    private Button btnImportCharacterAsset;
    private Button btnUploadDiff;
    private PetStore petStore;
    private Pet activePet;
    private OutfitItem selectedItem;
    private List<OutfitItem> outfitItems;
    private OutfitAdapter outfitAdapter;
    private ActivityResultLauncher<String[]> dressImagePickerLauncher;
    private ActivityResultLauncher<String[]> diffImagePickerLauncher;
    private String pendingDiffOutfitId;
    private String pendingDiffEmotion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dress_up);
        setupScreen(getString(R.string.dress_up_title), true);

        petStore = UtilHub.petStore(this);
        dressImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onDressImagePicked
        );
        diffImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::onDiffImagePicked
        );
        rvOutfits = bind(R.id.rv_dress_up_items);
        tvHint = bind(R.id.tv_dress_up_hint);
        btnImportLocal = bind(R.id.btn_dress_up_import_local);
        btnImportCharacterAsset = bind(R.id.btn_dress_up_import_asset);
        btnUploadDiff = bind(R.id.btn_dress_up_upload_diff);

        loadActivePet();
        rvOutfits.setLayoutManager(new LinearLayoutManager(this));
        outfitItems = loadOutfits();
        outfitAdapter = new OutfitAdapter(outfitItems, item -> {
            selectedItem = item;
            tvHint.setText(getString(R.string.dress_up_hint_select) + "：" + item.name);
        });
        rvOutfits.setAdapter(outfitAdapter);

        btnImportLocal.setOnClickListener(v -> importLocalDressImage());
        btnImportCharacterAsset.setOnClickListener(v -> pickAssetDressImage());
        btnUploadDiff.setOnClickListener(v -> uploadDressDiffImage());

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

    private List<OutfitItem> loadOutfits() {
        List<OutfitItem> list = SPUtils.getList(this, Constants.KEY_DRESS_UP_ITEMS, OutfitItem.class);
        if (list == null || list.isEmpty()) {
            list = buildDefaultOutfits();
            saveOutfits(list);
        }
        for (OutfitItem item : list) {
            if (item != null && !TextUtils.isEmpty(item.id)) {
                AvatarDiffStore.ensurePlaceholders(this, item.id);
            }
        }
        return list;
    }

    private List<OutfitItem> buildDefaultOutfits() {
        List<OutfitItem> list = new ArrayList<>();
        list.add(new OutfitItem("default", "默认服装", "可编辑默认服装", "asset:character/imiss.png", true));
        list.add(new OutfitItem("hoodie", "日常服装", "默认日常款，可替换", "res:ic_outfit", true));
        list.add(new OutfitItem("formal", "正式服装", "默认正式款，可替换", "res:ic_outfit", true));
        return list;
    }

    private void saveOutfits(@NonNull List<OutfitItem> list) {
        SPUtils.putList(this, Constants.KEY_DRESS_UP_ITEMS, list);
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

        if (TextUtils.isEmpty(selectedItem.imageSource)) {
            showToast(getString(R.string.dress_up_missing_image));
            return;
        }

        activePet.setAvatar("outfit:" + selectedItem.id);
        activePet.setAppearance(selectedItem.description);
        petStore.updatePet(activePet);
        showToast(getString(R.string.dress_up_success, selectedItem.name));
        finish();
    }

    private void importLocalDressImage() {
        if (dressImagePickerLauncher == null) {
            showToast("服装图选择器未初始化");
            return;
        }
        dressImagePickerLauncher.launch(new String[]{"image/*"});
    }

    private void onDressImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        applyDressImageSource(uri.toString(), "本地导入服装");
    }

    private void pickAssetDressImage() {
        try {
            String[] files = getAssets().list("character");
            if (files == null || files.length == 0) {
                showToast(getString(R.string.settings_avatar_asset_empty));
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.settings_avatar_asset_pick_title)
                    .setItems(files, (dialog, which) -> {
                        String selected = files[which];
                        applyDressImageSource("asset:character/" + selected, selected);
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        } catch (Exception e) {
            showToast(getString(R.string.settings_avatar_asset_empty));
        }
    }

    private void applyDressImageSource(@NonNull String source, @NonNull String displayName) {
        if (selectedItem == null) {
            OutfitItem custom = new OutfitItem(
                    "custom_" + System.currentTimeMillis(),
                    "自定义服装",
                    "导入的角色服装图",
                    source,
                    false
            );
            outfitItems.add(custom);
            selectedItem = custom;
            AvatarDiffStore.ensurePlaceholders(this, custom.id);
        } else {
            selectedItem.imageSource = source;
            AvatarDiffStore.ensurePlaceholders(this, selectedItem.id);
        }

        saveOutfits(outfitItems);
        if (outfitAdapter != null) {
            outfitAdapter.notifyDataSetChanged();
        }
        tvHint.setText(getString(R.string.dress_up_import_success, displayName));
    }

    private void uploadDressDiffImage() {
        if (selectedItem == null) {
            showSelectOutfitRequiredDialog();
            return;
        }
        showEmotionPickerForDiff();
    }

    private void showEmotionPickerForDiff() {
        String[] labels = AvatarDiffStore.supportedEmotionLabels();
        new AlertDialog.Builder(this)
                .setTitle(R.string.dress_up_diff_pick_emotion)
                .setItems(labels, (dialog, which) -> {
                    pendingDiffOutfitId = selectedItem == null ? null : selectedItem.id;
                    pendingDiffEmotion = AvatarDiffStore.emotionTagFromIndex(which);
                    if (diffImagePickerLauncher != null) {
                        diffImagePickerLauncher.launch(new String[]{"image/*"});
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void onDiffImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (TextUtils.isEmpty(pendingDiffOutfitId) || TextUtils.isEmpty(pendingDiffEmotion)) {
            showSelectOutfitRequiredDialog();
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        boolean saved = AvatarDiffStore.saveDiffImage(this, pendingDiffOutfitId, pendingDiffEmotion, uri);
        if (saved) {
            tvHint.setText(getString(R.string.dress_up_diff_upload_success, pendingDiffOutfitId, pendingDiffEmotion));
        } else {
            showToast(getString(R.string.dress_up_diff_upload_failed));
        }
    }

    private void showSelectOutfitRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dress_up_diff_need_outfit_title)
                .setMessage(R.string.dress_up_diff_need_outfit_message)
                .setPositiveButton(R.string.dress_up_diff_need_outfit_confirm, null)
                .show();
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
            holder.tvBadge.setText(item.editableDefault ? "可编辑" : "自定义");
            SceneImageLoader.loadInto(holder.ivIcon, item.imageSource, R.drawable.ic_outfit);

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
        public String id;
        public String name;
        public String description;
        public String imageSource;
        public boolean editableDefault;

        public OutfitItem() {
        }

        OutfitItem(String id, String name, String description, String imageSource, boolean editableDefault) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.imageSource = imageSource;
            this.editableDefault = editableDefault;
        }
    }
}
