package vswe.stevescarts.Modules.Realtimers;
import java.util.ArrayList;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import vswe.stevescarts.Carts.MinecartModular;
import vswe.stevescarts.Interfaces.GuiMinecart;
import vswe.stevescarts.Modules.ModuleBase;
import vswe.stevescarts.Slots.SlotBase;
import vswe.stevescarts.Slots.SlotFirework;

public class ModuleFirework extends ModuleBase {
	public ModuleFirework(MinecartModular cart) {
		super(cart);
	}

	private int fireCooldown;
	
	@Override
	public void update() {
		if (fireCooldown > 0) {
			fireCooldown--;
		}
	}
	
	
	@Override
	public void activatedByRail(int x, int y, int z, boolean active) {
		if (active && fireCooldown == 0 && getCart().hasFuel()) {
			fire();
			fireCooldown = 20;
		}
	}	
	
	
	@Override
	public boolean hasGui(){
		return true;
	}

	@Override
	protected SlotBase getSlot(int slotId, int x, int y) {
		return new SlotFirework(getCart(),slotId,8+x*18,16+y*18);
	}

	@Override
	public void drawForeground(GuiMinecart gui) {
	    drawString(gui,getModuleName(), 8, 6, 0x404040);
	}

	@Override
	public int guiWidth() {
		return 15 + getInventoryWidth() * 18;
	}

	@Override
	public int guiHeight() {
		return 20 + getInventoryHeight() * 18 ;
	}

	@Override
	protected int getInventoryWidth()
	{
		return 8;
	}
	@Override
	protected int getInventoryHeight() {
		return 3;
	}	
	
	public void fire() {
		if (getCart().worldObj.isRemote) {
			return;
		}
	
		ItemStack firework = getFirework();
		if (firework != null) {
			launchFirework(firework);
		}
	}
	
	private ItemStack getFirework() {
		boolean hasGunpowder = false;
		boolean hasPaper = false;	
	
		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			
			if (item != null) {
			
				if (item.getItem().itemID == Item.firework.itemID) {
					ItemStack firework = item.copy();
					firework.stackSize = 1;
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}
					
					return firework;
				}else if(item.getItem().itemID == Item.paper.itemID) {
					hasPaper = true;
				}else if(item.getItem().itemID == Item.gunpowder.itemID) {
					hasGunpowder = true;
				}
			}			
		}
		

		
		if (hasPaper && hasGunpowder) {

		
			ItemStack firework = new ItemStack(Item.firework);
			
			int maxGunpowder = getCart().rand.nextInt(3) + 1;
			int countGunpowder = 0;
			boolean removedPaper = false;
			for (int i = 0; i < getInventorySize(); i++) {
				ItemStack item = getStack(i);
				
				if (item != null) {
					if(item.getItem().itemID == Item.paper.itemID && !removedPaper) {
						if (--item.stackSize <= 0) {
							setStack(i, null);
						}
						removedPaper = true;
					}else if(item.getItem().itemID == Item.gunpowder.itemID && countGunpowder < maxGunpowder) {
						while (item.stackSize > 0 && countGunpowder < maxGunpowder) {
							countGunpowder++;
							item.stackSize--;
						}
						
						if (item.stackSize <= 0) {
							setStack(i, null);
						}					
					}
				}
			}
			
			int chargeCount = 1;
			
			while (chargeCount < 7 && getCart().rand.nextInt(3 + chargeCount / 3) == 0) {
				chargeCount++;
			}
			
			NBTTagCompound itemstackNBT = new NBTTagCompound();
			NBTTagCompound fireworksNBT = new NBTTagCompound("Fireworks");
			NBTTagList explosionsNBT = new NBTTagList("Explosions");			
			
			for (int i = 0; i < chargeCount; i++) {
				ItemStack charge = getCharge();
				if (charge == null) {
					break;
				}else if (charge.hasTagCompound() && charge.getTagCompound().hasKey("Explosion")) {					
					explosionsNBT.appendTag(charge.getTagCompound().getCompoundTag("Explosion"));					
				}				
			}
			
			fireworksNBT.setTag("Explosions", explosionsNBT);
			fireworksNBT.setByte("Flight", (byte)countGunpowder);
			itemstackNBT.setTag("Fireworks", fireworksNBT);
			firework.setTagCompound(itemstackNBT);			
			
			return firework;
		}


		return null;
	}
	
	private ItemStack getCharge() {
	
		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			
			if (item != null) {
			
				if (item.getItem().itemID == Item.fireworkCharge.itemID) {
					ItemStack charge = item.copy();
					charge.stackSize = 1;
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}
					
					return charge;
				}
			}			
		}	
	
		ItemStack charge = new ItemStack(Item.fireworkCharge);
		NBTTagCompound itemNBT = new NBTTagCompound();
		NBTTagCompound explosionNBT = new NBTTagCompound("Explosion");
		byte type = 0;

		

		boolean removedGunpowder = false;
		boolean canHasTrail = getCart().rand.nextInt(16) == 0;
		boolean canHasFlicker = getCart().rand.nextInt(8) == 0;
		boolean canHasModifier = getCart().rand.nextInt(4) == 0;
		byte modifierType = (byte)(getCart().rand.nextInt(4) + 1);
		boolean removedModifier = false;
		boolean removedDiamond = false;
		boolean removedGlow = false;
		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			
			if (item != null) {
				if(item.getItem().itemID == Item.gunpowder.itemID && !removedGunpowder) {
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}	
					removedGunpowder = true;
				}else if(item.getItem().itemID == Item.glowstone.itemID && canHasFlicker && !removedGlow) {
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}						
					removedGlow = true;
					explosionNBT.setBoolean("Flicker", true);
				}else if(item.getItem().itemID == Item.diamond.itemID && canHasTrail && !removedDiamond) {
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}						
					removedDiamond = true;
					explosionNBT.setBoolean("Trail", true);
				}else if(canHasModifier && !removedModifier && (
					(item.getItem().itemID == Item.fireballCharge.itemID && modifierType == 1) ||
					(item.getItem().itemID == Item.goldNugget.itemID && modifierType == 2) ||
					(item.getItem().itemID == Item.skull.itemID && modifierType == 3) ||
					(item.getItem().itemID == Item.feather.itemID && modifierType == 4)
					)
				
				
				) {
					if (--item.stackSize <= 0) {
						setStack(i, null);
					}						
					removedModifier = true;		
					type = modifierType;
				}
			}
		}


		
	
	

		int[] colors = generateColors(type != 0 ? 7 : 8);
		if (colors == null) {
			return null;
		}
		explosionNBT.setIntArray("Colors", colors);	
		if (getCart().rand.nextInt(4) == 0) {
			int[] fade = generateColors(8);
			if (fade != null) {
				explosionNBT.setIntArray("FadeColors", fade);
			}
		}
		explosionNBT.setByte("Type", type);
		itemNBT.setTag("Explosion", explosionNBT);
		charge.setTagCompound(itemNBT);	
		

		return charge;
	}
	
	
	private int[] generateColors(int maxColorCount) {
		int[] maxColors = new int[16];
		int[] currentColors = new int[16];
		
		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			
			if (item != null) {		
				if(item.getItem().itemID == Item.dyePowder.itemID) {
					maxColors[item.getItemDamage()] += item.stackSize;	
				}
			}
		}
		
		int colorCount = getCart().rand.nextInt(2) + 1;
		while (colorCount <= maxColorCount - 2 && getCart().rand.nextInt(2) == 0) {
			colorCount+=2;
		}	

		ArrayList<Integer> colorPointers = new ArrayList<Integer>();
		for (int i = 0; i < 16; i++) {
			if (maxColors[i] > 0) {
				colorPointers.add(i);
			}
		}
		
		if (colorPointers.size() == 0) {
			return null;
		}
		
		ArrayList<Integer> usedColors = new ArrayList<Integer>();
		while (colorCount > 0 && colorPointers.size() > 0) {
			int pointerId = getCart().rand.nextInt(colorPointers.size());
			int colorId = colorPointers.get(pointerId);
			currentColors[colorId]++;
			if(--maxColors[colorId] <= 0) {
				colorPointers.remove(pointerId);
			}
			usedColors.add(colorId);
			colorCount--;
		}
		
		int[] colors = new int[usedColors.size()];

		for (int i = 0; i < colors.length; ++i)
		{
			colors[i] = Integer.valueOf(ItemDye.dyeColors[usedColors.get(i)]);
		}

		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			
			if (item != null) {
				if(item.getItem().itemID == Item.dyePowder.itemID) {
					if (currentColors[item.getItemDamage()] > 0) {
						int count = Math.min(currentColors[item.getItemDamage()], item.stackSize);
						currentColors[item.getItemDamage()] -= count;
						item.stackSize -= count;
						if (item.stackSize <= 0) {
							setStack(i, null);
						}
					}
				}
			}
		}			
		
		return colors;
	}
	

	
	
	private void launchFirework(ItemStack firework) {
		EntityFireworkRocket rocket = new EntityFireworkRocket(getCart().worldObj, getCart().posX, getCart().posY + 1, getCart().posZ, firework);
		getCart().worldObj.spawnEntityInWorld(rocket);	
	}
	
	

	
}