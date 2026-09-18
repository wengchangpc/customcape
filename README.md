# Custom Cape (Forge 1.20.1) 自定义披风

一个纯客户端 Forge 模组：在你的角色背后渲染一张**自定义披风贴图**，**只有你自己看得见**，任何世界（单人 / 任意服务器）都生效。

- ✅ MC Java **1.20.1** + Forge **47.x**
- ✅ 纯客户端，加入原版服务器 / 服务器没装它也没问题（`displayTest="IGNORE_ALL_VERSION"`）
- ✅ 只影响**本地玩家**的披风，其他玩家渲染不受影响
- ✅ 贴图从本地文件动态加载，改图即换披风

## 🎒 使用方法

1. 把模组 jar 放进 `.minecraft/mods/`（需要 Forge 1.20.1-47.x）。
2. 首次启动后，模组会在 `.minecraft/config/CustomCape/` 里释放一张默认披风 `cape.png`。
3. 用你自己的 64×32（或任意 2:1 比例）PNG 覆盖它。
4. 游戏内聊天框输入：

```
/customcape reload
```

披风立刻换新！第四人称（F5 视角）和第一人称背面都能看到。

## 🏗️ 在 GitHub 上构建

1. 把本目录推送到一个 GitHub 仓库：

```bash
git init && git add . && git commit -m "CustomCape Forge 1.20.1"
git remote add origin https://github.com/<你的用户名>/customcape.git
git push -u origin main
```

2. GitHub Actions 会自动运行 `.github/workflows/build.yml`：
   - 每次 push / PR 都会构建，并在 **Actions → Artifacts** 产出 `CustomCape-<commit>.zip`（内含模组 jar）。
   - 打 tag `v1.0.0`（任意 `v*`）会额外自动创建 **Release** 并附带 jar。

3. 想本地构建也行：安装 JDK 17 + Gradle 8.1.1，然后 `gradle build`，产物在 `build/libs/`。

## 🗺️ 披风贴图说明

- 推荐尺寸 **64×32**（原版披风规格），左半边是正面。
- 透明通道可用（RGBA PNG）。
- 路径：`.minecraft/config/CustomCape/cape.png`

## 🔧 实现原理

通过 Mixin 注入 `AbstractClientPlayer`：

- `getCapeTexture()` → 本地玩家时返回 `customcape:cape` 动态贴图；
- `canRenderCapeTexture()` → 本地玩家时强制允许渲染披风层（即使皮肤本身没有披风）。

整个逻辑 100% 客户端，服务端与其它玩家零感知。

## 项目结构

```
customcape/
├── build.gradle / settings.gradle / gradle.properties   # ForgeGradle 6 + MixinGradle
├── .github/workflows/build.yml                          # GitHub Actions 自动构建
└── src/main/
    ├── java/com/example/customcape/
    │   ├── CustomCapeMod.java            # 模组入口 + /customcape reload 命令
    │   ├── CapeTextureManager.java       # 配置目录贴图动态加载
    │   └── mixin/AbstractClientPlayerMixin.java
    └── resources/
        ├── META-INF/mods.toml
        ├── customcape.mixins.json
        ├── pack.mcmeta
        └── assets/customcape/default_cape.png   # 内置默认披风
```

## License

MIT
