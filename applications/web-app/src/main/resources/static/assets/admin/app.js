const state = {
  accessToken: localStorage.getItem("admin_access_token") || "",
  refreshToken: localStorage.getItem("admin_refresh_token") || "",
  pendingChallengeId: "",
  pendingChallengeChannel: "",
  me: null,
  rolesMap: {},
  privilegeMap: {},
};

const permissions = {
  can(code) {
    return !!state.me?.authorities?.includes(code);
  },
};

const ui = {
  authOutput: document.getElementById("auth-output"),
  toolbar: document.getElementById("toolbar"),
  identity: document.getElementById("identity"),
  matrixCard: document.getElementById("matrix-card"),
  driftCard: document.getElementById("drift-card"),
  policyCard: document.getElementById("policy-card"),
  usersCard: document.getElementById("users-card"),
  rolesCard: document.getElementById("roles-card"),
  privilegesCard: document.getElementById("privileges-card"),
  urlsCard: document.getElementById("urls-card"),
};

function authHeaders() {
  return state.accessToken
    ? { Authorization: `Bearer ${state.accessToken}` }
    : {};
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...(options.headers || {}),
    },
    ...options,
  });
  const text = await response.text();
  let payload = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch (_err) {
    payload = text;
  }
  if (!response.ok) {
    const message = typeof payload === "string" ? payload : JSON.stringify(payload);
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return payload;
}

function parseErrorMessage(error) {
  if (!error?.message) return "Unknown error";
  try {
    const parsed = JSON.parse(error.message);
    return parsed?.message || parsed?.error || error.message;
  } catch (_ignored) {
    return error.message;
  }
}

function renderAuthOutput(message, isError = false) {
  ui.authOutput.textContent = message;
  ui.authOutput.style.color = isError ? "#ff7f7f" : "#c4f1be";
}

function parsePage(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.content)) return data.content;
  return [];
}

function renderBadges(values) {
  return (values || []).map((value) => `<span class="badge">${value}</span>`).join("");
}

function splitIds(value) {
  if (!value) return [];
  return value
    .split(",")
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part) => Number(part));
}

function showCards() {
  const cards = [ui.matrixCard, ui.usersCard, ui.rolesCard, ui.privilegesCard, ui.urlsCard, ui.driftCard, ui.policyCard, ui.toolbar];
  cards.forEach((card) => card.classList.remove("hidden"));
}

function hideCards() {
  const cards = [ui.matrixCard, ui.usersCard, ui.rolesCard, ui.privilegesCard, ui.urlsCard, ui.driftCard, ui.policyCard, ui.toolbar];
  cards.forEach((card) => card.classList.add("hidden"));
}

function hasAdminConsoleAccess() {
  return ["user:read", "role:read", "privilege:read", "url:read", "policy:read"].some((permission) => permissions.can(permission));
}

function hasHardAdminAccess() {
  return permissions.can("matrix:manage") || permissions.can("module:manage") || permissions.can("policy:manage");
}

async function login(event) {
  event.preventDefault();
  try {
    const body = {
      username: document.getElementById("username").value,
      password: document.getElementById("password").value,
      otp: document.getElementById("otp").value || null,
      otpChannel: document.getElementById("otp-channel").value || null,
      challengeId: state.pendingChallengeId || null,
    };
    const response = await api("/authenticate", {
      method: "POST",
      body: JSON.stringify(body),
    });
    if (response.requiresMfa) {
      state.pendingChallengeId = response.mfaChallengeId || "";
      state.pendingChallengeChannel = response.mfaChannel || "";
      const hint = document.getElementById("mfa-challenge-hint");
      hint.textContent = `MFA challenge required via ${response.mfaChannel || "selected channel"}. Enter OTP and submit again.`;
      renderAuthOutput(response.mfaMessage || "MFA challenge issued.");
      return;
    }

    state.accessToken = response.accessToken;
    state.refreshToken = response.refreshToken;
    state.pendingChallengeId = "";
    state.pendingChallengeChannel = "";
    document.getElementById("mfa-challenge-hint").textContent = "";
    localStorage.setItem("admin_access_token", state.accessToken);
    localStorage.setItem("admin_refresh_token", state.refreshToken || "");
    renderAuthOutput("Login successful.");
    await refreshAll();
  } catch (error) {
    renderAuthOutput(`Login failed: ${parseErrorMessage(error)}`, true);
  }
}

async function loadPolicySettings() {
  if (!permissions.can("policy:read")) {
    ui.policyCard.classList.add("hidden");
    return;
  }
  ui.policyCard.classList.remove("hidden");

  const policy = await api("/api/security/policy");
  const form = document.getElementById("policy-form");

  const editable = permissions.can("policy:manage");
  form.innerHTML = `
    <label>Registration Allowed Channels (CSV)
      <input id="policy-registration-allowed" value="${policy["registration.allowed.channels"] || "SMS,EMAIL"}" ${editable ? "" : "disabled"}>
    </label>
    <label>Registration Default Channel
      <input id="policy-registration-default" value="${policy["registration.default.channel"] || "EMAIL"}" ${editable ? "" : "disabled"}>
    </label>
    <label>MFA Optional Per User
      <input id="policy-mfa-optional" value="${policy["mfa.optional.per.user"] || "true"}" ${editable ? "" : "disabled"}>
    </label>
    <label>MFA Enforced Roles (CSV)
      <input id="policy-mfa-roles" value="${policy["mfa.enforced.roles"] || ""}" ${editable ? "" : "disabled"}>
    </label>
    <label>MFA Allowed Factors USER
      <input id="policy-factors-user" value="${policy["mfa.allowed.factors.user"] || "TOTP,SMS,EMAIL"}" ${editable ? "" : "disabled"}>
    </label>
    <label>MFA Allowed Factors ADMIN
      <input id="policy-factors-admin" value="${policy["mfa.allowed.factors.admin"] || "TOTP,EMAIL"}" ${editable ? "" : "disabled"}>
    </label>
    <label>MFA Allowed Factors SUPER_ADMIN
      <input id="policy-factors-super-admin" value="${policy["mfa.allowed.factors.super_admin"] || "TOTP,EMAIL"}" ${editable ? "" : "disabled"}>
    </label>
    ${editable ? '<button type="button" id="save-policy-btn">Save Policy</button>' : '<p class="hint">Missing policy:manage permission.</p>'}
  `;

  const saveBtn = document.getElementById("save-policy-btn");
  if (saveBtn) {
    saveBtn.onclick = async () => {
      const payload = [
        { key: "registration.allowed.channels", value: document.getElementById("policy-registration-allowed").value },
        { key: "registration.default.channel", value: document.getElementById("policy-registration-default").value },
        { key: "mfa.optional.per.user", value: document.getElementById("policy-mfa-optional").value },
        { key: "mfa.enforced.roles", value: document.getElementById("policy-mfa-roles").value },
        { key: "mfa.allowed.factors.user", value: document.getElementById("policy-factors-user").value },
        { key: "mfa.allowed.factors.admin", value: document.getElementById("policy-factors-admin").value },
        { key: "mfa.allowed.factors.super_admin", value: document.getElementById("policy-factors-super-admin").value },
      ];
      await api("/api/security/policy", {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      renderAuthOutput("Policy updated.");
      await loadPolicySettings();
    };
  }
}

async function loadMyPermissions() {
  state.me = await api("/api/me/permissions");
  document.getElementById("roles").innerHTML = `<strong>Roles:</strong> ${renderBadges(state.me.roles)}`;
  ui.identity.textContent = `Signed in as ${state.me.username} (${(state.me.roles || []).join(", ") || "no-role"})`;

  const matrixBody = document.querySelector("#matrix-table tbody");
  matrixBody.innerHTML = "";
  Object.entries(state.me.matrix || {}).forEach(([resource, actions]) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${resource}</td><td>${renderBadges(Object.keys(actions))}</td>`;
    matrixBody.appendChild(tr);
  });
}

async function loadPolicyDrift() {
  if (!permissions.can("matrix:manage")) {
    ui.driftCard.classList.add("hidden");
    return;
  }
  ui.driftCard.classList.remove("hidden");

  let report = null;
  try {
    report = await api("/api/policy/drift");
  } catch (error) {
    if (error.status === 403) {
      ui.driftCard.classList.add("hidden");
      return;
    }
    throw error;
  }
  const summary = document.getElementById("drift-summary");
  summary.innerHTML = `<strong>Generated:</strong> ${report.generatedAt || "n/a"} | <strong>Issues:</strong> ${report.issueCount || 0}`;

  const tbody = document.querySelector("#drift-table tbody");
  tbody.innerHTML = "";
  (report.issues || []).forEach((issue) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><span class="badge ${issue.severity === "ERROR" ? "badge-danger" : "badge-warn"}">${issue.severity || "WARN"}</span></td>
      <td>${issue.type || ""}</td>
      <td>${issue.method || ""}</td>
      <td>${issue.endpoint || ""}</td>
      <td>${issue.authority || ""}</td>
      <td>${issue.details || ""}</td>
    `;
    tbody.appendChild(tr);
  });
}

function renderForm(containerId, html) {
  document.getElementById(containerId).innerHTML = html;
}

async function loadRoleValues() {
  if (!permissions.can("role:read")) {
    state.rolesMap = {};
    return;
  }
  state.rolesMap = await api("/api/user/roleValues");
}

async function loadPrivilegeValues() {
  if (!permissions.can("privilege:read")) {
    state.privilegeMap = {};
    return;
  }
  state.privilegeMap = await api("/api/url/privilegeValues");
}

async function loadUsers() {
  if (!permissions.can("user:read")) {
    ui.usersCard.classList.add("hidden");
    return;
  }
  ui.usersCard.classList.remove("hidden");

  renderForm("create-user-form", permissions.can("user:create") ? `
    <label>Name <input id="new-user-name"></label>
    <label>Email <input id="new-user-email"></label>
    <label>Phone <input id="new-user-phone"></label>
    <label>Username <input id="new-user-username"></label>
    <label>Password <input id="new-user-password" type="password"></label>
    <label>Email Verified <input id="new-user-email-verified" type="checkbox"></label>
    <label>Phone Verified <input id="new-user-phone-verified" type="checkbox"></label>
    <label>SMS MFA Enabled <input id="new-user-sms-mfa" type="checkbox"></label>
    <label>Email MFA Enabled <input id="new-user-email-mfa" type="checkbox"></label>
    <label>Preferred MFA <input id="new-user-preferred-mfa" placeholder="TOTP/SMS/EMAIL"></label>
    <label>Role IDs (comma separated) <input id="new-user-role-ids" placeholder="10001,10002"></label>
    <button type="button" id="create-user-btn">Create User</button>
  ` : "<p class='hint'>Missing user:create permission.</p>");

  const users = parsePage(await api("/api/user"));
  const tbody = document.querySelector("#users-table tbody");
  tbody.innerHTML = "";

  users.forEach((user) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${user.id}</td>
      <td>${user.username || ""}</td>
      <td>${user.name || ""}</td>
      <td>${user.email || ""}</td>
      <td>${user.phone || ""}</td>
      <td>${renderBadges([
        user.twoFactorEnabled ? "TOTP" : "",
        user.smsMfaEnabled ? "SMS" : "",
        user.emailMfaEnabled ? "EMAIL" : "",
      ].filter(Boolean))}</td>
      <td>${renderBadges((user.role || []).map((roleId) => state.rolesMap[roleId] || roleId))}</td>
      <td class="actions"></td>
    `;
    const actions = tr.querySelector(".actions");

    if (permissions.can("user:update")) {
      const btn = document.createElement("button");
      btn.className = "secondary";
      btn.textContent = "Edit";
      btn.onclick = async () => {
        const name = prompt("Name", user.name || "") || user.name;
        const email = prompt("Email", user.email || "") || user.email;
        const phone = prompt("Phone", user.phone || "") || user.phone;
        const username = prompt("Username", user.username || "") || user.username;
        const password = prompt("New password (required by API)", "changeme123!") || "changeme123!";
        const roleIds = prompt("Role IDs (comma separated)", (user.role || []).join(",")) || "";
        const smsMfaEnabled = confirm("Enable SMS MFA?");
        const emailMfaEnabled = confirm("Enable EMAIL MFA?");
        const twoFactorEnabled = confirm("Enable TOTP MFA?");
        const preferredMfaFactor = prompt("Preferred MFA (TOTP/SMS/EMAIL)", user.preferredMfaFactor || "") || user.preferredMfaFactor;
        await api(`/api/user/${user.id}`, {
          method: "PUT",
          body: JSON.stringify({
            name,
            email,
            phone,
            username,
            password,
            role: splitIds(roleIds),
            twoFactorEnabled,
            smsMfaEnabled,
            emailMfaEnabled,
            preferredMfaFactor,
          }),
        });
        await loadUsers();
      };
      actions.appendChild(btn);
    }

    if (permissions.can("user:delete")) {
      const btn = document.createElement("button");
      btn.className = "danger";
      btn.textContent = "Delete";
      btn.onclick = async () => {
        if (confirm(`Delete user ${user.username}?`)) {
          await api(`/api/user/${user.id}`, { method: "DELETE" });
          await loadUsers();
        }
      };
      actions.appendChild(btn);
    }

    tbody.appendChild(tr);
  });

  const createBtn = document.getElementById("create-user-btn");
  if (createBtn) {
    createBtn.onclick = async () => {
      await api("/api/user", {
        method: "POST",
        body: JSON.stringify({
          name: document.getElementById("new-user-name").value,
          email: document.getElementById("new-user-email").value,
          phone: document.getElementById("new-user-phone").value,
          username: document.getElementById("new-user-username").value,
          password: document.getElementById("new-user-password").value,
          emailVerified: document.getElementById("new-user-email-verified").checked,
          phoneVerified: document.getElementById("new-user-phone-verified").checked,
          smsMfaEnabled: document.getElementById("new-user-sms-mfa").checked,
          emailMfaEnabled: document.getElementById("new-user-email-mfa").checked,
          preferredMfaFactor: document.getElementById("new-user-preferred-mfa").value,
          role: splitIds(document.getElementById("new-user-role-ids").value),
        }),
      });
      await loadUsers();
    };
  }
}

async function loadRoles() {
  if (!permissions.can("role:read")) {
    ui.rolesCard.classList.add("hidden");
    return;
  }
  ui.rolesCard.classList.remove("hidden");

  renderForm("create-role-form", permissions.can("role:create") ? `
    <label>Name <input id="new-role-name"></label>
    <label>Description <input id="new-role-description"></label>
    <label>Privilege IDs (comma separated) <input id="new-role-privileges" placeholder="10010,10011"></label>
    <button type="button" id="create-role-btn">Create Role</button>
  ` : "<p class='hint'>Missing role:create permission.</p>");

  const roles = parsePage(await api("/api/roles"));
  const tbody = document.querySelector("#roles-table tbody");
  tbody.innerHTML = "";

  roles.forEach((role) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${role.id}</td>
      <td>${role.name}</td>
      <td>${role.description}</td>
      <td>${renderBadges((role.privilege || []).map((id) => state.privilegeMap[id] || id))}</td>
      <td class="actions"></td>
    `;

    const actions = tr.querySelector(".actions");
    if (permissions.can("role:update")) {
      const btn = document.createElement("button");
      btn.className = "secondary";
      btn.textContent = "Edit";
      btn.onclick = async () => {
        const name = prompt("Name", role.name) || role.name;
        const description = prompt("Description", role.description) || role.description;
        const privilege = prompt("Privilege IDs (comma separated)", (role.privilege || []).join(",")) || "";
        await api(`/api/roles/${role.id}`, {
          method: "PUT",
          body: JSON.stringify({ name, description, privilege: splitIds(privilege) }),
        });
        await loadRoles();
      };
      actions.appendChild(btn);
    }

    if (permissions.can("role:delete")) {
      const btn = document.createElement("button");
      btn.className = "danger";
      btn.textContent = "Delete";
      btn.onclick = async () => {
        if (confirm(`Delete role ${role.name}?`)) {
          await api(`/api/roles/${role.id}`, { method: "DELETE" });
          await loadRoles();
        }
      };
      actions.appendChild(btn);
    }

    tbody.appendChild(tr);
  });

  const createBtn = document.getElementById("create-role-btn");
  if (createBtn) {
    createBtn.onclick = async () => {
      await api("/api/roles", {
        method: "POST",
        body: JSON.stringify({
          name: document.getElementById("new-role-name").value,
          description: document.getElementById("new-role-description").value,
          privilege: splitIds(document.getElementById("new-role-privileges").value),
        }),
      });
      await loadRoles();
    };
  }
}

async function loadPrivileges() {
  if (!permissions.can("privilege:read")) {
    ui.privilegesCard.classList.add("hidden");
    return;
  }
  ui.privilegesCard.classList.remove("hidden");

  renderForm("create-privilege-form", permissions.can("privilege:create") ? `
    <label>Privilege Code <input id="new-priv-name" placeholder="user:export"></label>
    <button type="button" id="create-priv-btn">Create Privilege</button>
  ` : "<p class='hint'>Missing privilege:create permission.</p>");

  const privileges = await api("/api/privileges");
  const tbody = document.querySelector("#privileges-table tbody");
  tbody.innerHTML = "";

  privileges.forEach((privilege) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${privilege.id}</td><td>${privilege.name}</td><td class="actions"></td>`;
    const actions = tr.querySelector(".actions");

    if (permissions.can("privilege:update")) {
      const btn = document.createElement("button");
      btn.className = "secondary";
      btn.textContent = "Edit";
      btn.onclick = async () => {
        const name = prompt("Privilege code", privilege.name) || privilege.name;
        await api(`/api/privileges/${privilege.id}`, {
          method: "PUT",
          body: JSON.stringify({ name }),
        });
        await loadPrivileges();
      };
      actions.appendChild(btn);
    }

    if (permissions.can("privilege:delete")) {
      const btn = document.createElement("button");
      btn.className = "danger";
      btn.textContent = "Delete";
      btn.onclick = async () => {
        if (confirm(`Delete privilege ${privilege.name}?`)) {
          await api(`/api/privileges/${privilege.id}`, { method: "DELETE" });
          await loadPrivileges();
        }
      };
      actions.appendChild(btn);
    }

    tbody.appendChild(tr);
  });

  const createBtn = document.getElementById("create-priv-btn");
  if (createBtn) {
    createBtn.onclick = async () => {
      await api("/api/privileges", {
        method: "POST",
        body: JSON.stringify({ name: document.getElementById("new-priv-name").value }),
      });
      await loadPrivileges();
    };
  }
}

async function loadUrls() {
  if (!permissions.can("url:read")) {
    ui.urlsCard.classList.add("hidden");
    return;
  }
  ui.urlsCard.classList.remove("hidden");

  renderForm("create-url-form", permissions.can("url:create") ? `
    <label>Endpoint <input id="new-url-endpoint" placeholder="/api/custom/report"></label>
    <label>Method <input id="new-url-method" placeholder="GET"></label>
    <label>Privilege IDs (comma separated) <input id="new-url-privileges" placeholder="10010,10011"></label>
    <button type="button" id="create-url-btn">Create URL</button>
  ` : "<p class='hint'>Missing url:create permission.</p>");

  const urls = parsePage(await api("/api/url"));
  const tbody = document.querySelector("#urls-table tbody");
  tbody.innerHTML = "";

  urls.forEach((url) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${url.id}</td>
      <td>${url.endpoint}</td>
      <td>${url.method}</td>
      <td>${renderBadges((url.privileges || []).map((id) => state.privilegeMap[id] || id))}</td>
      <td class="actions"></td>
    `;

    const actions = tr.querySelector(".actions");
    if (permissions.can("url:update")) {
      const btn = document.createElement("button");
      btn.className = "secondary";
      btn.textContent = "Edit";
      btn.onclick = async () => {
        const endpoint = prompt("Endpoint", url.endpoint) || url.endpoint;
        const method = prompt("Method", url.method) || url.method;
        const privileges = prompt("Privilege IDs (comma separated)", (url.privileges || []).join(",")) || "";
        await api(`/api/url/${url.id}`, {
          method: "PUT",
          body: JSON.stringify({ endpoint, method, privileges: splitIds(privileges) }),
        });
        await loadUrls();
      };
      actions.appendChild(btn);
    }

    if (permissions.can("url:delete")) {
      const btn = document.createElement("button");
      btn.className = "danger";
      btn.textContent = "Delete";
      btn.onclick = async () => {
        if (confirm(`Delete URL ${url.endpoint} ${url.method}?`)) {
          await api(`/api/url/${url.id}`, { method: "DELETE" });
          await loadUrls();
        }
      };
      actions.appendChild(btn);
    }

    tbody.appendChild(tr);
  });

  const createBtn = document.getElementById("create-url-btn");
  if (createBtn) {
    createBtn.onclick = async () => {
      await api("/api/url", {
        method: "POST",
        body: JSON.stringify({
          endpoint: document.getElementById("new-url-endpoint").value,
          method: document.getElementById("new-url-method").value,
          privileges: splitIds(document.getElementById("new-url-privileges").value),
        }),
      });
      await loadUrls();
    };
  }
}

async function refreshAll() {
  if (!state.accessToken) {
    hideCards();
    return;
  }
  try {
    await loadMyPermissions();
    if (!hasAdminConsoleAccess()) {
      ui.matrixCard.classList.remove("hidden");
      ui.toolbar.classList.remove("hidden");
      ui.driftCard.classList.add("hidden");
      ui.usersCard.classList.add("hidden");
      ui.rolesCard.classList.add("hidden");
      ui.privilegesCard.classList.add("hidden");
      ui.urlsCard.classList.add("hidden");
      ui.policyCard.classList.add("hidden");
      renderAuthOutput("Signed in with non-admin role. Matrix view only.");
      return;
    }

    const issues = [];
    const loadGuarded = async (title, fn) => {
      try {
        await fn();
      } catch (error) {
        if (error.status === 403) {
          issues.push(`${title}: forbidden by current policy`);
          return;
        }
        throw error;
      }
    };

    await loadGuarded("Role values", loadRoleValues);
    await loadGuarded("Privilege values", loadPrivilegeValues);
    await loadGuarded("Users", loadUsers);
    await loadGuarded("Roles", loadRoles);
    await loadGuarded("Privileges", loadPrivileges);
    await loadGuarded("URL policies", loadUrls);
    await loadGuarded("Security policy", loadPolicySettings);
    await loadGuarded("Policy drift", loadPolicyDrift);

    showCards();
    const modeLabel = hasHardAdminAccess() ? "policy-admin" : "admin";
    if (issues.length > 0) {
      renderAuthOutput(`Signed in (${modeLabel}) with partial access. ${issues.join(" | ")}`);
    } else {
      renderAuthOutput(`Signed in (${modeLabel}).`);
    }
  } catch (error) {
    const message = parseErrorMessage(error);
    if (error.status === 401) {
      logout();
      renderAuthOutput("Session expired. Please sign in again.", true);
      return;
    }
    renderAuthOutput(`Session error: ${message}`, true);
    hideCards();
  }
}

function logout() {
  state.accessToken = "";
  state.refreshToken = "";
  state.pendingChallengeId = "";
  state.pendingChallengeChannel = "";
  state.me = null;
  localStorage.removeItem("admin_access_token");
  localStorage.removeItem("admin_refresh_token");
  document.getElementById("mfa-challenge-hint").textContent = "";
  hideCards();
  renderAuthOutput("Signed out.");
}

document.getElementById("login-form").addEventListener("submit", login);
document.getElementById("refresh-btn").addEventListener("click", refreshAll);
document.getElementById("logout-btn").addEventListener("click", logout);

refreshAll();

