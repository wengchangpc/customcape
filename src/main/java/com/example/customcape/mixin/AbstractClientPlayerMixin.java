package com.example.customcape.mixin;

import com.example.customcape.CapeTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在 AbstractClientPlayer 的披风贴图入口处"偷梁换柱"：
 * 仅当渲染的是本地玩家（也就是你自己）时，返回自定义贴图。
 * 其他玩家的披风逻辑完全不受影响，服务器也看不到任何变化。
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Unique
    private boolean customcape$isLocalPlayer() {
        return (Object) this == Minecraft.getInstance().player;
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void customcape$getCapeTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        if (customcape$isLocalPlayer() && CapeTextureManager.isAvailable()) {
            cir.setReturnValue(CapeTextureManager.getCapeTextureId());
        }
    }

    @Inject(method = "canRenderCapeTexture", at = @At("HEAD"), cancellable = true)
    private void customcape$canRenderCapeTexture(CallbackInfoReturnable<Boolean> cir) {
        if (customcape$isLocalPlayer() && CapeTextureManager.isAvailable()) {
            cir.setReturnValue(true);
        }
    }
}
