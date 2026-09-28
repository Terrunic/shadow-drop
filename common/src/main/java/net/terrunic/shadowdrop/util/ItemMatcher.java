package net.terrunic.shadowdrop.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Matcher for a config list of item and tag ids, parsed once
public final class ItemMatcher {
    private final List<String> source;
    private final Set<Item> items = new HashSet<>();
    private final List<TagKey<Item>> tags = new ArrayList<>();

    private ItemMatcher(List<String> source) {
        this.source = source;

        for (String entry : source) {
            // Entries starting with # are tag ids, otherwise are item ids
            boolean isTag = entry.startsWith("#");
            String id = isTag ? entry.substring(1) : entry;
            if (!id.contains(":")) continue;

            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null) continue;

            if (isTag) {
                tags.add(TagKey.create(Registries.ITEM, location));
            } else {
                BuiltInRegistries.ITEM.getOptional(location).ifPresent(items::add);
            }
        }
    }

    // Reuse matcher while the config list is unchanged
    public static ItemMatcher of(ItemMatcher cached, List<String> entries) {
        return cached != null && cached.source == entries ? cached : new ItemMatcher(entries);
    }

    public boolean matches(ItemStack stack) {
        if (items.contains(stack.getItem())) return true;
        for (TagKey<Item> tag : tags) {
            if (stack.is(tag)) return true;
        }
        return false;
    }
}
