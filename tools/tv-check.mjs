#!/usr/bin/env node
// Checks the app on an Android TV over adb: starts it (a debug build at --url, if given), sees that
// it is in front, finds its WebView's DevTools (a debug build's only), and reports what the page
// gets: the user agent and TV mode, the viewport and pixel ratio, WebGL 2 and the GPU, the Gamepad
// API, and the frame rate standing, walking the hall and facing a door. Writes a JSON report (and
// the screen, --screenshot) to perf/ and prints a summary. --help for usage.

import { execFile } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs, promisify } from 'node:util';

const execFileP = promisify(execFile);
const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const APP = 'io.github.ariamokr.hallway';

const HELP = `Usage: node tools/tv-check.mjs [options]

Options:
  --url <url>      the page to open (a debug build only; else the app's own start, the hallway)
  --serial <id>    adb device serial (else ANDROID_SERIAL, or the one device connected)
  --seconds <n>    how long to time each phase (default 4)
  --screenshot     also save the screen as a PNG
  --out <dir>      where to write the report (default perf/)
  -h, --help       this help

Needs Node 22 or later (its WebSocket) and adb (on the PATH, or in ANDROID_HOME's platform-tools),
with the TV connected (adb connect <tv-address>) and the app installed. A release build has no
DevTools, so for it the check stops once the app is in front. A --url on this computer's
localhost is reached over adb reverse.`;

const { values: opt } = parseArgs({
  options: {
    url: { type: 'string' },
    serial: { type: 'string' },
    seconds: { type: 'string', default: '4' },
    screenshot: { type: 'boolean', default: false },
    out: { type: 'string', default: path.join(ROOT, 'perf') },
    help: { type: 'boolean', short: 'h', default: false },
  },
});
if (opt.help) {
  console.log(HELP);
  process.exit(0);
}

const ADB = (() => {
  const home = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
  const exe = home && path.join(home, 'platform-tools', process.platform === 'win32' ? 'adb.exe' : 'adb');
  return exe && fs.existsSync(exe) ? exe : 'adb';
})();
let serial = opt.serial || process.env.ANDROID_SERIAL || '';
const adb = async (...args) => (await execFileP(ADB, serial ? ['-s', serial, ...args] : args, { maxBuffer: 64 << 20, encoding: args[0] === 'exec-out' ? 'buffer' : 'utf8' })).stdout;
const shell = async (cmd) => String(await adb('shell', cmd)).trim();
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

if (!serial) {
  const devices = String(await adb('devices')).split('\n').slice(1).map((l) => l.trim().split(/\s+/)).filter(([s, state]) => s && state === 'device');
  if (devices.length !== 1) {
    console.error(devices.length ? `Several devices: pick one with --serial (${devices.map(([s]) => s).join(', ')})` : 'No device: adb connect <tv-address> first');
    process.exit(1);
  }
  serial = devices[0][0];
}

const report = { when: new Date().toISOString(), device: {}, launch: {}, page: null, frames: null };
const stamp = report.when.replace(/[:.]/g, '-').slice(0, 19);
report.device = {
  model: await shell('getprop ro.product.model'),
  android: await shell('getprop ro.build.version.release'),
  sdk: Number(await shell('getprop ro.build.version.sdk')),
  webview: (await shell(`dumpsys webviewupdate | grep -m1 'Current WebView package'`)).replace(/^.*package \(name, version\): /, ''),
  app: (await shell(`dumpsys package ${APP} | grep -m1 versionName`)).replace(/^.*versionName=/, ''),
};
if (!report.device.app) {
  console.error(`${APP} is not installed on ${report.device.model}`);
  process.exit(1);
}

// Start it, awake, in front, at the page asked for.
const reversed = [];
if (opt.url) {
  const u = new URL(opt.url);
  if (['localhost', '127.0.0.1'].includes(u.hostname) && u.port) {
    await adb('reverse', `tcp:${u.port}`, `tcp:${u.port}`);
    reversed.push(u.port);
  }
}
await shell('input keyevent KEYCODE_WAKEUP');
await shell(`am start -W -n ${APP}/.MainActivity${opt.url ? ` --es url '${opt.url}'` : ''}`);
await sleep(2000);
const resumed = await shell('dumpsys activity activities | grep -m1 -E "mResumedActivity|topResumedActivity"');
report.launch.inFront = resumed.includes(APP);
// Full screen: its window's frame is the whole display's, and it asked for the system bars hidden.
const win = (await shell('dumpsys window windows')).split('Window #').find((w) => w.includes(`${APP}/`)) ?? '';
const frame = win.match(/mFrame=(\[\S+\])/)?.[1];
report.launch.frame = frame ?? null;
report.launch.fullScreen = !!frame && frame === win.match(/display=(\[\S+\])/)?.[1] && !/STATUS_BAR: visible/.test(win);

const cleanUp = async (forwarded) => {
  if (forwarded) await adb('forward', '--remove', `tcp:${forwarded}`).catch(() => {});
  for (const port of reversed) await adb('reverse', '--remove', `tcp:${port}`).catch(() => {});
};

async function inspect(port) {
  const page = (await fetch(`http://127.0.0.1:${port}/json/list`).then((r) => r.json())).find((p) => p.type === 'page');
  if (!page) throw new Error('The WebView has no page');
  const ws = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise((res, rej) => {
    ws.onopen = res;
    ws.onerror = () => rej(new Error('No DevTools connection'));
  });
  let seq = 0;
  const pending = new Map();
  ws.onmessage = ({ data }) => {
    const m = JSON.parse(data);
    if (pending.has(m.id)) pending.get(m.id)(m);
  };
  const evaluate = async (expression) => {
    const id = ++seq;
    const m = await new Promise((res) => {
      pending.set(id, res);
      ws.send(JSON.stringify({ id, method: 'Runtime.evaluate', params: { expression, awaitPromise: true, returnByValue: true } }));
    });
    pending.delete(id);
    if (m.error || m.result.exceptionDetails) throw new Error(m.error?.message || m.result.exceptionDetails.exception?.description || 'exception');
    return m.result.result.value;
  };
  try {
    for (let i = 0; i < 40 && !(await evaluate('!!window.__hallway?.renderer')); i++) await sleep(500);
    report.page = await evaluate(`(() => {
      const r = window.__hallway?.renderer, gl = r?.getContext();
      const info = gl?.getExtension('WEBGL_debug_renderer_info');
      return {
        url: location.href,
        userAgent: navigator.userAgent,
        tvMode: document.documentElement.classList.contains('tv'),
        hallwayTV: typeof window.hallwayTV?.back === 'function' && typeof window.hallwayTV?.menu === 'function',
        viewport: innerWidth + 'x' + innerHeight,
        devicePixelRatio,
        pixelRatio: r?.getPixelRatio() ?? null,
        buffer: r ? r.domElement.width + 'x' + r.domElement.height : null,
        webgl2: typeof WebGL2RenderingContext !== 'undefined' && gl instanceof WebGL2RenderingContext,
        gpu: gl ? gl.getParameter(info ? info.UNMASKED_RENDERER_WEBGL : gl.RENDERER) : null,
        maxTexture: gl?.getParameter(gl.MAX_TEXTURE_SIZE) ?? null,
        gamepadAPI: typeof navigator.getGamepads === 'function',
        gamepads: typeof navigator.getGamepads === 'function' ? [...navigator.getGamepads()].filter(Boolean).map((p) => p.id) : [],
      };
    })()`);
    if (await evaluate('!!window.__hallway?.layout')) report.frames = await evaluate(FRAMES.replace('MS', String(Number(opt.seconds) * 1000)));
  } finally {
    ws.close();
  }
}

// Evaluated in the hallway: rAF intervals at its own pixel ratio, standing at the entrance, walking
// the hall to its end, and facing the first open door from 0.6 m off its wall (the doorway then
// fills most of the view). A frame counts as dropped over 25 ms, 1.5 frames at 60 Hz.
const FRAMES = `(async () => {
  const h = __hallway, L = h.layout, c = h.controls, ms = MS;
  const frames = (dur, each) => new Promise((resolve) => {
    const out = [];
    let last = 0, start = 0;
    const f = (t) => {
      if (!start) start = t;
      if (last) out.push(t - last);
      last = t;
      if (each) each((t - start) / dur);
      if (t - start < dur) requestAnimationFrame(f);
      else resolve(out);
    };
    requestAnimationFrame(f);
  });
  const sum = (xs) => {
    const s = [...xs].sort((a, b) => a - b), mean = xs.reduce((a, b) => a + b, 0) / xs.length;
    return { fps: Math.round(10000 / mean) / 10, dropped: xs.filter((x) => x > 25).length, p95: Math.round(s[Math.floor(s.length * 0.95)] * 10) / 10, worst: Math.round(s[s.length - 1] * 10) / 10, frames: xs.length };
  };
  const pose = [c.rig.position.x, c.rig.position.z, c.rig.rotation.y];
  const door = L.doors.find((d) => d.env);
  const out = {};
  c.setPose(0, -0.6, 0);
  await frames(800);
  out.stand = sum(await frames(ms));
  out.drawCalls = h.renderer.info.render.calls;
  out.walk = sum(await frames(ms * 1.5, (p) => c.setPose(0, -0.6 + (L.zEnd + 1.2) * Math.min(1, p), 0)));
  c.setPose(door.side * 0.6, door.z, -door.side * Math.PI / 2);
  await frames(600);
  out.door = sum(await frames(ms));
  c.setPose(...pose);
  return out;
})()`;

async function finish() {
  if (!report.launch.devtools) await cleanUp(null);
  fs.mkdirSync(opt.out, { recursive: true });
  if (opt.screenshot) {
    const png = path.join(opt.out, `tv-${stamp}.png`);
    fs.writeFileSync(png, await adb('exec-out', 'screencap', '-p'));
    report.screenshot = path.basename(png);
  }
  const file = path.join(opt.out, `tv-${stamp}.json`);
  fs.writeFileSync(file, JSON.stringify(report, null, 2) + '\n');

  const d = report.device, p = report.page, f = report.frames;
  console.log(`${d.model}, Android ${d.android} (API ${d.sdk}), WebView ${d.webview}, the app ${d.app}`);
  console.log(`In front: ${report.launch.inFront ? 'yes' : 'NO'}; full screen: ${report.launch.fullScreen ? 'yes' : 'NO'} (${report.launch.frame})`);
  if (!report.launch.devtools) console.log('No DevTools (a release build): nothing more to check.');
  if (p) {
    console.log(`Page: ${p.url}`);
    console.log(`TV mode: ${p.tvMode ? 'yes' : 'NO'}; hallwayTV: ${p.hallwayTV ? 'yes' : 'NO'}; ${p.userAgent.match(/HallwayTV\/\S+/)?.[0] ?? 'no HallwayTV in the user agent'}`);
    console.log(`Viewport ${p.viewport} at ${p.devicePixelRatio}x; rendering at ${p.pixelRatio}x, ${p.buffer}`);
    console.log(`WebGL 2: ${p.webgl2 ? 'yes' : 'NO'}; ${p.gpu}; textures up to ${p.maxTexture}`);
    console.log(`Gamepad API: ${p.gamepadAPI ? 'yes' : 'no'}${p.gamepads.length ? ` (${p.gamepads.join(', ')})` : ''}`);
  }
  if (f) for (const phase of ['stand', 'walk', 'door']) console.log(`${phase.padEnd(6)} ${f[phase].fps} fps, ${f[phase].dropped} dropped of ${f[phase].frames}, p95 ${f[phase].p95} ms, worst ${f[phase].worst} ms`);
  if (f) console.log(`Draw calls: ${f.drawCalls}`);
  console.log(`Report: ${path.relative(process.cwd(), file)}`);
  process.exit(report.launch.inFront && report.launch.fullScreen && (!p || (p.tvMode && p.webgl2)) ? 0 : 1);
}

// Its WebView's DevTools: a debug build's only.
const pid = await shell(`pidof ${APP}`);
const sockets = await shell('cat /proc/net/unix');
const socket = `webview_devtools_remote_${pid}`;
if (!pid || !sockets.includes(`@${socket}`)) {
  report.launch.devtools = false;
  await finish();
} else {
  report.launch.devtools = true;
  const port = Number(String(await adb('forward', 'tcp:0', `localabstract:${socket}`)).trim());
  try {
    await inspect(port);
  } finally {
    await cleanUp(port);
  }
  await finish();
}
