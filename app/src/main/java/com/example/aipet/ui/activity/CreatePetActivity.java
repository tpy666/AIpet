package com.example.aipet.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.PetStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建角色页面。输入规则属性并保存到 SharedPreferences
 */
public class CreatePetActivity extends BaseActivity {

    private EditText etPetName;
    private Spinner spSpecies;
    private Spinner spPersonality;
    private Spinner spSpeakingStyle;
    private EditText etAppearance;
    private Button btnSave;
    private Button btnCancel;

    private PetStore petStore;
    private final List<String> speciesOptions = new ArrayList<>();
    private final List<String> personalityOptions = new ArrayList<>();
    private final List<String> speakingStyleOptions = new ArrayList<>();
    private ArrayAdapter<String> speciesAdapter;
    private ArrayAdapter<String> personalityAdapter;
    private ArrayAdapter<String> speakingStyleAdapter;
    private static final String CUSTOM_OPTION = "自定义...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pet);

        setupScreen("创建角色", true);
        petStore = UtilHub.petStore(this);
        initViews();
    }

    private void initViews() {
        etPetName = bind(R.id.et_pet_name);
        spSpecies = bind(R.id.sp_species);
        spPersonality = bind(R.id.sp_personality);
        spSpeakingStyle = bind(R.id.sp_speaking_style);
        etAppearance = bind(R.id.et_appearance);
        btnSave = bind(R.id.btn_save);
        btnCancel = bind(R.id.btn_cancel);

        // 加载下拉选项
        setupSpeciesSpinner();
        spSpecies.setAdapter(speciesAdapter);

        setupPersonalitySpinner();
        spPersonality.setAdapter(personalityAdapter);

        setupSpeakingStyleSpinner();
        spSpeakingStyle.setAdapter(speakingStyleAdapter);

        btnSave.setOnClickListener(v -> savePet());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpeciesSpinner() {
        speciesOptions.clear();
        speciesOptions.add("猫");
        speciesOptions.add("狗");
        speciesOptions.add("兔子");
        speciesOptions.add("狐狸");
        speciesOptions.add("龙");
        speciesOptions.add("小熊");
        speciesOptions.add(CUSTOM_OPTION);

        speciesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, speciesOptions);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpecies.setAdapter(speciesAdapter);
        spSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == speciesOptions.size() - 1) {
                    showCustomInputDialog("自定义物种", "请输入你喜欢的物种", text -> addCustomSpecies(text));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupPersonalitySpinner() {
        personalityOptions.clear();
        personalityOptions.add("温柔");
        personalityOptions.add("活泼");
        personalityOptions.add("高冷");
        personalityOptions.add("撒娇");
        personalityOptions.add("稳重");
        personalityOptions.add(CUSTOM_OPTION);

        personalityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personalityOptions);
        personalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPersonality.setAdapter(personalityAdapter);
        spPersonality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == personalityOptions.size() - 1) {
                    showCustomInputDialog("自定义性格", "请输入你喜欢的性格", text -> addCustomPersonality(text));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSpeakingStyleSpinner() {
        speakingStyleOptions.clear();
        speakingStyleOptions.add("卖萌");
        speakingStyleOptions.add("暖心");
        speakingStyleOptions.add("幽默");
        speakingStyleOptions.add("文艺");
        speakingStyleOptions.add("直白");
        speakingStyleOptions.add(CUSTOM_OPTION);

        speakingStyleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, speakingStyleOptions);
        speakingStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpeakingStyle.setAdapter(speakingStyleAdapter);
        spSpeakingStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == speakingStyleOptions.size() - 1) {
                    showCustomInputDialog("自定义说话风格", "请输入你喜欢的说话风格", text -> addCustomSpeakingStyle(text));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showCustomInputDialog(String title, String hint, CustomValueCallback callback) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("添加", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    callback.onValue(value);
                })
                .setNegativeButton("取消", (dialog, which) -> restoreDefaultSpinnerSelection())
                .show();
    }

    private void addCustomSpecies(String value) {
        if (!speciesOptions.contains(value)) {
            speciesOptions.add(speciesOptions.size() - 1, value);
            speciesAdapter.notifyDataSetChanged();
        }
        spSpecies.setSelection(speciesOptions.indexOf(value));
    }

    private void addCustomPersonality(String value) {
        if (!personalityOptions.contains(value)) {
            personalityOptions.add(personalityOptions.size() - 1, value);
            personalityAdapter.notifyDataSetChanged();
        }
        spPersonality.setSelection(personalityOptions.indexOf(value));
    }

    private void addCustomSpeakingStyle(String value) {
        if (!speakingStyleOptions.contains(value)) {
            speakingStyleOptions.add(speakingStyleOptions.size() - 1, value);
            speakingStyleAdapter.notifyDataSetChanged();
        }
        spSpeakingStyle.setSelection(speakingStyleOptions.indexOf(value));
    }

    private void restoreDefaultSpinnerSelection() {
        if (!speciesOptions.isEmpty()) {
            spSpecies.setSelection(0);
        }
        if (!personalityOptions.isEmpty()) {
            spPersonality.setSelection(0);
        }
        if (!speakingStyleOptions.isEmpty()) {
            spSpeakingStyle.setSelection(0);
        }
    }

    private interface CustomValueCallback {
        void onValue(String value);
    }

    private void savePet() {
        String name = etPetName.getText().toString().trim();
        String species = spSpecies.getSelectedItem().toString();
        String personality = spPersonality.getSelectedItem().toString();
        String speakingStyle = spSpeakingStyle.getSelectedItem().toString();
        String appearance = etAppearance.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入宠物名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(appearance)) {
            Toast.makeText(this, "请输入外观关键词", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = System.currentTimeMillis();
        Pet pet = new Pet(id, name, species, personality, speakingStyle, appearance, "");

        petStore.addPet(pet);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
