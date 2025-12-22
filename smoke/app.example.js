const cfg = {
  cognitoDomain: "__HOSTED_UI__",
  clientId: "__CLIENT_ID__",
  redirectUri: "https://sso-poc.rpgsecrets.net/callback",
  logoutUri: "https://sso-poc.rpgsecrets.net/",
  scopes: "openid+email+profile"
};

function login() {
  const url = `${cfg.cognitoDomain}/oauth2/authorize?client_id=${cfg.clientId}&response_type=token&scope=${cfg.scopes}&redirect_uri=${encodeURIComponent(cfg.redirectUri)}`;
  location.assign(url);
}

function logout() {
  const url = `${cfg.cognitoDomain}/logout?client_id=${cfg.clientId}&logout_uri=${encodeURIComponent(cfg.logoutUri)}`;
  localStorage.removeItem("id_token");
  localStorage.removeItem("access_token");
  location.assign(url);
}

function decodeJwt(token) {
  const payload = token.split(".")[1];
  const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
  return JSON.parse(json);
}

function handleCallback() {
  if (location.pathname === "/callback" && location.hash) {
    const params = new URLSearchParams(location.hash.substring(1));
    const idt = params.get("id_token");
    const act = params.get("access_token");
    if (idt) localStorage.setItem("id_token", idt);
    if (act) localStorage.setItem("access_token", act);
    history.replaceState({}, "", "/");
  }
}

function render() {
  const idt = localStorage.getItem("id_token");
  document.getElementById("login").style.display = idt ? "none" : "inline-block";
  document.getElementById("logout").style.display = idt ? "inline-block" : "none";
  document.getElementById("out").textContent = idt
    ? JSON.stringify(decodeJwt(idt), null, 2)
    : "Not logged in.";
}

document.getElementById("login").addEventListener("click", login);
document.getElementById("logout").addEventListener("click", logout);

handleCallback();
render();
