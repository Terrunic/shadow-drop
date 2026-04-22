<img width="380" height="224" alt="Shadow Drop" src="https://github.com/user-attachments/assets/3c5ec8a5-b35e-4f08-90c8-b6a27a3efa61" />

<a href='https://files.minecraftforge.net'><img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg"></a>
<a href='https://fabricmc.net'><img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"></a>
<a href='https://neoforged.net/'><img alt="neoforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg"></a>

This is a 26.1 multiloader port of Terrunic's excellent mod [Shadow Drop](https://modrinth.com/mod/shadow-drop). This port should function similarly to the original, except with native support for Fabric and Cloth Config. 

Description from the original mod page:

Shadow drop is a subtle client-side mod that adds drop shadows beneath items in GUIs, adding depth to the hotbar, inventory screens, advancements, etc. By default, shadows are translucent, offset by one pixel down/right, and cropped to fit within item slots.

⚠ *As shadows effectively double the number of items rendered in GUIs, expect a performance tradeoff, especially when many items are visible at once.*

<img width="1600" height="768" alt="Shadow Drop example" src="https://github.com/user-attachments/assets/2ab817a1-1c61-429a-8771-af39e3ccc3e4" />

Shadows can be configured via the generated `shadowdrop-client.toml` file.
> Note: for cropping shadows, item slots are detected using the color of the pixel to the bottom right of each item. Adjust `slotBrColors` for compatibility with resource packs or modded GUIs where this pixel is not #FFFFFF.

---

⚠ **Mods that may need configuration for proper compatibility:**
- **EMI**: disable `use-batched-renderer`
- **ImmediatelyFast**: disable `hud_batching`
- **Accelerated Rendering**: disable `gui_acceleration`

## License

[![Code license (MIT)](https://img.shields.io/badge/code%20license-MIT-green.svg?style=flat-square)](https://github.com/evanbones/Shadow-Drop/blob/1.20.1/LICENSE)

---

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://discord.com/invite/JcGRdT6Pbx) [![github-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/github-plural_vector.svg)](https://github.com/evanbones/Shadow-Drop)
