package com.example.aipet.util.store;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aipet.data.model.Pet;
import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;

import java.util.List;

/**
 * 宠物数据存储模块
 */
public class PetStore {

    private final Context appContext;

    public PetStore(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    public List<Pet> getAllPets() {
        return SPUtils.getList(appContext, Constants.KEY_PET_LIST, Pet.class);
    }

    public void saveAllPets(@NonNull List<Pet> pets) {
        SPUtils.putList(appContext, Constants.KEY_PET_LIST, pets);
    }

    public void addPet(@NonNull Pet pet) {
        List<Pet> pets = getAllPets();
        pets.add(pet);
        saveAllPets(pets);
    }

    @Nullable
    public Pet getPetById(long petId) {
        List<Pet> pets = getAllPets();
        for (Pet pet : pets) {
            if (pet.getId() == petId) {
                return pet;
            }
        }
        return null;
    }

    public boolean updatePet(@NonNull Pet updatedPet) {
        List<Pet> pets = getAllPets();
        for (int i = 0; i < pets.size(); i++) {
            if (pets.get(i).getId() == updatedPet.getId()) {
                pets.set(i, updatedPet);
                saveAllPets(pets);
                return true;
            }
        }
        return false;
    }

    public boolean deletePetById(long petId) {
        List<Pet> pets = getAllPets();
        boolean removed = false;
        for (int i = pets.size() - 1; i >= 0; i--) {
            if (pets.get(i).getId() == petId) {
                pets.remove(i);
                removed = true;
            }
        }

        if (removed) {
            saveAllPets(pets);
        }
        return removed;
    }

    @Nullable
    public Pet getFirstPetOrNull() {
        List<Pet> pets = getAllPets();
        return pets.isEmpty() ? null : pets.get(0);
    }
}
