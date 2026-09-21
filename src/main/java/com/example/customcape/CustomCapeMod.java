package com.example.customcape;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomCapeMod.MODID)
public class CustomCapeMod {
    public static final String MODID = "customcape";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CustomCapeMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            // 纯客户端模组：在专用服务器上什么都不做
            return;
        }

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);

        LOGGER.info("[CustomCape] 已初始化，愿披风与你同在！");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // GL 纹理操作必须回到渲染线程执行
        Minecraft.getInstance().execute(() -> {
            CapeTextureManager.copyDefaultIfMissing();
            CapeTextureManager.load();
        });
    }

    private void onAddLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
        // 用原版玩家模型的 bake 产物拿 cloak 部件（几何与原版披风完全一致）
        var root = event.getContext().bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER);
        for (String skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer pr) {
                pr.addLayer(new CapeGlossLayer(pr, root));
            }
        }
        CustomCapeMod.LOGGER.info("[CustomCape] 披风流光镀层已挂载！");
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("customcape")
            .then(Commands.literal("reload").executes(ctx -> {
                // 反馈必须在加载完成之后给出（load 在渲染线程执行）
                Minecraft.getInstance().execute(() -> {
                    CapeTextureManager.load();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            CapeTextureManager.isAvailable()
                                ? "[CustomCape] 披风贴图已重新加载！"
                                : "[CustomCape] 未找到 cape.png，请把贴图放到 config/CustomCape/cape.png"),
                        false);
                });
                return 1;
            }))
            .then(Commands.literal("fp").executes(ctx -> {
                CapeConfig.fpCape = !CapeConfig.fpCape;
                CapeConfig.save();
                ctx.getSource().sendSuccess(() -> Component.literal(
                        CapeConfig.fpCape
                            ? "[CustomCape] 第一人称披风已开启！回头或俯冲即可看到。"
                            : "[CustomCape] 第一人称披风已关闭。"), false);
                return 1;
            }))
            .then(Commands.literal("vivid").executes(ctx -> {
                CapeConfig.vivid = !CapeConfig.vivid;
                CapeConfig.save();
                Minecraft.getInstance().execute(() -> CapeTextureManager.load()); // 贴图需重载生效
                ctx.getSource().sendSuccess(() -> Component.literal(
                        CapeConfig.vivid
                            ? "[CustomCape] 增艳已开启，贴图已重载！"
                            : "[CustomCape] 增艳已关闭，贴图已重载。"), false);
                return 1;
            }))
            .then(Commands.literal("gloss").executes(ctx -> {
                CapeConfig.gloss = !CapeConfig.gloss;
                CapeConfig.save();
                ctx.getSource().sendSuccess(() -> Component.literal(
                        CapeConfig.gloss
                            ? "[CustomCape] 流光镀层已开启！暗处也会发光。"
                            : "[CustomCape] 流光镀层已关闭。"), false);
                return 1;
            })));
    }
}
