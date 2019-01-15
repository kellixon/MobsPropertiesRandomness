package net.insane96mcp.mpr.json.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import net.insane96mcp.mpr.exceptions.InvalidJsonException;
import net.insane96mcp.mpr.json.IJsonObject;
import net.insane96mcp.mpr.lib.Logger;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;

public class Item extends WeightedRandom.Item implements IJsonObject{

	public Item(int itemWeightIn) {
		super(itemWeightIn);
	}

	public String id;
	public int data;
	private int weight;
	@SerializedName("weight_difficulty")
	private WeightDifficulty weightDifficulty;
	@SerializedName("drop_chance")
	public float dropChance;
	public List<Enchantment> enchantments;
	public List<ItemAttribute> attributes;
	public String nbt;
	
	@Override
	public String toString() {
		return String.format("Item{id: %s, data: %d, weight: %d, weightDifficulty: %s, dropChance: %f, enchantments: %s, attributes: %s, nbt: %s}", id, data, weight, weightDifficulty, dropChance, enchantments, attributes, nbt);
	}
	
	public Item GetWeightWithDifficulty(Item item, World world) {
		Item item2 = item.copy();
		
		switch (world.getDifficulty()) {
		case EASY:
			item2.itemWeight += weightDifficulty.easy;
			break;
			
		case NORMAL:
			item2.itemWeight += weightDifficulty.normal;
			break;
			
		case HARD:
			item2.itemWeight += weightDifficulty.hard;
			break;

		default:
			break;
		}

		return item2;
	}

	public void Validate(final File file) throws InvalidJsonException{
		if (id == null)
			throw new InvalidJsonException("Missing Id for " + this, file);
		else if (net.minecraft.item.Item.getByNameOrId(id) == null)
			Logger.Warning("Failed to find item with id " + id);
		
		if (weight <= 0)
			throw new InvalidJsonException("Missing weight (or weight <= 0) for " + this, file);
		else
			itemWeight = weight;
		
		if (weightDifficulty == null)
			weightDifficulty = new WeightDifficulty();
		
		if (dropChance == 0f) {
			Logger.Debug("Drop Chance has been set to 0 (or omitted). Will now default to 8.5f. If you want mobs to not drop this item, set dropChance to -1");
			dropChance = 8.5f;
		}
		else if (dropChance == -1f) {
			Logger.Debug("Drop Chance has been set to -1. Mob no longer drops this item in any case");
			dropChance = Short.MIN_VALUE;
		}
		
		if (enchantments == null)
			enchantments = new ArrayList<Enchantment>();
		
		if (!enchantments.isEmpty()) {
			for (Enchantment enchantment : enchantments) {
				enchantment.Validate(file);
			}
		}
		
		if (attributes == null) 
			attributes = new ArrayList<ItemAttribute>();
		
		if (!attributes.isEmpty()) {
			for (ItemAttribute itemAttribute : attributes) {
				itemAttribute.Validate(file);
			}
		}
	}

	/**
	 * Returns a copy of the Item
	 * @return a copy of the Item
	 */
	protected Item copy() {
		Item item = new Item(this.weight);
		item.attributes = this.attributes;
		item.data = this.data;
		item.dropChance = this.dropChance;
		item.enchantments = this.enchantments;
		item.id = this.id;
		item.itemWeight = this.itemWeight;
		item.nbt = this.nbt;
		item.weight = this.weight;
		item.weightDifficulty = this.weightDifficulty;
		return item;
	}
}
