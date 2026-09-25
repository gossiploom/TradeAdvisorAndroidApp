// Writes capacitor.config.json from the config AppCoWeb sends to Codemagic.
import { writeFileSync } from "node:fs";
const b64 = process.env.CAPACITOR_CONFIG_B64;
if (!b64) { console.error("CAPACITOR_CONFIG_B64 missing"); process.exit(1); }
const cfg = JSON.parse(Buffer.from(b64, "base64").toString("utf8"));
if (!/^https:\/\//.test(cfg?.server?.url ?? "")) { console.error("Website URL must be https"); process.exit(1); }
writeFileSync("capacitor.config.json", JSON.stringify(cfg, null, 2));
console.log("Configured", cfg.appId, cfg.appName);
