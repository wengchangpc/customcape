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
            })));
    }
}
