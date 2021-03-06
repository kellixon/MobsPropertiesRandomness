package net.insane96mcp.mobrandomness.events.mobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sun.jna.Library;

import net.insane96mcp.mobrandomness.events.mobs.utils.MobEquipment;
import net.insane96mcp.mobrandomness.events.mobs.utils.MobPotionEffect;
import net.insane96mcp.mobrandomness.events.mobs.utils.MobPotionEffect.RNGPotionEffect;
import net.insane96mcp.mobrandomness.lib.Properties;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RNGEntity {

	private static List<MobEquipment> mobEquipments = new ArrayList<MobEquipment>();
	
	private static void LoadEquipment(String[] equipments, EntityEquipmentSlot slot) {
		String[] split;
		String mobName;
		float chance;
	
		for (String equipment : equipments) {
			if (equipment.equals(""))
				continue;
			
			try {
				split = equipment.split(",");
				mobName = split[0];
				chance = Float.parseFloat(split[1]);
			}
			catch (Exception e) {
				System.err.println("Failed to parse equipment \"" + equipment + "\": " + e.getMessage());
				continue;
			}
			
			boolean found = false;
			for (MobEquipment mobEquipment : mobEquipments) {
				if (mobName.equals(mobEquipment.mobName)) {
					for (int i = 2; i < split.length; i++) {
						String itemName = split[i];
						mobEquipment.AddEquipment(itemName, chance, slot, null);
					}
					found = true;
					break;
				}
			}
			if (!found) {
				MobEquipment newMobEquipment = new MobEquipment(mobName);
				for (int i = 2; i < split.length; i++) {
					String itemName = split[i];
					newMobEquipment.AddEquipment(itemName, chance, slot, null);
					mobEquipments.add(newMobEquipment);
				}
				mobEquipments.add(newMobEquipment);
			}
			
			
		}
	}
	
	private static void LoadAllEquipment(String[] equipment) {
		LoadEquipment(equipment, EntityEquipmentSlot.HEAD);
		LoadEquipment(equipment, EntityEquipmentSlot.CHEST);
		LoadEquipment(equipment, EntityEquipmentSlot.LEGS);
		LoadEquipment(equipment, EntityEquipmentSlot.FEET);
		LoadEquipment(equipment, EntityEquipmentSlot.MAINHAND);
		LoadEquipment(equipment, EntityEquipmentSlot.OFFHAND);
	}
	
	public static void Equipment(EntityLiving living, EntityEquipmentSlot equipmentSlot, String[] equipment, float multiplier, Random random){
		if (equipment.length == 0)
			return;
		
		mobEquipments = new ArrayList<MobEquipment>();
		
		if (mobEquipments.isEmpty()) {
			LoadAllEquipment(equipment);
		}
		
		ResourceLocation mobResourceLocation;
		for (MobEquipment mobEquipment : mobEquipments) {
			mobResourceLocation = new ResourceLocation(mobEquipment.mobName);
			if (!EntityList.isMatchingName(living, mobResourceLocation))
				continue;
				
			ItemStack itemStack = mobEquipment.GetRandomItem(random, equipmentSlot);
			if (itemStack.equals(ItemStack.EMPTY))
				continue;

			living.setItemStackToSlot(equipmentSlot, itemStack);
			
			break;
		}
	}
	
	public static void Attributes(EntityLiving living, IAttribute attribute, String[] stats, float multiplier, Random random) {
		ResourceLocation mobName;
		float minIncrease, maxIncrease;
		
		if (stats.length == 0)
			return;
		
		for (String stat : stats) {
			try {
				mobName = new ResourceLocation(stat.split(",")[0]);
				if (!EntityList.isMatchingName(living, mobName))
					continue;

				minIncrease = Float.parseFloat(stat.split(",")[1]) * multiplier;
				maxIncrease = Float.parseFloat(stat.split(",")[2]) * multiplier;
				
				double attributeValue = living.getEntityAttribute(attribute).getBaseValue();
				float increase = MathHelper.nextFloat(random, minIncrease, maxIncrease);
				
				if (attribute.equals(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) || !Properties.Stats.valuesAsPercentage)
					attributeValue += increase;
				else
					attributeValue += attributeValue * increase / 100f;
				
				living.getEntityAttribute(attribute).setBaseValue(attributeValue);
				
				if (attribute.equals(SharedMonsterAttributes.MAX_HEALTH))
					living.setHealth((float) attributeValue);
				
				break;
			}
			catch (Exception e) {
				System.err.println("Failed to parse attribute \"" + stat + "\": " + e.getMessage());
				continue;
			}
		}
	}
	
	private static List<MobPotionEffect> mobPotionEffects = new ArrayList<MobPotionEffect>();
	
	private static void LoadPotionEffects(String[] potionEffects) {		
		String mobName;
		float chance;
		String id;
		int minAmplifier;
		int maxAmplifier;
		boolean ambientParticles;
		boolean showParticles;
		
		for (String potionEffect : potionEffects) {
			if (potionEffect.equals(""))
				continue;
			try {	
				mobName = potionEffect.split(",")[0];
				chance = Float.parseFloat(potionEffect.split(",")[1]);
				id = potionEffect.split(",")[2];
				minAmplifier = Integer.parseInt(potionEffect.split(",")[3]);
				maxAmplifier = Integer.parseInt(potionEffect.split(",")[4]);
				ambientParticles = Boolean.parseBoolean(potionEffect.split(",")[5]);
				showParticles = Boolean.parseBoolean(potionEffect.split(",")[6]);
			} catch (Exception e) {
				System.err.println("Failed to parse potion line \"" + potionEffect + "\": " + e.getMessage());
				continue;
			}
			
			boolean found = false;
			for (MobPotionEffect mobPotionEffect : mobPotionEffects) {
				if (mobName.equals(mobPotionEffect.mobName)) {
					mobPotionEffect.AddPotionEffect(chance, id, minAmplifier, maxAmplifier, showParticles, ambientParticles);
					found = true;
					break;
				}
			}
			if (!found) {
				MobPotionEffect newMobPotionEffect = new MobPotionEffect(mobName);
				newMobPotionEffect.AddPotionEffect(chance, id, minAmplifier, maxAmplifier, showParticles, ambientParticles);
				mobPotionEffects.add(newMobPotionEffect);
			}
		}
	}

	public static void PotionEffects(EntityLiving living, String[] potionEffects, Random random) {
		if (potionEffects.length == 0)
			return;
		
		if (mobPotionEffects.isEmpty())
			LoadPotionEffects(potionEffects);
		
		ResourceLocation mobResourceLocation;
		for (MobPotionEffect mobPotionEffect : mobPotionEffects) {
			mobResourceLocation = new ResourceLocation(mobPotionEffect.mobName);
			if (EntityList.isMatchingName(living, mobResourceLocation)) {
				for (RNGPotionEffect rngPotionEffect : mobPotionEffect.potionEffects) {
					if (random.nextFloat() > rngPotionEffect.chance / 100f)
						continue;
					Potion potion = Potion.getPotionFromResourceLocation(rngPotionEffect.id);
					PotionEffect potionEffect = new PotionEffect(potion, 100000, MathHelper.getInt(random, rngPotionEffect.minAmplifier, rngPotionEffect.maxAmplifier - 1), rngPotionEffect.ambientParticles, rngPotionEffect.showParticles);
					living.addPotionEffect(potionEffect);
				}
				break;
			}
		}
	}
}
