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
  command(client, 'weather clear');
  command(client, 'fill -4 0 -6 4 0 6 minecraft:stone');
  command(client, 'setblock -2 1 3 tfcm:metal/block/invar');
  command(client, 'setblock -1 1 3 tfcm:ore/normal_native_platinum/granite');
  command(client, 'setblock 0 1 3 tfcm:mineral/quartz_block');
  command(client, 'setblock 1 1 3 tfcm:metal/block/neutronium');
  command(client, 'setblock 2 1 3 tfcm:mineral/budding_certus_quartz');
  assert.match(JSON.stringify(mct(client, 'block', 'get', '1', '1', '3')), /tfcm:metal\/block\/neutronium/);
  command(client, 'summon minecraft:armor_stand 3.5 1 3.5 {NoGravity:1b,ShowArms:1b,ArmorItems:[{id:"tfcm:metal/boots/titanium",count:1},{id:"tfcm:metal/greaves/titanium",count:1},{id:"tfcm:metal/chestplate/titanium",count:1},{id:"tfcm:metal/helmet/titanium",count:1}]}');
  const items = [
    'tfcm:metal/mining_hammer/invar',
    'tfcm:metal/quarry_hammer/titanium',
    'tfcm:metal/excavator/netherite',
    'tfcm:metal/plate/neutronium',
    'tfcm:metal/nugget/neutronium',
    'tfcm:metal/dust/neutronium',
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
    ['neutronium', 'creative-neutronium-items', 'tfcm:metal/ingot/neutronium'],
    ['Mining Hammer', 'creative-hammer-items', 'tfcm:metal/mining_hammer/invar'],
    ['Normal Native Platinum in Granite', 'creative-ore-items', 'tfcm:ore/normal_native_platinum/granite'],
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
  command(client, 'item replace entity @s hotbar.0 with tfcm:metal/ingot/neutronium');
  await camera(client, { x: 1.5, y: 2.6, z: -2.5, yaw: 0, pitch: 18 });
  const first = screenshot(client, 'AC-F028-002', 'neutronium-block-and-ingot-frame-one');
  mct(client, 'wait', '--ticks', '1');
  const second = screenshot(client, 'AC-F028-002', 'neutronium-block-and-ingot-frame-two');
  const firstFrame = PNG.sync.read(readFileSync(path.join(process.env.HARNESS_SCREENSHOT_DIR, first)));
  let animated = false;
  for (let attempt = 0; attempt < 12 && !animated; attempt++) {
    const nextFrame = PNG.sync.read(readFileSync(path.join(process.env.HARNESS_SCREENSHOT_DIR, second)));
    assert.equal(firstFrame.width, nextFrame.width);
    assert.equal(firstFrame.height, nextFrame.height);
    for (let y = Math.floor(firstFrame.height * 0.34); y < Math.floor(firstFrame.height * 0.5) && !animated; y++) {
      for (let x = Math.floor(firstFrame.width * 0.46); x < Math.floor(firstFrame.width * 0.55); x++) {
        const pixel = (y * firstFrame.width + x) * 4;
        if (firstFrame.data[pixel] !== nextFrame.data[pixel]
            || firstFrame.data[pixel + 1] !== nextFrame.data[pixel + 1]
            || firstFrame.data[pixel + 2] !== nextFrame.data[pixel + 2]) {
          animated = true;
          break;
        }
      }
    }
    if (!animated) {
      mct(client, 'wait', '--ticks', '1');
      mct(client, 'screenshot', '--output', path.join(process.env.HARNESS_SCREENSHOT_DIR, second));
    }
  }
  assert.ok(animated, 'Placed Neutronium did not change between rendered frames');

  const log = readFileSync(path.join(process.env.MCT_HOME, 'clients', client, 'minecraft', 'logs', 'latest.log'), 'utf8');
  assert.doesNotMatch(log, /(?:Missing textures in model|Unable to load model|Failed to load (?:model|texture)) tfcm:|tfcm:.*(?:missing model|missing texture)/i);
});
