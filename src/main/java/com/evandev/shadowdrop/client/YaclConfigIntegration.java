package com.evandev.shadowdrop.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.evandev.shadowdrop.ShadowDropConfig;

import java.util.Arrays;

public class YaclConfigIntegration {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.shadowdrop.title"));

        ConfigCategory.Builder general = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.general"));

        general.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.modEnabled"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.modEnabled.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.modEnabled, ShadowDropConfig.CLIENT.modEnabled::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        general.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowColor"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowColor.tooltip")))
                .binding("#000000", ShadowDropConfig.CLIENT.shadowColor, val -> {
                    if (val.matches("^#[0-9A-Fa-f]{6}$")) {
                        ShadowDropConfig.CLIENT.shadowColor.set(val);
                    }
                })
                .controller(StringControllerBuilder::create)
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowAlpha"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowAlpha.tooltip")))
                .binding(99, ShadowDropConfig.CLIENT.shadowAlpha, ShadowDropConfig.CLIENT.shadowAlpha::set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 255).step(1))
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowXOffset"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowXOffset.tooltip")))
                .binding(1, ShadowDropConfig.CLIENT.shadowXOffset, ShadowDropConfig.CLIENT.shadowXOffset::set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 4).step(1))
                .build());

        general.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowYOffset"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowYOffset.tooltip")))
                .binding(1, ShadowDropConfig.CLIENT.shadowYOffset, ShadowDropConfig.CLIENT.shadowYOffset::set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 4).step(1))
                .build());

        general.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.offsetItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.offsetItems.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.offsetItems, ShadowDropConfig.CLIENT.offsetItems::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        ConfigCategory.Builder contexts = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.contexts"));

        contexts.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowsAlways"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowsAlways.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.shadowsAlways, ShadowDropConfig.CLIENT.shadowsAlways::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        contexts.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowsInSlots"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowsInSlots.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.shadowsInSlots, ShadowDropConfig.CLIENT.shadowsInSlots::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        contexts.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowsInHotbar"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowsInHotbar.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.shadowsInHotbar, ShadowDropConfig.CLIENT.shadowsInHotbar::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        contexts.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.shadowsInCursor"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.shadowsInCursor.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.shadowsInCursor, ShadowDropConfig.CLIENT.shadowsInCursor::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        ConfigCategory.Builder cropping = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.cropping"));

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.cropToSlots"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.cropToSlots.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.cropToSlots, ShadowDropConfig.CLIENT.cropToSlots::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.cropToHotbar"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.cropToHotbar.tooltip")))
                .binding(true, ShadowDropConfig.CLIENT.cropToHotbar, ShadowDropConfig.CLIENT.cropToHotbar::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        cropping.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.uncropUnderCursor"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.uncropUnderCursor.tooltip")))
                .binding(false, ShadowDropConfig.CLIENT.uncropUnderCursor, ShadowDropConfig.CLIENT.uncropUnderCursor::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        ConfigCategory.Builder advanced = ConfigCategory.createBuilder()
                .name(Component.translatable("config.shadowdrop.category.advanced"));

        advanced.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.slotBrColors"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.slotBrColors.tooltip")))
                .binding("#FFFFFF",
                        () -> String.join(", ", ShadowDropConfig.CLIENT.slotBrColors.get()),
                        val -> ShadowDropConfig.CLIENT.slotBrColors.set(Arrays.stream(val.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()))
                .controller(StringControllerBuilder::create)
                .build());

        advanced.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.translucentItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.translucentItems.tooltip")))
                .binding("#c:glass, #c:glass_panes, #forge:glass, #forge:glass_panes, minecraft:beacon",
                        () -> String.join(", ", ShadowDropConfig.CLIENT.translucentItems.get()),
                        val -> ShadowDropConfig.CLIENT.translucentItems.set(Arrays.stream(val.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()))
                .controller(StringControllerBuilder::create)
                .build());

        advanced.option(Option.<String>createBuilder()
                .name(Component.translatable("config.shadowdrop.transparentItems"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.transparentItems.tooltip")))
                .binding("",
                        () -> String.join(", ", ShadowDropConfig.CLIENT.transparentItems.get()),
                        val -> ShadowDropConfig.CLIENT.transparentItems.set(Arrays.stream(val.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()))
                .controller(StringControllerBuilder::create)
                .build());

        advanced.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.shadowdrop.forceDepthRefresh"))
                .description(OptionDescription.of(Component.translatable("config.shadowdrop.forceDepthRefresh.tooltip")))
                .binding(false, ShadowDropConfig.CLIENT.forceDepthRefresh, ShadowDropConfig.CLIENT.forceDepthRefresh::set)
                .controller(TickBoxControllerBuilder::create)
                .build());

        return builder
                .category(general.build())
                .category(contexts.build())
                .category(cropping.build())
                .category(advanced.build())
                .build()
                .generateScreen(parent);
    }
}
