import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';

const { scenario, command, camera, screenshot, mct } = await import(process.env.HARNESS_E2E_LIB);
const require = createRequire(path.join(process.env.MCT_HOME, '..', 'pilot', 'package.json'));
const { PNG } = require('pngjs');

await scenario(async ({ clients }) => {
  const client = clients[0];
  command(client, 'gamemode creative');
  command(client, 'gamerule sendCommandFeedback false');
  command(client, 'time set day');
  command(client, 'execute if entity @e[type=minecraft:item_frame] run kill @e[type=minecraft:item_frame]');
  command(client, 'execute if entity @e[type=minecraft:armor_stand] run kill @e[type=minecraft:armor_stand]');
  command(client, 'execute if entity @e[type=minecraft:item] run kill @e[type=minecraft:item]');
  command(client, 'fill -12 1 -10 12 10 8 minecraft:air');
  command(client, 'fill -4 0 -6 4 0 6 minecraft:stone');
  command(client, 'setblock -2 1 3 tfcm:metal/block/invar');
  command(client, 'setblock -1 1 3 tfcm:ore/normal_native_platinum/granite');
  command(client, 'setblock 0 1 3 tfcm:mineral/quartz_block');
  command(client, 'setblock 1 1 3 tfcm:metal/block/naquadah');
  command(client, 'setblock 2 1 3 tfcm:mineral/budding_certus_quartz');
  assert.match(JSON.stringify(mct(client, 'block', 'get', '1', '1', '3')), /tfcm:metal\/block\/naquadah/);
  command(client, 'summon minecraft:armor_stand 3.5 1 3.5 {NoGravity:1b,ShowArms:1b,ArmorItems:[{id:"tfcm:metal/boots/titanium",count:1},{id:"tfcm:metal/greaves/titanium",count:1},{id:"tfcm:metal/chestplate/titanium",count:1},{id:"tfcm:metal/helmet/titanium",count:1}]}');
  const items = [
    'tfcm:metal/mining_hammer/invar',
    'tfcm:metal/quarry_hammer/titanium',
    'tfcm:metal/excavator/netherite',
    'tfcm:metal/plate/naquadah',
    'tfcm:metal/nugget/naquadah',
    'tfcm:metal/dust/naquadah',
    'tfcm:metal/sword/tungsten_steel',
    'tfcm:ore_processing/powder/native_platinum',
    'tfcm:gem/certus_quartz'
  ];
  for (let slot = 0; slot < items.length; slot++) {
    command(client, `item replace entity @s hotbar.${slot} with ${items[slot]}`);
  }
  await camera(client, { x: 0.5, y: 2.6, z: -2.5, yaw: 0, pitch: 18 });
  const sceneImage = screenshot(client, 'AC-F028-001', 'blocks-and-items');
  const image = PNG.sync.read(readFileSync(path.join(process.env.HARNESS_SCREENSHOT_DIR, sceneImage)));
  for (const [query, label, item] of [
    ['naquadah', 'creative-naquadah-items', 'tfcm:metal/ingot/naquadah'],
    ['Mining Hammer', 'creative-hammer-items', 'tfcm:metal/mining_hammer/invar'],
    ['Normal Granite Native Platinum', 'creative-ore-items', 'tfcm:ore/normal_native_platinum/granite'],
    ['Quartz Block', 'creative-quartz-items', 'tfcm:mineral/quartz_block']
  ]) {
    mct(client, 'input', 'key', 'press', 'e');
    mct(client, 'gui', 'wait-open');
    assert.match(JSON.stringify(mct(client, 'gui', 'info')), /creative/i);
    const layout = mct(client, 'gui', 'layout');
    mct(client, 'input', 'click',
      String(Math.round(1805 * layout.scaledWidth / image.width)),
      String(Math.round(235 * layout.scaledHeight / image.height)));
    mct(client, 'input', 'type', query);
    mct(client, 'wait', '--ticks', '5');
    const slots = mct(client, 'gui', 'snapshot').slots;
    assert.ok(slots.slice(0, 45).some(slot => JSON.stringify(slot).includes(item)), `Creative search did not display ${item}`);
    screenshot(client, 'AC-F028-001', label);
    mct(client, 'gui', 'close');
  }
  await camera(client, { x: 0.5, y: 2.6, z: -2.5, yaw: 0, pitch: 18 });
  screenshot(client, 'AC-F028-001', 'equipped-armor');
  mct(client, 'block', 'break', '-2', '1', '3');
  mct(client, 'wait', '--ticks', '1');
  screenshot(client, 'AC-F028-001', 'block-break-particles');
  command(client, 'execute if entity @e[type=minecraft:item_frame] run kill @e[type=minecraft:item_frame]');
  command(client, 'fill 30 0 -8 42 0 6 minecraft:smooth_stone');
  command(client, 'fill 30 1 5 42 7 5 minecraft:smooth_stone');
  const forms = ['rocky_chunks', 'chunks', 'dirty_dust', 'powder', 'pellet', 'briquet'];
  for (const [row, series] of ['native_copper', 'native_platinum', 'borax'].entries()) {
    const stages = series === 'borax' ? forms.slice(0, 4) : forms;
    for (const [column, form] of stages.entries()) {
      command(client, `summon minecraft:item_frame ${31 + column * 2} ${6 - row * 2} 4.49 {Facing:2b,Fixed:1b,Item:{id:"tfcm:ore_processing/${form}/${series}",count:1}}`);
    }
  }
  await camera(client, { x: 36.5, y: 4, z: -6, yaw: 0, pitch: 0 });
  screenshot(client, 'AC-F028-001', 'source-processing-stages');
  command(client, 'execute if entity @e[type=minecraft:armor_stand] run kill @e[type=minecraft:armor_stand]');
  for (const [index, metal] of ['invar', 'titanium', 'netherite', 'tungsten_steel'].entries()) {
    command(client, `summon minecraft:armor_stand ${32 + index * 3} 1 0 {NoGravity:1b,ShowArms:1b,Rotation:[180f,0f],HandItems:[{id:"tfcm:metal/javelin/${metal}",count:1},{id:"tfcm:metal/shield/${metal}",count:1}],Pose:{RightArm:[-90f,0f,0f],LeftArm:[-90f,0f,0f]}}`);
  }
  command(client, 'item replace entity @s hotbar.0 with tfcm:metal/javelin/invar');
  await camera(client, { x: 36.5, y: 2.4, z: -6.5, yaw: 0, pitch: 8 });
  screenshot(client, 'AC-F028-001', 'custom-javelins-and-shields');

  const log = readFileSync(path.join(process.env.MCT_HOME, 'clients', client, 'minecraft', 'logs', 'latest.log'), 'utf8');
  assert.doesNotMatch(log, /(?:Missing textures in model|Unable to load model|Failed to load (?:model|texture)) tfcm:|tfcm:.*(?:missing model|missing texture)/i);
});
