// PKCE Smoke Test
// Copy to app.js and replace placeholders.
//
// Required placeholders:
//   __HOSTED_UI__    e.g., https://<domainPrefix>.auth.us-east-1.amazoncognito.com
//   __CLIENT_ID__    Cognito User Pool App Client ID
//
// IMPORTANT:
// - This file is safe to commit as a template.
// - Your local smoke-pkce/app.js should NOT be committed (add it to .gitignore).

const cfg = {
  cognitoDomain: "__HOSTED_UI__",
  clientId: "__CLIENT_ID__",
  redirectUri: "https://sso-poc.example.net/callback",
  logoutUri: "https://sso-poc.example.net/",
  scopes: "openid+email+profile"
};

async function sha256(buffer) {
  const digest = await crypto.subtle.digest("SHA-256", buffer);
  return new Uint8Array(digest);
}

function base64UrlEncode(bytes) {
  let str = btoa(String.fromCharCode(...bytes));
  return str.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function randomString(len = 64) {
  const bytes = new Uint8Array(len);
  crypto.getRandomValues(bytes);
  return base64UrlEncode(bytes);
}

async function pkceChallengeFromVerifier(verifier) {
  const bytes = new TextEncoder().encode(verifier);
  const hashed = await sha256(bytes);
  return base64UrlEncode(hashed);
}

function decodeJwt(token) {
  const payload = token.split(".")[1];
  const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
  return JSON.parse(json);
}

async function login() {
  const verifier = randomString(64);
  sessionStorage.setItem("pkce_verifier", verifier);

  const challenge = await pkceChallengeFromVerifier(verifier);

  const url =
    `${cfg.cognitoDomain}/oauth2/authorize` +
    `?client_id=${encodeURIComponent(cfg.clientId)}` +
    `&response_type=code` +
    `&scope=${cfg.scopes}` +
    `&redirect_uri=${encodeURIComponent(cfg.redirectUri)}` +
    `&code_challenge=${encodeURIComponent(challenge)}` +
    `&code_challenge_method=S256`;

  location.assign(url);
}

async function exchangeCodeForTokens(code) {
  const verifier = sessionStorage.getItem("pkce_verifier");
  if (!verifier) {
    throw new Error("Missing PKCE verifier in sessionStorage (was the login started in this same browser session?)");
  }

  const body = new URLSearchParams();
  body.set("grant_type", "authorization_code");
  body.set("client_id", cfg.clientId);
  body.set("code", code);
  body.set("redirect_uri", cfg.redirectUri);
  body.set("code_verifier", verifier);

  const resp = await fetch(`${cfg.cognitoDomain}/oauth2/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: body.toString()
  });

  if (!resp.ok) {
    const txt = await resp.text();
    throw new Error(`Token exchange failed (${resp.status}):\n${txt}`);
  }

  return await resp.json();
}

async function handleCallback() {
  if (location.pathname !== "/callback") return;

  const qs = new URLSearchParams(location.search);
  const code = qs.get("code");
  const err = qs.get("error");

  if (err) {
    document.getElementById("out").textContent = "OAuth error: " + err;
    return;
  }
  if (!code) return;

  try {
    const tokens = await exchangeCodeForTokens(code);

    if (tokens.id_token) localStorage.setItem("id_token", tokens.id_token);
    if (tokens.access_token) localStorage.setItem("access_token", tokens.access_token);

    sessionStorage.removeItem("pkce_verifier");
    history.replaceState({}, "", "/");
  } catch (e) {
    document.getElementById("out").textContent = String(e);
  }
}

function logout() {
  const url =
    `${cfg.cognitoDomain}/logout` +
    `?client_id=${encodeURIComponent(cfg.clientId)}` +
    `&logout_uri=${encodeURIComponent(cfg.logoutUri)}`;

  localStorage.removeItem("id_token");
  localStorage.removeItem("access_token");
  location.assign(url);
}

function render() {
  const idt = localStorage.getItem("id_token");
  document.getElementById("login").style.display = idt ? "none" : "inline-block";
  document.getElementById("logout").style.display = idt ? "inline-block" : "none";
  document.getElementById("out").textContent = idt
    ? JSON.stringify(decodeJwt(idt), null, 2)
    : "Not logged in.";
}

document.getElementById("login").addEventListener("click", () => login());
document.getElementById("logout").addEventListener("click", logout);

handleCallback().then(render);
