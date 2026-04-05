package com.example.aipet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;

import java.util.List;

/**
 * 角色卡适配器
 */
public class PetCardAdapter extends RecyclerView.Adapter<PetCardAdapter.PetCardViewHolder> {

    private List<Pet> petList;
    private OnPetClickListener listener;
    private OnPetDeleteListener deleteListener;

    public interface OnPetClickListener {
        void onPetClick(Pet pet);
    }

    public interface OnPetDeleteListener {
        void onPetDelete(Pet pet);
    }

    public PetCardAdapter(OnPetClickListener listener, OnPetDeleteListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setPetList(List<Pet> petList) {
        this.petList = petList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_card, parent, false);
        return new PetCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetCardViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.tvPetName.setText(pet.getName());
        holder.tvPetSpecies.setText("物种: " + pet.getSpecies());
        holder.tvPetPersonality.setText("性格: " + pet.getPersonality());
        holder.tvPetSpeakingStyle.setText("说话风格: " + pet.getSpeakingStyle());
        holder.tvPetAppearance.setText("外观: " + pet.getAppearance());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPetClick(pet);
            }
        });

        holder.btnDeletePet.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPetDelete(pet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0;
    }

    static class PetCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetName, tvPetSpecies, tvPetPersonality, tvPetSpeakingStyle, tvPetAppearance;
        android.widget.Button btnDeletePet;

        public PetCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.tv_pet_name);
            tvPetSpecies = itemView.findViewById(R.id.tv_pet_species);
            tvPetPersonality = itemView.findViewById(R.id.tv_pet_personality);
            tvPetSpeakingStyle = itemView.findViewById(R.id.tv_pet_speaking_style);
            tvPetAppearance = itemView.findViewById(R.id.tv_pet_appearance);
            btnDeletePet = itemView.findViewById(R.id.btn_delete_pet);
        }
    }
}