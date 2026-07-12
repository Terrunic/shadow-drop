<img width="380" height="224" alt="Shadow Drop" src="https://github.com/user-attachments/assets/3c5ec8a5-b35e-4f08-90c8-b6a27a3efa61" />

A subtle client-side mod that adds drop shadows beneath items in GUIs, adding depth to the hotbar, inventory screens, advancements, etc. By default, shadows are translucent, offset by one pixel down/right, and cropped to fit within item slots.

⚠ *As shadows effectively double the number of items rendered in GUIs, expect a performance tradeoff, especially when many items are visible at once.*

<img width="1600" height="768" alt="Shadow Drop example" src="https://github.com/user-attachments/assets/2ab817a1-1c61-429a-8771-af39e3ccc3e4" />

Shadows can be configured via the generated `shadowdrop-client.toml` file.
> Note: for cropping shadows, item slots are detected using the color of the pixel to the bottom right of each item. Adjust `slotBrColors` for compatibility with resource packs or modded GUIs where this pixel is not #FFFFFF.

---

⚠ **Mods that may need configuration for proper compatibility:**
- **EMI**: disable `use-batched-renderer`
- **ImmediatelyFast**: disable `hud_batching`
- **Accelerated Rendering**: disable `gui_acceleration`
