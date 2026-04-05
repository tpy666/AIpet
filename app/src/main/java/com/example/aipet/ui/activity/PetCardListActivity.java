package com.example.aipet.ui.activity;

import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.ui.adapter.PetCardAdapter;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.PetStore;

import java.util.List;

/**
 * 角色卡列表页面
 * 显示已创建的所有角色卡片列表
 */
public class PetCardListActivity extends BaseActivity {

    private RecyclerView rvPetCards;
    private View emptyStateContainer;
    private TextView tvEmptyHint;
    private Button btnGoCreatePet;
    private PetCardAdapter adapter;
    private List<Pet> petList;
    private PetStore petStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_card_list);

        setupScreen("我的角色", true);
        petStore = UtilHub.petStore(this);
        initViews();
        loadPetList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetList();
    }

    private void initViews() {
        rvPetCards = bind(R.id.rv_pet_cards);
        emptyStateContainer = bind(R.id.layout_empty_state);
        tvEmptyHint = bind(R.id.tv_empty_hint);
        btnGoCreatePet = bind(R.id.btn_go_create_pet);

        btnGoCreatePet.setOnClickListener(v -> {
            navigateTo(UiNavigator.toCreatePet(this));
        });

        rvPetCards.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetCardAdapter(pet -> {
            // 点击角色卡跳转到 ChatActivity，并传递当前选择的 Pet 对象
            navigateTo(UiNavigator.toChat(this, pet));
        }, this::confirmDeletePet);
        rvPetCards.setAdapter(adapter);
    }

    private void confirmDeletePet(Pet pet) {
        new AlertDialog.Builder(this)
                .setTitle("删除角色")
                .setMessage("确定要删除「" + pet.getName() + "」吗？此操作不可撤销。")
                .setPositiveButton("删除", (dialog, which) -> {
                    boolean removed = petStore.deletePetById(pet.getId());
                    if (removed) {
                        Toast.makeText(this, "已删除角色", Toast.LENGTH_SHORT).show();
                        loadPetList();
                    } else {
                        Toast.makeText(this, "删除失败，未找到该角色", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadPetList() {
        // 从 SharedPreferences 读取角色列表
        petList = petStore.getAllPets();
        if (petList != null && !petList.isEmpty()) {
            adapter.setPetList(petList);
            rvPetCards.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        } else {
            tvEmptyHint.setText("暂无角色，请先创建");
            rvPetCards.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
    }
}
