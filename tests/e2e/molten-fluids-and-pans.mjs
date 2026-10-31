import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);

const metals = ['compressed_iron', 'platinum', 'naquadah', 'iridium', 'osmium', 'osmiridium', 'mithril', 'arcane',
  'refined_glowstone', 'refined_obsidian', 'antimony', 'titanium', 'cobalt', 'lithium', 'aluminum', 'constantan',
  'invar', 'electrum', 'lead', 'uranium', 'tungsten', 'solder', 'tungsten_steel', 'netherite', 'dawnstone',
  'andesite_alloy', 'draconium', 'awakened_draconium', 'boron', 'thorium', 'magnesium', 'beryllium', 'zirconium',
  'neutronium', 'ferroboron', 'tough_alloy', 'zircaloy', 'hsla_steel', 'super_alloy'];
// TFC's own deposit first, as the reference for how a filled pan looks
const deposits = ['tfc:deposit/cassiterite/andesite', 'tfc:rock/gravel/andesite', 'tfc:sand/red', 'tfcm:ore_processing/rock_powder',
  'tfcm:ore_processing/rocky_chunks/galena', 'tfcm:ore_processing/dirty_dust/native_gold'];
const COLUMNS = 13;

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'time set day');
  command(client, 'kill @e[type=minecraft:item_frame]');
  command(client, 'fill -16 0 -4 16 12 16 minecraft:air');
  command(client, 'fill -16 0 8 16 12 9 minecraft:stone');
  command(client, 'fill -16 0 0 16 0 8 minecraft:stone');
  for (const [index, metal] of metals.entries()) {
    const x = index % COLUMNS - 6;
    const row = Math.floor(index / COLUMNS);
    command(client, `summon minecraft:item_frame ${x} ${6 - row} 7 {Facing:2b,Fixed:1b,Item:{id:"tfcm:bucket/metal/${metal}",count:1}}`);
  }
  await camera(client, { x: 0.5, y: 3.5, z: 0.5, yaw: 0, pitch: 0 });
  await new Promise(resolve => setTimeout(resolve, 5000));
  screenshot(client, 'AC-F028-001', 'molten-metal-buckets');

  command(client, 'kill @e[type=minecraft:item_frame]');
  for (const [index, metal] of metals.entries()) {
    const x = index % COLUMNS - 6;
    const y = 6 - Math.floor(index / COLUMNS);
    command(client, `setblock ${x} ${y} 7 minecraft:glass`);
    command(client, `setblock ${x} ${y} 8 tfcm:fluid/metal/${metal}`);
  }
  await camera(client, { x: 0.5, y: 3.5, z: 0.5, yaw: 0, pitch: 0 });
  await new Promise(resolve => setTimeout(resolve, 3000));
  screenshot(client, 'AC-F028-001', 'molten-metal-fluid-blocks');

  command(client, 'fill -16 1 8 16 12 9 minecraft:stone');
  command(client, 'fill -16 1 0 16 12 7 minecraft:air');
  for (const [index, deposit] of deposits.entries()) {
    command(client, `summon minecraft:item_frame ${index - 3} 4 7 {Facing:2b,Fixed:1b,Item:{id:"tfc:pan/filled",count:1,components:{"tfc:deposit":{id:"${deposit}",count:1}}}}`);
    command(client, `item replace entity @s hotbar.${index} with tfc:pan/filled[tfc:deposit={id:"${deposit}",count:1}]`);
  }
  await camera(client, { x: 0.5, y: 4.5, z: 3.5, yaw: 0, pitch: 0 });
  await new Promise(resolve => setTimeout(resolve, 3000));
  screenshot(client, 'AC-F028-001', 'pans-show-their-material');

  // Small Ore as an item is a flat sprite, not the world's groundcover model
  command(client, 'item replace entity @s hotbar.8 with tfcm:ore/small_galena');
  await camera(client, { x: 0.5, y: 4.5, z: 3.5, yaw: 0, pitch: 0 });
  screenshot(client, 'AC-F028-001', 'small-ore-item');

  const log = readFileSync(path.join(process.env.MCT_HOME, 'clients', client, 'minecraft', 'logs', 'latest.log'), 'utf8');
  assert.doesNotMatch(log, /Unable to load model: 'tfcm:|Missing textures in model tfcm:item\/pan|tfcm:item\/pan[^\n]*(missing|Missing)/);
});
