
package net.sashakyotoz.variousworld.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.sashakyotoz.variousworld.entity.MultipleEnderPearlEntity;
import net.sashakyotoz.variousworld.init.VariousWorldModEntities;

public class MultipleEnderPearlItem extends Item {
	public MultipleEnderPearlItem() {
		super(new Item.Properties().durability(4).fireResistant().rarity(Rarity.UNCOMMON));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemstack) {
		return UseAnim.EAT;
	}

	@Override
	public boolean hasCraftingRemainingItem() {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack itemstack) {
		return new ItemStack(this);
	}

	@Override
	public boolean isRepairable(ItemStack itemstack) {
		return false;
	}

	@Override
	public float getDestroySpeed(ItemStack par1ItemStack, BlockState par2Block) {
		return 1.2F;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, player, hand);
		ItemStack itemstack = ar.getObject();
		Level projectileLevel = player.level();
		if (!projectileLevel.isClientSide()) {
			Projectile projectile = new Object() {
				public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
					AbstractArrow entityToSpawn = new MultipleEnderPearlEntity(VariousWorldModEntities.MULTIPLE_ENDER_PEARL.get(), level);
					entityToSpawn.setOwner(shooter);
					entityToSpawn.setBaseDamage(damage);
					entityToSpawn.setKnockback(knockback);
					entityToSpawn.setSilent(true);
					return entityToSpawn;
				}
			}.getArrow(projectileLevel, player, 0, 0);
			projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
			projectile.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, 2, 0);
			projectileLevel.addFreshEntity(projectile);
		}
		if (itemstack.hurt(1, RandomSource.create(), null)) {
			itemstack.shrink(1);
			itemstack.setDamageValue(0);
		}
		return ar;
	}
}
