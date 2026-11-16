(() => {
    let csrfToken;

    async function tokenCsrf() {
        if (csrfToken) return csrfToken;
        const response = await fetch("/api/auth/csrf", { credentials: "same-origin" });
        if (!response.ok) throw new Error("No fue posible preparar la solicitud segura.");
        const data = await response.json();
        csrfToken = data.token;
        return csrfToken;
    }

    async function api(url, options = {}) {
        const method = (options.method || "GET").toUpperCase();
        const headers = new Headers(options.headers || {});
        if (options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
        if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
            headers.set("X-XSRF-TOKEN", await tokenCsrf());
        }
        return fetch(url, { ...options, method, headers, credentials: "same-origin" });
    }

    async function responseData(response) {
        const text = await response.text();
        if (!text) return {};
        try { return JSON.parse(text); } catch { return { detail: text }; }
    }

    function show(id, message, error = false) {
        const target = document.getElementById(id);
        if (!target) return;
        target.textContent = message || "";
        target.className = message ? `notice ${error ? "error" : "success"}` : "notice";
        target.setAttribute("role", error ? "alert" : "status");
    }

    async function submitJson(form, url, method, outputId, onSuccess) {
        const button = form.querySelector("button[type=submit]");
        button.disabled = true;
        try {
            const body = Object.fromEntries(new FormData(form).entries());
            const response = await api(url, { method, body: JSON.stringify(body) });
            const data = await responseData(response);
            if (!response.ok) throw new Error(data.detail || data.message || "No fue posible completar la operación.");
            show(outputId, data.mensaje || "Cambios guardados correctamente.");
            if (onSuccess) await onSuccess(data);
        } catch (error) {
            show(outputId, error.message, true);
        } finally {
            button.disabled = false;
        }
    }

    window.App = { api, responseData, show, submitJson };
})();
