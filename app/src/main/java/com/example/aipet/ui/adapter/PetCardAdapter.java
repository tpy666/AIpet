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
    private final OnPetClickListener listener;
    private final OnPetDeleteListener deleteListener;

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
        int oldSize = this.petList == null ? 0 : this.petList.size();
        this.petList = petList;
        int newSize = this.petList == null ? 0 : this.petList.size();

        if (oldSize == 0 && newSize == 0) {
            return;
        }
        if (oldSize == 0) {
            notifyItemRangeInserted(0, newSize);
            return;
        }
        if (newSize == 0) {
            notifyItemRangeRemoved(0, oldSize);
            return;
        }

        int sharedSize = Math.min(oldSize, newSize);
        notifyItemRangeChanged(0, sharedSize);
        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize);
        }
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
        android.content.Context context = holder.itemView.getContext();
        holder.tvPetName.setText(pet.getName());
        holder.tvPetSpecies.setText(context.getString(R.string.pet_species_format, pet.getSpecies()));
        holder.tvPetPersonality.setText(context.getString(R.string.pet_personality_format, pet.getPersonality()));
        holder.tvPetSpeakingStyle.setText(context.getString(R.string.pet_speaking_style_format, pet.getSpeakingStyle()));
        holder.tvPetAppearance.setText(context.getString(R.string.pet_appearance_format, pet.getAppearance()));

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