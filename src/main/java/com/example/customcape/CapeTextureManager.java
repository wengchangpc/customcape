package com.example.customcape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 披风贴图管理器：从 config/CustomCape/cape.png 动态加载披风贴图。
 */
public final class CapeTextureManager {
    private static final Logger LOGGER = LogManager.getLogger(CustomCapeMod.MODID);

    public static final ResourceLocation CAPE_ID = new ResourceLocation(CustomCapeMod.MODID, "cape");

    private static DynamicTexture texture;
    private static boolean available = false;

    private CapeTextureManager() {
    }

    public static boolean isAvailable() {
        return available;
    }

    public static ResourceLocation getCapeTextureId() {
        return CAPE_ID;
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve("CustomCape");
    }

    public static Path getCapeFile() {
        return getConfigDir().resolve("cape.png");
    }

    /**
     * 首次运行时，把模组内置的默认披风贴图释放到配置目录。
     * 必须在渲染线程上调用（涉及 GL 纹理上传）。
     */
    public static void copyDefaultIfMissing() {
        try {
            if (Files.exists(getCapeFile())) {
                return;
            }
            Files.createDirectories(getConfigDir());
            try (InputStream in = CapeTextureManager.class
                    .getResourceAsStream("/assets/customcape/default_cape.png")) {
                if (in != null) {
                    Files.copy(in, getCapeFile());
                    LOGGER.info("[CustomCape] 已释放默认披风贴图到 {}", getCapeFile());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[CustomCape] 无法创建默认披风文件", e);
        }
    }

    /**
     * 加载（或重新加载）config/CustomCape/cape.png。
     * 必须在渲染线程上调用。
     */
    public static void load() {
        Path file = getCapeFile();
        if (!Files.isRegularFile(file)) {
            available = false;
            LOGGER.info("[CustomCape] 未找到披风贴图: {}", file);
            return;
        }
        try (InputStream in = Files.newInputStream(file)) {
            var image = com.mojang.blaze3d.pipeline.NativeImage.read(in);
            DynamicTexture newTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(CAPE_ID, newTexture);
            if (texture != null) {
                texture.close();
            }
            texture = newTexture;
            available = true;
            LOGGER.info("[CustomCape] 披风贴图已加载: {}", file);
        } catch (Exception e) {
            available = false;
            LOGGER.error("[CustomCape] 加载披风贴图失败: {}", file, e);
        }
    }
}
