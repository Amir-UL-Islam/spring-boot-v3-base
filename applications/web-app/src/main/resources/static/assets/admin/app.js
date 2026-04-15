const state = {
  accessToken: localStorage.getItem("admin_access_token") || "",
  refreshToken: localStorage.getItem("admin_refresh_token") || "",
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
    throw new Error(typeof payload === "string" ? payload : JSON.stringify(payload));
  }
  return payload;
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
  const cards = [ui.matrixCard, ui.usersCard, ui.rolesCard, ui.privilegesCard, ui.urlsCard, ui.toolbar];
  cards.forEach((card) => card.classList.remove("hidden"));
}

function hideCards() {
  const cards = [ui.matrixCard, ui.usersCard, ui.rolesCard, ui.privilegesCard, ui.urlsCard, ui.toolbar];
  cards.forEach((card) => card.classList.add("hidden"));
}

function hasAdminConsoleAccess() {
  return ["user:read", "role:read", "privilege:read", "url:read"].some((permission) => permissions.can(permission));
}

async function login(event) {
  event.preventDefault();
  try {
    const body = {
      username: document.getElementById("username").value,
      password: document.getElementById("password").value,
      otp: document.getElementById("otp").value || null,
    };
    const response = await api("/authenticate", {
      method: "POST",
      body: JSON.stringify(body),
    });
    state.accessToken = response.accessToken;
    state.refreshToken = response.refreshToken;
    localStorage.setItem("admin_access_token", state.accessToken);
    localStorage.setItem("admin_refresh_token", state.refreshToken || "");
    renderAuthOutput("Login successful.");
    await refreshAll();
  } catch (error) {
    renderAuthOutput(`Login failed: ${error.message}`, true);
  }
}

async function loadMyPermissions() {
  state.me = await api("/api/me/permissions");
  document.getElementById("roles").innerHTML = `<strong>Roles:</strong> ${renderBadges(state.me.roles)}`;
  ui.identity.textContent = `Signed in as ${state.me.username}`;

  const matrixBody = document.querySelector("#matrix-table tbody");
  matrixBody.innerHTML = "";
  Object.entries(state.me.matrix || {}).forEach(([resource, actions]) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${resource}</td><td>${renderBadges(Object.keys(actions))}</td>`;
    matrixBody.appendChild(tr);
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
    <label>Username <input id="new-user-username"></label>
    <label>Password <input id="new-user-password" type="password"></label>
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
        const username = prompt("Username", user.username || "") || user.username;
        const password = prompt("New password (required by API)", "changeme123!") || "changeme123!";
        const roleIds = prompt("Role IDs (comma separated)", (user.role || []).join(",")) || "";
        await api(`/api/user/${user.id}`, {
          method: "PUT",
          body: JSON.stringify({ name, email, username, password, role: splitIds(roleIds) }),
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
          username: document.getElementById("new-user-username").value,
          password: document.getElementById("new-user-password").value,
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
      ui.usersCard.classList.add("hidden");
      ui.rolesCard.classList.add("hidden");
      ui.privilegesCard.classList.add("hidden");
      ui.urlsCard.classList.add("hidden");
      renderAuthOutput("Signed in with non-admin role. Matrix view only.");
      return;
    }
    await loadRoleValues();
    await loadPrivilegeValues();
    await loadUsers();
    await loadRoles();
    await loadPrivileges();
    await loadUrls();
    showCards();
  } catch (error) {
    renderAuthOutput(`Session error: ${error.message}`, true);
    hideCards();
  }
}

function logout() {
  state.accessToken = "";
  state.refreshToken = "";
  state.me = null;
  localStorage.removeItem("admin_access_token");
  localStorage.removeItem("admin_refresh_token");
  hideCards();
  renderAuthOutput("Signed out.");
}

document.getElementById("login-form").addEventListener("submit", login);
document.getElementById("refresh-btn").addEventListener("click", refreshAll);
document.getElementById("logout-btn").addEventListener("click", logout);

refreshAll();

