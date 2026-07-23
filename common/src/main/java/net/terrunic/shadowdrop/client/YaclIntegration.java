package net.terrunic.shadowdrop.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.terrunic.shadowdrop.ShadowDropConfig;

import java.util.ArrayList;
import java.util.Arrays;

public class YaclIntegration {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.shadowdrop.title"))
                .save(ShadowDropConfig::save);

        ConfigCategory.Builder general = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.general"));

        general.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.modEnabled"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.modEnabled.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.modEnabled, val -> ShadowDropConfig.CLIENT.modEnabled = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        general.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowColor"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowColor.tooltip")))
                .binding("#000000", () -> ShadowDropConfig.CLIENT.shadowColor, val -> {
                    if (val.matches("^#[0-9A-Fa-f]{6}$")) {
                        ShadowDropConfig.CLIENT.shadowColor = val;
                    }
                })
                .controller(StringControllerBuilder::create)
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowAlpha"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowAlpha.tooltip")))
                .binding(99, () -> ShadowDropConfig.CLIENT.shadowAlpha, val -> ShadowDropConfig.CLIENT.shadowAlpha = val)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 255).step(1))
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowXOffset"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowXOffset.tooltip")))
                .binding(1, () -> ShadowDropConfig.CLIENT.shadowXOffset, val -> ShadowDropConfig.CLIENT.shadowXOffset = val)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-4, 4).step(1))
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowYOffset"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowYOffset.tooltip")))
                .binding(1, () -> ShadowDropConfig.CLIENT.shadowYOffset, val -> ShadowDropConfig.CLIENT.shadowYOffset = val)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-4, 4).step(1))
                .build());

        ConfigCategory.Builder shadows = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.shadows"));

        shadows.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.hotbarShadows"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.hotbarShadows.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.hotbarShadows, val -> ShadowDropConfig.CLIENT.hotbarShadows = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        shadows.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.slotShadows"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.slotShadows.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.slotShadows, val -> ShadowDropConfig.CLIENT.slotShadows = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        shadows.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.hoverShadows"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.hoverShadows.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.hoverShadows, val -> ShadowDropConfig.CLIENT.hoverShadows = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        shadows.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.cursorShadows"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.cursorShadows.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.cursorShadows, val -> ShadowDropConfig.CLIENT.cursorShadows = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        shadows.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.elsewhereShadows"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.elsewhereShadows.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.elsewhereShadows, val -> ShadowDropConfig.CLIENT.elsewhereShadows = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        ConfigCategory.Builder cropping = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.cropping"));

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.hotbarCropped"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.hotbarCropped.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.hotbarCropped, val -> ShadowDropConfig.CLIENT.hotbarCropped = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.slotCropped"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.slotCropped.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.slotCropped, val -> ShadowDropConfig.CLIENT.slotCropped = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.hoverCropped"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.hoverCropped.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.hoverCropped, val -> ShadowDropConfig.CLIENT.hoverCropped = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        ConfigCategory.Builder advanced = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.advanced"));

        advanced.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.translucentItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.translucentItems.tooltip")))
                .binding("#c:glass_blocks, #c:glass_panes, #forge:glass, #forge:glass_panes, minecraft:beacon",
                        () -> String.join(", ", ShadowDropConfig.CLIENT.translucentItems),
                        val -> ShadowDropConfig.CLIENT.translucentItems = new ArrayList<>(Arrays.stream(val.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()))
                .controller(StringControllerBuilder::create)
                .build());

        advanced.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.transparentItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.transparentItems.tooltip")))
                .binding("",
                        () -> String.join(", ", ShadowDropConfig.CLIENT.transparentItems),
                        val -> ShadowDropConfig.CLIENT.transparentItems = new ArrayList<>(Arrays.stream(val.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()))
                .controller(StringControllerBuilder::create)
                .build());

        advanced.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.offsetItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.offsetItems.tooltip")))
                .binding(true, () -> ShadowDropConfig.CLIENT.offsetItems, val -> ShadowDropConfig.CLIENT.offsetItems = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        return builder
                .category(general.build())
                .category(shadows.build())
                .category(cropping.build())
                .category(advanced.build())
                .build()
                .generateScreen(parent);
    }
}
