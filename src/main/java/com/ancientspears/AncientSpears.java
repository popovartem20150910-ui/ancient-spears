package com.ancientspears;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AncientSpears implements ModInitializer {
    public static final String MOD_ID = "ancient_spears";

    public static final Item WOODEN_SPEAR = register("wooden_spear", 59, 3.0f);
    public static final Item STONE_SPEAR = register("stone_spear", 131, 4.0f);
    public static final Item IRON_SPEAR = register("iron_spear", 250, 5.0f);
    public static final Item GOLDEN_SPEAR = register("golden_spear", 32, 4.0f);
    public static final Item DIAMOND_SPEAR = register("diamond_spear", 1561, 6.0f);
    public static final Item NETHERITE_SPEAR = register("netherite_spear", 2031, 7.0f);

    private static Item register(String name, int durability, float damage) {
        Item.Settings settings = new Item.Settings().maxCount(1).maxDamage(durability);
        if (name.equals("netherite_spear")) settings.fireproof();
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
                new SpearItem(settings, damage));
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(WOODEN_SPEAR);
            entries.add(STONE_SPEAR);
            entries.add(IRON_SPEAR);
            entries.add(GOLDEN_SPEAR);
            entries.add(DIAMOND_SPEAR);
            entries.add(NETHERITE_SPEAR);
        });
    }

    /** Hold right click to prepare a charge; release to strike in front of you. */
    public static class SpearItem extends Item {
        private final float baseDamage;

        public SpearItem(Settings settings, float baseDamage) {
            super(settings);
            this.baseDamage = baseDamage;
        }

        @Override
        public int getMaxUseTime(ItemStack stack, LivingEntity user) {
            return 72000;
        }

        @Override
        public UseAction getUseAction(ItemStack stack) {
            // Do not play the bow-drawing animation. Vanilla still slows movement
            // while the player is actively using an item.
            return UseAction.NONE;
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            ItemStack stack = player.getStackInHand(hand);
            if (player.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        @Override
        public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
            if (!(user instanceof PlayerEntity player) || world.isClient) return;
            int heldTicks = getMaxUseTime(stack, user) - remainingUseTicks;
            if (heldTicks < 3) return;

            Vec3d origin = player.getEyePos();
            Vec3d facing = player.getRotationVec(1.0f).normalize();
            LivingEntity closest = null;
            double best = 4.5;

            for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class,
                    new Box(origin, origin.add(facing.multiply(4.5))).expand(1.5),
                    entity -> entity != player && entity.isAlive())) {
                Vec3d toward = target.getBoundingBox().getCenter().subtract(origin);
                double forward = toward.dotProduct(facing);
                if (forward < 0 || forward > 4.5) continue;
                double sideways = toward.subtract(facing.multiply(forward)).length();
                if (sideways > target.getWidth() * 0.5 + 0.8) continue;
                if (!player.canSee(target)) continue;
                if (forward < best) {
                    closest = target;
                    best = forward;
                }
            }

            if (closest != null) {
                double speed = player.getVelocity().horizontalLength();
                float charge = Math.min(1.0f, heldTicks / 20.0f);
                float damage = baseDamage + (float)Math.min(10.0, speed * 16.0) * (0.5f + charge);
                closest.damage(world.getDamageSources().playerAttack(player), damage);
                stack.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
            }

            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    player.getSoundCategory(), 1.0f, 0.85f);
            player.getItemCooldownManager().set(this, 12);
        }
    }
}
