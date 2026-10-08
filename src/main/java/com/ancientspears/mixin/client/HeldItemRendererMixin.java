package com.ancientspears.mixin.client;

import com.ancientspears.AncientSpears;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void ancientSpears$chargePose(AbstractClientPlayerEntity player, float tickDelta,
            float pitch, Hand hand, float swingProgress, ItemStack stack,
            float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light, CallbackInfo ci) {
        if (!(stack.getItem() instanceof AncientSpears.SpearItem)
                || !player.isUsingItem() || player.getActiveHand() != hand) return;

        // Gradually move the spear into a forward-pointing charge pose.
        float progress = Math.min(1.0f, (player.getItemUseTime() + tickDelta) / 8.0f);
        matrices.translate(0.0f, 0.055f * progress, -0.12f * progress);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-12.0f * progress));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                (hand == Hand.MAIN_HAND ? -1.0f : 1.0f) * 10.0f * progress));
    }
}
