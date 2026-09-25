// Signed callbacks to AppCoWeb.
// Usage: node scripts/report.mjs step "<name>" <status>
//        node scripts/report.mjs job <status> [progress] [errorCode] [errorMessage]
//        node scripts/report.mjs upload <platform> <artifactType> <filePath>
import { createHmac, createHash } from "node:crypto";
import { readFileSync, statSync } from "node:fs";
import { basename } from "node:path";
const { APPCOWEB_CALLBACK_URL: URL_, APPCOWEB_CALLBACK_KEY: KEY, APPCOWEB_JOB_ID: JOB } = process.env;
if (!URL_ || !KEY || !JOB) { console.log("AppCoWeb reporting disabled (vars missing)"); process.exit(0); }
async function send(payload) {
  const body = JSON.stringify({ buildJobId: JOB, ...payload });
  const sig = createHmac("sha256", KEY).update(body).digest("hex");
  const res = await fetch(URL_, { method: "POST", headers: { "content-type": "application/json", "x-appcoweb-signature": sig }, body });
  const text = await res.text();
  if (!res.ok) throw new Error(`AppCoWeb callback ${res.status}: ${text.slice(0, 200)}`);
  return JSON.parse(text || "{}");
}
const [cmd, ...a] = process.argv.slice(2);
try {
  if (cmd === "step") await send({ type: "step", stepName: a[0], status: a[1] });
  else if (cmd === "job") await send({ type: "job", status: a[0], ...(a[1] ? { progress: Number(a[1]) } : {}), ...(a[2] ? { errorCode: a[2] } : {}), ...(a[3] ? { errorMessage: a[3] } : {}) });
  else if (cmd === "upload") {
    const [platform, artifactType, file] = a;
    const fileName = basename(file);
    const buf = readFileSync(file);
    const up = await send({ type: "upload_url", platform, fileName });
    const put = await fetch(up.signedUrl, { method: "PUT", headers: { "content-type": "application/octet-stream", "x-upsert": "true" }, body: buf });
    if (!put.ok) throw new Error(`Upload failed ${put.status}`);
    await send({ type: "artifact", platform, artifactType, fileName, path: up.path, fileSize: statSync(file).size, checksum: createHash("sha256").update(buf).digest("hex") });
    console.log("Uploaded", fileName);
  } else throw new Error("unknown command");
} catch (e) { console.error(String(e.message ?? e)); process.exit(1); }
