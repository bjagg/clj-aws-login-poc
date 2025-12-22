// Copy this file to config.js (and keep config.js out of git).
window.__APP_CONFIG__ = {
  cognitoDomain: "__HOSTED_UI__", // e.g., https://<prefix>.auth.us-east-1.amazoncognito.com
  clientId: "__CLIENT_ID__",      // Cognito app client id
  redirectUri: "https://sso-poc.rpgsecrets.net/callback",
  logoutUri: "https://sso-poc.rpgsecrets.net/",
  scopes: "openid email profile"
};
