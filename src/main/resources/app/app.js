/*
 * MonitorHack — lógica do painel de monitoramento de ataques (SUT).
 *
 * Importante para os testes: todo o estado vive em memória (array `attacks`).
 * Ao recarregar a página, o estado volta ao conjunto de dados inicial (seed),
 * o que torna cada teste DETERMINÍSTICO e independente.
 */
(function () {
    "use strict";

    var CREDENTIALS = { user: "analista", pass: "monitor2026" };

    // ---- Conjunto de dados inicial (seed) — 12 eventos, contagens conhecidas ----
    // Tipo:  Força Bruta=3, SQL Injection=2, Port Scan=2, DDoS=2, XSS=2, Malware=1
    // Sever: Crítico=4, Alto=3, Médio=2, Baixo=3
    function seedData() {
        return [
            { id: 1,  time: "08:01:12", ip: "203.0.113.10", type: "Força Bruta",   severity: "Crítico", status: "ativo" },
            { id: 2,  time: "08:03:45", ip: "203.0.113.11", type: "SQL Injection", severity: "Crítico", status: "ativo" },
            { id: 3,  time: "08:05:02", ip: "198.51.100.5", type: "Port Scan",     severity: "Baixo",   status: "ativo" },
            { id: 4,  time: "08:07:31", ip: "203.0.113.12", type: "DDoS",          severity: "Alto",    status: "ativo" },
            { id: 5,  time: "08:09:18", ip: "203.0.113.13", type: "Força Bruta",   severity: "Alto",    status: "ativo" },
            { id: 6,  time: "08:11:54", ip: "198.51.100.7", type: "XSS",           severity: "Médio",   status: "ativo" },
            { id: 7,  time: "08:13:09", ip: "203.0.113.14", type: "Malware",       severity: "Crítico", status: "ativo" },
            { id: 8,  time: "08:15:40", ip: "198.51.100.8", type: "Port Scan",     severity: "Baixo",   status: "ativo" },
            { id: 9,  time: "08:17:22", ip: "203.0.113.15", type: "SQL Injection", severity: "Alto",    status: "ativo" },
            { id: 10, time: "08:19:03", ip: "203.0.113.16", type: "DDoS",          severity: "Crítico", status: "ativo" },
            { id: 11, time: "08:21:47", ip: "203.0.113.17", type: "Força Bruta",   severity: "Médio",   status: "ativo" },
            { id: 12, time: "08:23:11", ip: "198.51.100.9", type: "XSS",           severity: "Baixo",   status: "ativo" }
        ];
    }

    // Fila determinística para o botão "Simular ataque"
    function simQueue() {
        return [
            { time: "08:30:00", ip: "203.0.113.99", type: "Força Bruta",   severity: "Crítico", status: "ativo" },
            { time: "08:31:10", ip: "203.0.113.98", type: "DDoS",          severity: "Alto",    status: "ativo" },
            { time: "08:32:20", ip: "198.51.100.42", type: "SQL Injection", severity: "Crítico", status: "ativo" }
        ];
    }

    var attacks = [];
    var sim = [];
    var nextId = 100;
    var filters = { type: "", severity: "", ip: "" };

    // ---------- Elementos ----------
    var el = {};
    function cache() {
        el.loginView   = document.getElementById("login-view");
        el.dashView    = document.getElementById("dashboard-view");
        el.username    = document.getElementById("username");
        el.password    = document.getElementById("password");
        el.btnLogin    = document.getElementById("btn-login");
        el.loginError  = document.getElementById("login-error");
        el.btnLogout   = document.getElementById("btn-logout");
        el.filterType  = document.getElementById("filter-type");
        el.filterSev   = document.getElementById("filter-severity");
        el.searchIp    = document.getElementById("search-ip");
        el.btnClear    = document.getElementById("btn-clear");
        el.btnSimulate = document.getElementById("btn-simulate");
        el.tbody       = document.getElementById("attacks-body");
        el.notif       = document.getElementById("notification");
        el.empty       = document.getElementById("empty-state");
        el.statTotal   = document.getElementById("stat-total");
        el.statCrit    = document.getElementById("stat-critical");
        el.statActive  = document.getElementById("stat-active");
        el.statBlocked = document.getElementById("stat-blocked");
    }

    // ---------- Login ----------
    function doLogin() {
        var u = el.username.value.trim();
        var p = el.password.value;
        if (u === CREDENTIALS.user && p === CREDENTIALS.pass) {
            el.loginError.hidden = true;
            el.loginView.hidden = true;
            el.dashView.hidden = false;
            resetState();
        } else {
            el.loginError.hidden = false;
        }
    }

    function doLogout() {
        el.dashView.hidden = true;
        el.loginView.hidden = false;
        el.username.value = "";
        el.password.value = "";
    }

    // ---------- Estado ----------
    function resetState() {
        attacks = seedData();
        sim = simQueue();
        nextId = 100;
        filters = { type: "", severity: "", ip: "" };
        el.filterType.value = "";
        el.filterSev.value = "";
        el.searchIp.value = "";
        hideNotification();
        render();
    }

    function findById(id) {
        for (var i = 0; i < attacks.length; i++) {
            if (attacks[i].id === id) return attacks[i];
        }
        return null;
    }

    // ---------- Ações ----------
    function blockIp(id) {
        var a = findById(id);
        if (a && a.status !== "bloqueado") {
            a.status = "bloqueado";
            notify("IP " + a.ip + " bloqueado com sucesso.");
            render();
        }
    }

    function acknowledge(id) {
        var a = findById(id);
        if (a && a.status === "ativo") {
            a.status = "reconhecido";
            notify("Evento de " + a.ip + " marcado como reconhecido.");
            render();
        }
    }

    function simulateAttack() {
        var next;
        if (sim.length > 0) {
            next = sim.shift();
        } else {
            // Após esvaziar a fila, gera algo determinístico
            next = { time: "08:40:00", ip: "203.0.113.200", type: "Port Scan", severity: "Baixo", status: "ativo" };
        }
        next.id = nextId++;
        attacks.push(next);
        notify("Novo ataque detectado: " + next.type + " de " + next.ip + ".");
        render();
    }

    // ---------- Filtros ----------
    function applyFilters() {
        return attacks.filter(function (a) {
            if (filters.type && a.type !== filters.type) return false;
            if (filters.severity && a.severity !== filters.severity) return false;
            if (filters.ip && a.ip.indexOf(filters.ip) === -1) return false;
            return true;
        });
    }

    function onFilterChange() {
        filters.type = el.filterType.value;
        filters.severity = el.filterSev.value;
        filters.ip = el.searchIp.value.trim();
        render();
    }

    function clearFilters() {
        el.filterType.value = "";
        el.filterSev.value = "";
        el.searchIp.value = "";
        filters = { type: "", severity: "", ip: "" };
        render();
    }

    // ---------- Notificação ----------
    function notify(msg) {
        el.notif.textContent = msg;
        el.notif.hidden = false;
    }
    function hideNotification() {
        el.notif.hidden = true;
        el.notif.textContent = "";
    }

    // ---------- Render ----------
    function statusLabel(s) {
        if (s === "bloqueado") return "Bloqueado";
        if (s === "reconhecido") return "Reconhecido";
        return "Ativo";
    }

    function render() {
        // Estatísticas (sempre sobre o conjunto COMPLETO)
        el.statTotal.textContent   = attacks.length;
        el.statCrit.textContent    = attacks.filter(function (a) { return a.severity === "Crítico"; }).length;
        el.statActive.textContent  = attacks.filter(function (a) { return a.status === "ativo"; }).length;
        el.statBlocked.textContent = attacks.filter(function (a) { return a.status === "bloqueado"; }).length;

        // Linhas (sobre o conjunto FILTRADO)
        var rows = applyFilters();
        el.tbody.innerHTML = "";
        el.empty.hidden = rows.length !== 0;

        rows.forEach(function (a) {
            var tr = document.createElement("tr");
            tr.className = "attack-row";
            tr.setAttribute("data-id", a.id);
            tr.setAttribute("data-ip", a.ip);
            tr.setAttribute("data-type", a.type);
            tr.setAttribute("data-severity", a.severity);
            tr.setAttribute("data-status", a.status);

            tr.innerHTML =
                '<td>' + a.time + '</td>' +
                '<td class="ip-cell">' + a.ip + '</td>' +
                '<td>' + a.type + '</td>' +
                '<td><span class="badge sev-' + a.severity + '">' + a.severity + '</span></td>' +
                '<td class="status-cell status-' + a.status + '">' + statusLabel(a.status) + '</td>' +
                '<td class="row-actions">' +
                    '<button class="btn-block" data-id="' + a.id + '"' + (a.status === "bloqueado" ? " disabled" : "") + '>Bloquear</button>' +
                    '<button class="btn-ack" data-id="' + a.id + '"' + (a.status !== "ativo" ? " disabled" : "") + '>Reconhecer</button>' +
                '</td>';

            el.tbody.appendChild(tr);
        });
    }

    // ---------- Eventos ----------
    function bind() {
        el.btnLogin.addEventListener("click", doLogin);
        el.password.addEventListener("keydown", function (e) { if (e.key === "Enter") doLogin(); });
        el.btnLogout.addEventListener("click", doLogout);

        el.filterType.addEventListener("change", onFilterChange);
        el.filterSev.addEventListener("change", onFilterChange);
        el.searchIp.addEventListener("input", onFilterChange);
        el.btnClear.addEventListener("click", clearFilters);
        el.btnSimulate.addEventListener("click", simulateAttack);

        // Delegação de eventos para os botões das linhas
        el.tbody.addEventListener("click", function (e) {
            var t = e.target;
            if (t.classList.contains("btn-block")) {
                blockIp(parseInt(t.getAttribute("data-id"), 10));
            } else if (t.classList.contains("btn-ack")) {
                acknowledge(parseInt(t.getAttribute("data-id"), 10));
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        cache();
        bind();
    });
})();
