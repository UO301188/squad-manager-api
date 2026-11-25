

/**
 * Lógica de presentación, estado y renderizado de SquadManager Pro
 */
const state = {
    currentView: 'dashboard',
    players: [],
    selectedPosition: '',
    selectedStatus: '',
    searchQuery: '',
    currentMatchId: null,
    currentTrainingId: null,
    analyticsData: [],
    charts: {
        contributions: null,
        status: null
    },

    currentMatchData: null,
    tacticalFormation: '4-3-3',
    tacticalSlots: {},
    selectedPitchSlot: 1
};

// Diccionarios de traducción y estilos visuales
const POSITION_META = {
    GOALKEEPER: { label: 'Portero', short: 'POR', badge: 'bg-amber-500/15 text-amber-400 border-amber-500/30' },
    DEFENDER:   { label: 'Defensa', short: 'DEF', badge: 'bg-blue-500/15 text-blue-400 border-blue-500/30' },
    MIDFIELDER: { label: 'Centrocampista', short: 'MED', badge: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' },
    FORWARD:    { label: 'Delantero', short: 'DEL', badge: 'bg-rose-500/15 text-rose-400 border-rose-500/30' }
};

const STATUS_META = {
    AVAILABLE:   { label: 'Disponible', badge: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' },
    INJURED:     { label: 'Lesionado', badge: 'bg-rose-500/15 text-rose-400 border-rose-500/30' },
    SUSPENDED:   { label: 'Sancionado', badge: 'bg-amber-500/15 text-amber-400 border-amber-500/30' },
    UNAVAILABLE: { label: 'No disponible', badge: 'bg-slate-500/15 text-slate-400 border-slate-500/30' }
};

const CALLUP_META = {
    STARTER:    { label: 'Titular', badge: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' },
    SUBSTITUTE: { label: 'Suplente', badge: 'bg-blue-500/15 text-blue-400 border-blue-500/30' },
    NOT_CALLED: { label: 'No convocado', badge: 'bg-slate-500/15 text-slate-400 border-slate-500/30' }
};

const ATTENDANCE_META = {
    PRESENT:            { label: 'Presente', badge: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' },
    ABSENT_JUSTIFIED:   { label: 'Aus. Justificada', badge: 'bg-amber-500/15 text-amber-400 border-amber-500/30' },
    ABSENT_UNJUSTIFIED: { label: 'Aus. Injustificada', badge: 'bg-rose-500/15 text-rose-400 border-rose-500/30' },
    INJURED_RECOVERY:   { label: 'Readaptación', badge: 'bg-purple-500/15 text-purple-400 border-purple-500/30' }
};

// ==================== INICIALIZACIÓN ====================
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    setupEventListeners();
    document.getElementById('form-auth').addEventListener('submit', handleAuthSubmit);
    lucide.createIcons();
    checkSession();
});

function checkSession() {
    const token = localStorage.getItem('squad_token');
    const userRaw = localStorage.getItem('squad_user');
    if (!token || !userRaw) {
        document.getElementById('auth-screen').classList.remove('hidden');
        document.getElementById('app-shell').classList.add('hidden');
        return;
    }
    const user = JSON.parse(userRaw);
    document.getElementById('sidebar-club-name').textContent = user.clubName;
    document.getElementById('sidebar-user-email').textContent = user.email;
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-shell').classList.remove('hidden');
    switchView('dashboard');
}

function toggleAuthMode(mode) {
    document.getElementById('auth-mode').value = mode;
    const isReg = mode === 'register';
    document.getElementById('auth-club-group').classList.toggle('hidden', !isReg);
    document.getElementById('auth-clubName').required = isReg;
    document.getElementById('auth-submit-btn').textContent = isReg ? 'Crear Cuenta y Entrar' : 'Entrar al Panel';

    document.getElementById('tab-login').className = !isReg
        ? 'py-2 text-xs font-bold rounded-lg bg-emerald-500/20 text-emerald-400'
        : 'py-2 text-xs font-bold rounded-lg text-slate-400 hover:text-white';
    document.getElementById('tab-register').className = isReg
        ? 'py-2 text-xs font-bold rounded-lg bg-emerald-500/20 text-emerald-400'
        : 'py-2 text-xs font-bold rounded-lg text-slate-400 hover:text-white';
}

async function handleAuthSubmit(e) {
    e.preventDefault();
    const mode = document.getElementById('auth-mode').value;
    const email = document.getElementById('auth-email').value.trim();
    const password = document.getElementById('auth-password').value;

    try {
        const res = mode === 'register'
            ? await api.register({ clubName: document.getElementById('auth-clubName').value.trim(), email, password })
            : await api.login({ email, password });

        localStorage.setItem('squad_token', res.token);
        localStorage.setItem('squad_user', JSON.stringify({ email: res.email, clubName: res.clubName }));
        showToast(`Bienvenido, ${res.clubName}`, 'success');
        checkSession();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loginWithDemo() {
    try {
        const res = await api.login({ email: 'demo@squadmanager.com', password: 'demo1234' });
        localStorage.setItem('squad_token', res.token);
        localStorage.setItem('squad_user', JSON.stringify({ email: res.email, clubName: res.clubName }));
        showToast('Sesión iniciada en modo Demo', 'success');
        checkSession();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function logout() {
    localStorage.removeItem('squad_token');
    localStorage.removeItem('squad_user');
    checkSession();
}

function setupNavigation() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            switchView(btn.dataset.view);
        });
    });
}

function switchView(view) {
    state.currentView = view;

    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.view === view);
    });

    ['dashboard', 'players', 'matches', 'trainings', 'analytics'].forEach(v => {
        document.getElementById(`view-${v}`).classList.toggle('hidden', v !== view);
    });

    const titleEl = document.getElementById('page-title');
    const subtitleEl = document.getElementById('page-subtitle');
    const actionsEl = document.getElementById('header-actions');

    const headers = {
        dashboard: {
            title: 'Dashboard General',
            sub: 'Visión global del estado físico, competitivo y carga de trabajo del equipo',
            action: `<button onclick="loadDashboard()" class="btn-secondary"><i data-lucide="refresh-cw" class="w-4 h-4"></i> Actualizar</button>`
        },
        players: {
            title: 'Gestión de Plantilla',
            sub: 'Altas, bajas, dorsales y control de disponibilidad médica o disciplinaria',
            action: `<button onclick="openPlayerModal()" class="btn-primary"><i data-lucide="user-plus" class="w-4 h-4"></i> Nuevo Jugador</button>`
        },
        matches: {
            title: 'Partidos, Convocatorias y Actas',
            sub: 'Planificación del calendario, selección de titulares y registro de estadísticas oficiales',
            action: `<button onclick="openModal('modal-match')" class="btn-primary"><i data-lucide="plus-circle" class="w-4 h-4"></i> Programar Partido</button>`
        },
        trainings: {
            title: 'Sesiones de Entrenamiento',
            sub: 'Control de asistencia diaria, motivos de ausencia y esfuerzo percibido (RPE)',
            action: `<button onclick="openModal('modal-training')" class="btn-primary"><i data-lucide="plus-circle" class="w-4 h-4"></i> Nueva Sesión</button>`
        },
        analytics: {
            title: 'Analítica y Rendimiento',
            sub: 'Métricas agregadas en SQL: minutos, goles/90, compromiso en entrenos y carga física',
            action: `<button onclick="loadAnalytics()" class="btn-secondary"><i data-lucide="refresh-cw" class="w-4 h-4"></i> Recalcular</button>`
        }
    };

    titleEl.textContent = headers[view].title;
    subtitleEl.textContent = headers[view].sub;
    actionsEl.innerHTML = headers[view].action;
    lucide.createIcons();

    if (view === 'dashboard') loadDashboard();
    if (view === 'players') loadPlayers();
    if (view === 'matches') loadMatches();
    if (view === 'trainings') loadTrainings();
    if (view === 'analytics') loadAnalytics();
}

function setupEventListeners() {
    // Filtros plantilla
    document.querySelectorAll('#position-filters .filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('#position-filters .filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            state.selectedPosition = chip.dataset.pos;
            loadPlayers();
        });
    });

    document.getElementById('filter-player-status').addEventListener('change', (e) => {
        state.selectedStatus = e.target.value;
        loadPlayers();
    });

    document.getElementById('search-player-input').addEventListener('input', (e) => {
        state.searchQuery = e.target.value.toLowerCase().trim();
        renderPlayers();
    });

    // Formularios
    document.getElementById('form-player').addEventListener('submit', handlePlayerSubmit);
    document.getElementById('form-match').addEventListener('submit', handleMatchSubmit);
    document.getElementById('form-match-result').addEventListener('submit', handleMatchResultSubmit);
    document.getElementById('form-callup').addEventListener('submit', handleCallUpSubmit);
    document.getElementById('form-appearance-stats').addEventListener('submit', handleAppearanceStatsSubmit);
    document.getElementById('form-training').addEventListener('submit', handleTrainingSubmit);
    document.getElementById('form-attendance').addEventListener('submit', handleAttendanceSubmit);

    // Filtros analítica
    document.getElementById('analytics-pos-filter').addEventListener('change', renderAnalyticsTable);
    document.getElementById('analytics-sort-select').addEventListener('change', renderAnalyticsTable);

    document.getElementById('compare-player-a').addEventListener('change', renderPlayerComparison);
    document.getElementById('compare-player-b').addEventListener('change', renderPlayerComparison);
}

// ==================== DASHBOARD ====================
async function loadDashboard() {
    try {
        const [players, matches, stats] = await Promise.all([
            api.getPlayers(),
            api.getMatches(),
            api.getAllPlayersStats()
        ]);

        state.players = players;

        const availableCount = players.filter(p => p.playerStatus === 'AVAILABLE').length;
        document.getElementById('kpi-total-players').textContent = players.length;
        document.getElementById('kpi-available-players').textContent = `${availableCount} disponibles`;

        document.getElementById('kpi-total-matches').textContent = matches.length;
        const upcoming = matches.find(m => m.goalsFor === null);
        document.getElementById('kpi-next-match').textContent = upcoming ? `Próx: vs ${upcoming.opponent}` : 'Al día';

        const totalGoals = stats.reduce((acc, s) => acc + s.totalGoals, 0);
        const totalAssists = stats.reduce((acc, s) => acc + s.totalAssists, 0);
        document.getElementById('kpi-total-goals').textContent = totalGoals;
        document.getElementById('kpi-total-assists').textContent = `${totalAssists} asistencias`;

        const activeTrainingsStats = stats.filter(s => s.totalSessions > 0);
        const avgAttendance = activeTrainingsStats.length
            ? (activeTrainingsStats.reduce((acc, s) => acc + s.attendancePercentage, 0) / activeTrainingsStats.length).toFixed(1)
            : '0.0';
        const rpeStats = stats.filter(s => s.averageRpe > 0);
        const avgRpe = rpeStats.length
            ? (rpeStats.reduce((acc, s) => acc + s.averageRpe, 0) / rpeStats.length).toFixed(1)
            : '0.0';

        document.getElementById('kpi-avg-attendance').textContent = `${avgAttendance}%`;
        document.getElementById('kpi-avg-rpe').textContent = `RPE medio: ${avgRpe}`;

        renderDashboardCharts(players, stats);
        renderDashboardLists(matches, stats);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function renderDashboardCharts(players, stats) {
    // 1. Gráfico de Contribuciones (Goles + Asistencias)
    const topContributors = [...stats]
        .sort((a, b) => b.goalContributions - a.goalContributions || b.totalMinutes - a.totalMinutes)
        .slice(0, 6);

    const ctxContrib = document.getElementById('chart-contributions').getContext('2d');
    if (state.charts.contributions) state.charts.contributions.destroy();

    state.charts.contributions = new Chart(ctxContrib, {
        type: 'bar',
        data: {
            labels: topContributors.map(p => `#${p.shirtNumber} ${p.fullName.split(' ')[1] || p.fullName}`),
            datasets: [
                {
                    label: 'Goles',
                    data: topContributors.map(p => p.totalGoals),
                    backgroundColor: '#10b981',
                    borderRadius: 6
                },
                {
                    label: 'Asistencias',
                    data: topContributors.map(p => p.totalAssists),
                    backgroundColor: '#3b82f6',
                    borderRadius: 6
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: '#94a3b8', font: { size: 11 } } }
            },
            scales: {
                x: { stacked: true, ticks: { color: '#94a3b8' }, grid: { display: false } },
                y: { stacked: true, beginAtZero: true, ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: 'rgba(148,163,184,0.08)' } }
            }
        }
    });

    // 2. Gráfico de Disponibilidad
    const counts = {
        AVAILABLE: players.filter(p => p.playerStatus === 'AVAILABLE').length,
        INJURED: players.filter(p => p.playerStatus === 'INJURED').length,
        SUSPENDED: players.filter(p => p.playerStatus === 'SUSPENDED').length,
        UNAVAILABLE: players.filter(p => p.playerStatus === 'UNAVAILABLE').length
    };

    const ctxStatus = document.getElementById('chart-status').getContext('2d');
    if (state.charts.status) state.charts.status.destroy();

    state.charts.status = new Chart(ctxStatus, {
        type: 'doughnut',
        data: {
            labels: ['Disponibles', 'Lesionados', 'Sancionados', 'No disp.'],
            datasets: [{
                data: [counts.AVAILABLE, counts.INJURED, counts.SUSPENDED, counts.UNAVAILABLE],
                backgroundColor: ['#10b981', '#f43f5e', '#f59e0b', '#64748b'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '68%',
            plugins: {
                legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 14, font: { size: 11 } } }
            }
        }
    });
}

function renderDashboardLists(matches, stats) {
    const matchesContainer = document.getElementById('dashboard-matches-list');
    if (!matches.length) {
        matchesContainer.innerHTML = `<p class="text-xs text-slate-500">No hay partidos registrados.</p>`;
    } else {
        matchesContainer.innerHTML = matches.slice(0, 4).map(m => {
            const played = m.goalsFor !== null && m.goalsAgainst !== null;
            return `
                <div class="flex items-center justify-between p-3 rounded-xl bg-pitch-900/80 border border-slate-800/80">
                    <div>
                        <div class="flex items-center gap-2">
                            <span class="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">${m.homeMatch ? 'LOCAL' : 'VISITANTE'}</span>
                            <span class="text-sm font-bold text-white">vs ${m.opponent}</span>
                        </div>
                        <p class="text-[11px] text-slate-400 mt-0.5">${m.competition} · ${formatDateTime(m.matchDate)}</p>
                    </div>
                    <div class="text-right">
                        ${played
                ? `<span class="font-mono font-extrabold text-base px-2.5 py-1 rounded-lg bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">${m.goalsFor} - ${m.goalsAgainst}</span>`
                : `<span class="text-xs font-mono text-amber-400 bg-amber-500/10 px-2.5 py-1 rounded-lg border border-amber-500/20">Pendiente</span>`
            }
                    </div>
                </div>
            `;
        }).join('');
    }

    const minutesContainer = document.getElementById('dashboard-minutes-list');
    const topMinutes = [...stats].sort((a, b) => b.totalMinutes - a.totalMinutes).slice(0, 4);
    minutesContainer.innerHTML = topMinutes.map(p => `
        <div class="flex items-center justify-between p-3 rounded-xl bg-pitch-900/80 border border-slate-800/80">
            <div class="flex items-center gap-3">
                <span class="w-8 h-8 rounded-lg bg-slate-800 font-mono font-bold text-xs flex items-center justify-center text-emerald-400">#${p.shirtNumber}</span>
                <div>
                    <p class="text-sm font-semibold text-white">${p.fullName}</p>
                    <p class="text-[11px] text-slate-400">${POSITION_META[p.position]?.label || p.position} · ${p.matchesPlayed} partidos</p>
                </div>
            </div>
            <div class="text-right font-mono">
                <span class="text-sm font-bold text-white">${p.totalMinutes}'</span>
                <p class="text-[10px] text-slate-400">${p.totalGoals}G · ${p.totalAssists}A</p>
            </div>
        </div>
    `).join('');

    const medicalContainer = document.getElementById('dashboard-medical-list');
    if (medicalContainer) {
        const medicalAlerts = state.players.filter(p =>
            p.playerStatus === 'INJURED' || p.playerStatus === 'SUSPENDED' || p.warnedByCards
        );

        if (!medicalAlerts.length) {
            medicalContainer.innerHTML = `<p class="col-span-full text-xs text-slate-400">Toda la plantilla está disponible y sin jugadores apercibidos.</p>`;
        } else {
            medicalContainer.innerHTML = medicalAlerts.map(p => {
                if (p.playerStatus === 'INJURED') {
                    const rec = getRecoveryProgress(p.injuryStartDate, p.expectedReturnDate);
                    return `
                        <div class="p-3.5 rounded-xl bg-rose-950/20 border border-rose-500/25 space-y-2">
                            <div class="flex items-center justify-between">
                                <span class="text-xs font-bold text-white">#${p.shirtNumber} ${p.firstName} ${p.lastName}</span>
                                <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-400">LESIONADO</span>
                            </div>
                            <p class="text-[11px] text-slate-300">${p.injuryDescription || 'Baja médica'}</p>
                            ${rec ? `
                                <div>
                                    <div class="flex justify-between text-[10px] text-slate-400 mb-1">
                                        <span>Recuperación: ${rec.percent}%</span>
                                        <span class="text-rose-300 font-semibold">${rec.label}</span>
                                    </div>
                                    <div class="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                                        <div class="h-full bg-gradient-to-r from-rose-500 to-emerald-400 rounded-full" style="width: ${rec.percent}%"></div>
                                    </div>
                                </div>
                            ` : ''}
                        </div>
                    `;
                }
                if (p.playerStatus === 'SUSPENDED') {
                    return `
                        <div class="p-3.5 rounded-xl bg-amber-950/25 border border-amber-500/30 flex items-center justify-between">
                            <div>
                                <span class="text-xs font-bold text-white block">#${p.shirtNumber} ${p.firstName} ${p.lastName}</span>
                                <span class="text-[11px] text-amber-300">Sanción disciplinaria activa</span>
                            </div>
                            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30">SANCIONADO</span>
                        </div>
                    `;
                }
                return `
                    <div class="p-3.5 rounded-xl bg-pitch-900/90 border border-amber-500/25 flex items-center justify-between">
                        <div>
                            <span class="text-xs font-bold text-white block">#${p.shirtNumber} ${p.firstName} ${p.lastName}</span>
                            <span class="text-[11px] text-slate-400">Acumula ${p.totalYellowCards} amarillas</span>
                        </div>
                        <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/40">APERCIBIDO</span>
                    </div>
                `;
            }).join('');
        }
    }
}

// ==================== PLANTILLA (PLAYERS) ====================
async function loadPlayers() {
    try {
        state.players = await api.getPlayers(state.selectedPosition, state.selectedStatus);
        renderPlayers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}



function toggleInjuryFields() {
    const isInjured = document.getElementById('player-status').value === 'INJURED';
    document.getElementById('player-injury-fields').classList.toggle('hidden', !isInjured);
}

function renderPlayers() {
    const grid = document.getElementById('players-grid');
    const filtered = state.players.filter(p => {
        if (!state.searchQuery) return true;
        const fullName = `${p.firstName} ${p.lastName}`.toLowerCase();
        return fullName.includes(state.searchQuery) || String(p.shirtNumber).includes(state.searchQuery);
    });

    if (!filtered.length) {
        grid.innerHTML = `
            <div class="col-span-full glass-card p-10 rounded-2xl text-center">
                <p class="text-slate-400 text-sm">No se encontraron jugadores con los filtros seleccionados.</p>
            </div>`;
        return;
    }

    grid.innerHTML = filtered.map(p => {
        const pos = POSITION_META[p.position] || { label: p.position, badge: 'bg-slate-800 text-slate-300' };
        const st = STATUS_META[p.playerStatus] || { label: p.playerStatus, badge: 'bg-slate-800 text-slate-300' };
        const age = calculateAge(p.birthDate);
        const cycleCount = p.totalYellowCards % 5;

        let injuryBlock = '';
        if (p.playerStatus === 'INJURED') {
            const rec = getRecoveryProgress(p.injuryStartDate, p.expectedReturnDate);
            injuryBlock = `
                <div class="mt-3 p-3 rounded-xl bg-rose-950/25 border border-rose-500/25 space-y-2">
                    <div class="flex items-center justify-between text-[11px]">
                        <span class="font-semibold text-rose-300 truncate">${p.injuryDescription || 'Baja médica'}</span>
                        ${rec ? `<span class="font-mono text-[10px] text-rose-400 shrink-0 ml-2">${rec.label}</span>` : ''}
                    </div>
                    ${rec ? `
                        <div class="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                            <div class="h-full bg-gradient-to-r from-rose-500 to-emerald-400 rounded-full" style="width: ${rec.percent}%"></div>
                        </div>
                    ` : ''}
                </div>
            `;
        }

        const cardBadge = p.warnedByCards
            ? `<span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-amber-500/20 text-amber-400 border border-amber-500/40">4/5 AMARILLAS · APERCIBIDO</span>`
            : `<span class="text-[10px] font-mono text-slate-400">Ciclo: ${cycleCount}/5 A</span>`;

        return `
            <div class="glass-card p-5 rounded-2xl flex flex-col justify-between hover:border-slate-700 transition-all">
                <div>
                    <div class="flex items-start justify-between gap-3">
                        <div class="flex items-center gap-3.5">
                            <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-slate-800 to-slate-900 border border-slate-700/80 flex items-center justify-center font-mono font-extrabold text-xl text-emerald-400 shadow-inner">
                                ${p.shirtNumber}
                            </div>
                            <div>
                                <h4 class="font-bold text-white text-base leading-tight">${p.firstName} ${p.lastName}</h4>
                                <p class="text-xs text-slate-400 mt-0.5">${age} años · Nac. ${p.birthDate}</p>
                            </div>
                        </div>
                        <span class="text-[11px] font-semibold px-2.5 py-1 rounded-full border ${st.badge}">${st.label}</span>
                    </div>

                    ${injuryBlock}

                    <div class="mt-4 pt-3 border-t border-slate-800/70 flex items-center justify-between gap-2">
                        <div class="flex items-center gap-2 flex-wrap">
                            <span class="text-[11px] font-semibold px-2.5 py-0.5 rounded-md border ${pos.badge}">${pos.label}</span>
                            ${cardBadge}
                        </div>
                        <button onclick="openPlayerStatsModal(${p.id})" class="text-xs text-emerald-400 hover:text-emerald-300 font-medium inline-flex items-center gap-1 shrink-0">
                            <i data-lucide="bar-chart-2" class="w-3.5 h-3.5"></i> Stats
                        </button>
                    </div>
                </div>

                <div class="mt-4 pt-3 border-t border-slate-800/70 flex items-center justify-end gap-2">
                    <button onclick='openPlayerModal(${JSON.stringify(p)})' class="btn-secondary py-1.5 px-3 text-xs">
                        <i data-lucide="pencil" class="w-3.5 h-3.5"></i> Editar
                    </button>
                    <button onclick="deletePlayer(${p.id}, '${p.firstName} ${p.lastName}')" class="p-2 rounded-xl bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/20 transition-colors" title="Eliminar jugador">
                        <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');

    lucide.createIcons();
}

function openPlayerModal(player = null) {
    document.getElementById('form-player').reset();
    if (player) {
        document.getElementById('modal-player-title').textContent = `Editar a ${player.firstName} ${player.lastName}`;
        document.getElementById('player-id').value = player.id;
        document.getElementById('player-firstName').value = player.firstName;
        document.getElementById('player-lastName').value = player.lastName;
        document.getElementById('player-shirtNumber').value = player.shirtNumber;
        document.getElementById('player-birthDate').value = player.birthDate;
        document.getElementById('player-position').value = player.position;
        document.getElementById('player-status').value = player.playerStatus;
        document.getElementById('player-injuryDescription').value = player.injuryDescription || '';
        document.getElementById('player-injuryStartDate').value = player.injuryStartDate || '';
        document.getElementById('player-expectedReturnDate').value = player.expectedReturnDate || '';
    } else {
        document.getElementById('modal-player-title').textContent = 'Alta de Nuevo Jugador';
        document.getElementById('player-id').value = '';
    }
    toggleInjuryFields();
    openModal('modal-player');
}

async function handlePlayerSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('player-id').value;
    const status = document.getElementById('player-status').value;

    const payload = {
        firstName: document.getElementById('player-firstName').value.trim(),
        lastName: document.getElementById('player-lastName').value.trim(),
        shirtNumber: parseInt(document.getElementById('player-shirtNumber').value, 10),
        birthDate: document.getElementById('player-birthDate').value,
        position: document.getElementById('player-position').value,
        playerStatus: status,
        injuryDescription: status === 'INJURED' ? (document.getElementById('player-injuryDescription').value.trim() || null) : null,
        injuryStartDate: status === 'INJURED' ? (document.getElementById('player-injuryStartDate').value || null) : null,
        expectedReturnDate: status === 'INJURED' ? (document.getElementById('player-expectedReturnDate').value || null) : null
    };

    try {
        if (id) {
            await api.updatePlayer(id, payload);
            showToast('Ficha del jugador actualizada correctamente', 'success');
        } else {
            await api.createPlayer(payload);
            showToast('Jugador incorporado a la plantilla', 'success');
        }
        closeModal('modal-player');
        loadPlayers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deletePlayer(id, name) {
    if (!confirm(`¿Seguro que deseas eliminar a ${name} de la plantilla? Se borrarán sus registros asociados.`)) return;
    try {
        await api.deletePlayer(id);
        showToast(`Jugador ${name} eliminado`, 'info');
        loadPlayers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==================== PARTIDOS (MATCHES) ====================
async function loadMatches() {
    try {
        const matches = await api.getMatches();
        const grid = document.getElementById('matches-grid');

        if (!matches.length) {
            grid.innerHTML = `<div class="col-span-full glass-card p-10 rounded-2xl text-center text-slate-400 text-sm">No hay partidos programados todavía.</div>`;
            return;
        }

        grid.innerHTML = matches.map(m => {
            const played = m.goalsFor !== null && m.goalsAgainst !== null;
            let resultBadge = `<span class="text-xs font-mono px-3 py-1 rounded-full bg-amber-500/15 text-amber-400 border border-amber-500/30">Por disputar</span>`;
            if (played) {
                const win = m.goalsFor > m.goalsAgainst;
                const draw = m.goalsFor === m.goalsAgainst;
                const color = win ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' : (draw ? 'bg-slate-500/20 text-slate-300 border-slate-500/30' : 'bg-rose-500/15 text-rose-400 border-rose-500/30');
                resultBadge = `<span class="text-base font-mono font-extrabold px-3.5 py-1 rounded-xl border ${color}">${m.goalsFor} - ${m.goalsAgainst}</span>`;
            }

            return `
                <div class="glass-card p-5 rounded-2xl flex flex-col justify-between gap-4 hover:border-slate-700 transition-all">
                    <div class="flex items-start justify-between gap-3">
                        <div>
                            <div class="flex items-center gap-2 mb-1.5">
                                <span class="text-[10px] font-mono uppercase tracking-wider px-2 py-0.5 rounded bg-slate-800 text-emerald-400">${m.competition}</span>
                                <span class="text-[10px] font-mono uppercase tracking-wider px-2 py-0.5 rounded bg-slate-800 text-slate-300">${m.homeMatch ? 'Local' : 'Visitante'}</span>
                            </div>
                            <h4 class="text-lg font-extrabold text-white">vs ${m.opponent}</h4>
                            <p class="text-xs text-slate-400 mt-1 flex items-center gap-1.5">
                                <i data-lucide="calendar" class="w-3.5 h-3.5"></i> ${formatDateTime(m.matchDate)}
                            </p>
                        </div>
                        ${resultBadge}
                    </div>

                    <div class="pt-3 border-t border-slate-800/80 flex justify-end">
                        <button onclick="openMatchDetail(${m.id})" class="btn-primary text-xs">
                            <i data-lucide="clipboard-list" class="w-4 h-4"></i> Convocatoria y Acta
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        lucide.createIcons();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleMatchSubmit(e) {
    e.preventDefault();
    const payload = {
        opponent: document.getElementById('match-opponent').value.trim(),
        matchDate: document.getElementById('match-date').value,
        competition: document.getElementById('match-competition').value.trim(),
        homeMatch: document.getElementById('match-home').value === 'true'
    };

    try {
        await api.createMatch(payload);
        showToast('Partido añadido al calendario', 'success');
        closeModal('modal-match');
        e.target.reset();
        loadMatches();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function openMatchDetail(matchId) {
    state.currentMatchId = matchId;
    hideAppearanceEditor();

    try {
        const [match, players] = await Promise.all([
            api.getMatchById(matchId),
            api.getPlayers()
        ]);

        state.currentMatchData = match;
        state.tacticalFormation = match.formation || '4-3-3';
        state.tacticalSlots = {};
        (match.appearances || []).forEach(a => {
            if (a.lineupSlot && a.lineupSlot >= 1 && a.lineupSlot <= 11) {
                state.tacticalSlots[a.lineupSlot] = a.playerId;
            }
        });
        switchMatchTab('callup');

        document.getElementById('md-competition').textContent = `${match.competition} · ${match.homeMatch ? 'LOCAL' : 'VISITANTE'}`;
        document.getElementById('md-title').textContent = `vs ${match.opponent}`;
        document.getElementById('md-date').textContent = formatDateTime(match.matchDate);

        document.getElementById('md-goalsFor').value = match.goalsFor !== null ? match.goalsFor : 0;
        document.getElementById('md-goalsAgainst').value = match.goalsAgainst !== null ? match.goalsAgainst : 0;

        // Poblar selector de jugadores para convocar
        const select = document.getElementById('callup-player-select');
        select.innerHTML = players.map(p => {
            let statusTag = '';
            if (p.playerStatus !== 'AVAILABLE') {
                statusTag = ` [${STATUS_META[p.playerStatus]?.label.toUpperCase()}]`;
            } else if (p.warnedByCards) {
                statusTag = ` [APERCIBIDO ${p.totalYellowCards}A]`;
            }
            return `<option value="${p.id}">#${p.shirtNumber} ${p.firstName} ${p.lastName} (${POSITION_META[p.position]?.short})${statusTag}</option>`;
        }).join('');

        const appearances = match.appearances || match.matchAppearances || [];
        const startersCount = appearances.filter(a => a.callUpStatus === 'STARTER').length;
        document.getElementById('md-starters-count').textContent = `${startersCount} / 11 Titulares`;

        const tbody = document.getElementById('md-appearances-body');
        if (!appearances.length) {
            tbody.innerHTML = `<tr><td colspan="8" class="py-6 text-center text-slate-500">Aún no hay jugadores convocados para este encuentro.</td></tr>`;
        } else {
            tbody.innerHTML = appearances.map(a => {
                const cMeta = CALLUP_META[a.callUpStatus] || { label: a.callUpStatus, badge: 'bg-slate-800 text-slate-300' };
                const name = a.playerFullName || a.fullName || 'Jugador';
                return `
                    <tr class="hover:bg-slate-800/30">
                        <td class="py-2.5 px-3 font-mono font-bold text-emerald-400">${a.shirtNumber}</td>
                        <td class="py-2.5 px-3 font-semibold text-white">${name}</td>
                        <td class="py-2.5 px-3"><span class="px-2 py-0.5 rounded-full text-[10px] font-semibold border ${cMeta.badge}">${cMeta.label}</span></td>
                        <td class="py-2.5 px-3 text-center font-mono">${a.minutesPlayed}'</td>
                        <td class="py-2.5 px-3 text-center font-mono font-bold text-white">${a.goals}</td>
                        <td class="py-2.5 px-3 text-center font-mono">${a.assists}</td>
                        <td class="py-2.5 px-3 text-center font-mono">
                            ${a.yellowCards > 0 ? `<span class="text-amber-400 font-bold">${a.yellowCards}A</span>` : '-'}
                            ${a.redCard ? `<span class="ml-1 text-rose-500 font-bold">1R</span>` : ''}
                        </td>
                        <td class="py-2.5 px-3 text-right space-x-1">
                            <button onclick='openAppearanceEditor(${JSON.stringify(a)})' class="px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 text-[11px]">Acta</button>
                            <button onclick="removeAppearance(${a.playerId})" class="px-2 py-1 rounded bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-[11px]">Quitar</button>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        openModal('modal-match-detail');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleMatchResultSubmit(e) {
    e.preventDefault();
    const payload = {
        goalsFor: parseInt(document.getElementById('md-goalsFor').value, 10),
        goalsAgainst: parseInt(document.getElementById('md-goalsAgainst').value, 10)
    };
    try {
        await api.updateMatchResult(state.currentMatchId, payload);
        showToast('Marcador actualizado', 'success');
        loadMatches();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleCallUpSubmit(e) {
    e.preventDefault();
    const payload = {
        playerId: parseInt(document.getElementById('callup-player-select').value, 10),
        callUpStatus: document.getElementById('callup-status-select').value
    };
    try {
        await api.addPlayerToMatch(state.currentMatchId, payload);
        showToast('Jugador añadido a la convocatoria', 'success');
        openMatchDetail(state.currentMatchId);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function openAppearanceEditor(app) {
    const box = document.getElementById('edit-appearance-box');
    box.classList.remove('hidden');
    document.getElementById('edit-app-title').textContent = `Acta: #${app.shirtNumber} ${app.playerFullName || app.fullName}`;
    document.getElementById('edit-app-playerId').value = app.playerId;
    document.getElementById('edit-app-status').value = app.callUpStatus;
    document.getElementById('edit-app-minutes').value = app.minutesPlayed;
    document.getElementById('edit-app-goals').value = app.goals;
    document.getElementById('edit-app-assists').value = app.assists;
    document.getElementById('edit-app-yellows').value = app.yellowCards;
    document.getElementById('edit-app-red').checked = Boolean(app.redCard);
}

function hideAppearanceEditor() {
    document.getElementById('edit-appearance-box').classList.add('hidden');
}

async function handleAppearanceStatsSubmit(e) {
    e.preventDefault();
    const playerId = document.getElementById('edit-app-playerId').value;
    const payload = {
        callUpStatus: document.getElementById('edit-app-status').value,
        minutesPlayed: parseInt(document.getElementById('edit-app-minutes').value, 10),
        goals: parseInt(document.getElementById('edit-app-goals').value, 10),
        assists: parseInt(document.getElementById('edit-app-assists').value, 10),
        yellowCards: parseInt(document.getElementById('edit-app-yellows').value, 10),
        redCard: document.getElementById('edit-app-red').checked
    };

    try {
        await api.updateMatchAppearance(state.currentMatchId, playerId, payload);
        showToast('Acta post-partido guardada', 'success');
        openMatchDetail(state.currentMatchId);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function removeAppearance(playerId) {
    try {
        await api.removePlayerFromMatch(state.currentMatchId, playerId);
        showToast('Jugador retirado de la convocatoria', 'info');
        openMatchDetail(state.currentMatchId);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==================== PIZARRA TÁCTICA (11 INICIAL) ====================
const FORMATIONS_MAP = {
    '4-3-3': [
        { slot: 1, role: 'POR', x: 50, y: 90 },
        { slot: 2, role: 'LD',  x: 84, y: 72 },
        { slot: 3, role: 'DFC', x: 63, y: 75 },
        { slot: 4, role: 'DFC', x: 37, y: 75 },
        { slot: 5, role: 'LI',  x: 16, y: 72 },
        { slot: 6, role: 'MCD', x: 50, y: 55 },
        { slot: 7, role: 'MC',  x: 72, y: 46 },
        { slot: 8, role: 'MC',  x: 28, y: 46 },
        { slot: 9, role: 'ED',  x: 80, y: 24 },
        { slot: 10, role: 'DC', x: 50, y: 17 },
        { slot: 11, role: 'EI', x: 20, y: 24 }
    ],
    '4-4-2': [
        { slot: 1, role: 'POR', x: 50, y: 90 },
        { slot: 2, role: 'LD',  x: 84, y: 72 },
        { slot: 3, role: 'DFC', x: 63, y: 75 },
        { slot: 4, role: 'DFC', x: 37, y: 75 },
        { slot: 5, role: 'LI',  x: 16, y: 72 },
        { slot: 6, role: 'MD',  x: 82, y: 46 },
        { slot: 7, role: 'MC',  x: 62, y: 49 },
        { slot: 8, role: 'MC',  x: 38, y: 49 },
        { slot: 9, role: 'MI',  x: 18, y: 46 },
        { slot: 10, role: 'DC', x: 62, y: 20 },
        { slot: 11, role: 'DC', x: 38, y: 20 }
    ],
    '4-2-3-1': [
        { slot: 1, role: 'POR', x: 50, y: 90 },
        { slot: 2, role: 'LD',  x: 84, y: 73 },
        { slot: 3, role: 'DFC', x: 63, y: 76 },
        { slot: 4, role: 'DFC', x: 37, y: 76 },
        { slot: 5, role: 'LI',  x: 16, y: 73 },
        { slot: 6, role: 'MCD', x: 63, y: 56 },
        { slot: 7, role: 'MCD', x: 37, y: 56 },
        { slot: 8, role: 'ED',  x: 80, y: 36 },
        { slot: 9, role: 'MP',  x: 50, y: 37 },
        { slot: 10, role: 'EI', x: 20, y: 36 },
        { slot: 11, role: 'DC', x: 50, y: 16 }
    ],
    '3-5-2': [
        { slot: 1, role: 'POR', x: 50, y: 90 },
        { slot: 2, role: 'DFC', x: 74, y: 75 },
        { slot: 3, role: 'DFC', x: 50, y: 77 },
        { slot: 4, role: 'DFC', x: 26, y: 75 },
        { slot: 5, role: 'CAD', x: 86, y: 50 },
        { slot: 6, role: 'MC',  x: 66, y: 52 },
        { slot: 7, role: 'MCD', x: 50, y: 57 },
        { slot: 8, role: 'MC',  x: 34, y: 52 },
        { slot: 9, role: 'CAI', x: 14, y: 50 },
        { slot: 10, role: 'DC', x: 62, y: 21 },
        { slot: 11, role: 'DC', x: 38, y: 21 }
    ]
};

function switchMatchTab(tab) {
    const isTactical = tab === 'tactical';
    document.getElementById('md-panel-callup').classList.toggle('hidden', isTactical);
    document.getElementById('md-panel-tactical').classList.toggle('hidden', !isTactical);

    document.getElementById('md-tab-callup').className = !isTactical
        ? 'px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-500/20 text-emerald-400'
        : 'px-3 py-1.5 rounded-lg text-xs font-bold text-slate-400 hover:text-white';

    document.getElementById('md-tab-tactical').className = isTactical
        ? 'px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-500/20 text-emerald-400 flex items-center gap-1.5'
        : 'px-3 py-1.5 rounded-lg text-xs font-bold text-slate-400 hover:text-white flex items-center gap-1.5';

    if (isTactical) {
        document.getElementById('tactical-formation-select').value = state.tacticalFormation;
        renderTacticalPitch();
    }
}

function changeTacticalFormation(formation) {
    state.tacticalFormation = formation;
    renderTacticalPitch();
}

function renderTacticalPitch() {
    const nodes = FORMATIONS_MAP[state.tacticalFormation] || FORMATIONS_MAP['4-3-3'];
    const appearances = state.currentMatchData?.appearances || [];
    const layer = document.getElementById('pitch-nodes-layer');

    layer.innerHTML = nodes.map(n => {
        const assignedPlayerId = state.tacticalSlots[n.slot];
        const playerApp = appearances.find(a => a.playerId === assignedPlayerId);
        const isSelected = state.selectedPitchSlot === n.slot;

        const tokenStyle = playerApp
            ? 'bg-slate-950 text-emerald-400 border-2 border-emerald-400'
            : 'bg-slate-900/80 text-slate-400 border border-dashed border-slate-400';

        const surname = playerApp
            ? (playerApp.playerFullName.split(' ').slice(1).join(' ') || playerApp.playerFullName)
            : n.role;

        return `
            <div onclick="selectPitchSlot(${n.slot})" class="pitch-node ${isSelected ? 'selected' : ''}" style="left: ${n.x}%; top: ${n.y}%;">
                <div class="pitch-token ${tokenStyle}">${playerApp ? playerApp.shirtNumber : '+'}</div>
                <span class="mt-1 px-2 py-0.5 rounded bg-slate-950/90 text-[10px] font-bold text-white shadow whitespace-nowrap border border-slate-800">
                    ${surname}
                </span>
            </div>
        `;
    }).join('');

    updateSlotAssignerUI();
}

function selectPitchSlot(slot) {
    state.selectedPitchSlot = slot;
    renderTacticalPitch();
}

function updateSlotAssignerUI() {
    const nodes = FORMATIONS_MAP[state.tacticalFormation] || FORMATIONS_MAP['4-3-3'];
    const slotMeta = nodes.find(n => n.slot === state.selectedPitchSlot) || nodes[0];
    document.getElementById('tactical-selected-slot-label').textContent = `Posición #${slotMeta.slot} · ${slotMeta.role}`;

    const appearances = state.currentMatchData?.appearances || [];
    const select = document.getElementById('tactical-player-assign');
    const currentAssigned = state.tacticalSlots[slotMeta.slot] || '';

    select.innerHTML = `<option value="">-- Seleccionar jugador convocado --</option>` + appearances.map(a => {
        const usedInOtherSlot = Object.entries(state.tacticalSlots)
            .some(([s, pId]) => Number(s) !== slotMeta.slot && pId === a.playerId);
        return `<option value="${a.playerId}" ${a.playerId === currentAssigned ? 'selected' : ''}>
            #${a.shirtNumber} ${a.playerFullName} (${POSITION_META[a.position]?.short})${usedInOtherSlot ? ' [En campo]' : ''}
        </option>`;
    }).join('');
}

function assignPlayerToSelectedSlot(playerIdStr) {
    if (!playerIdStr) {
        clearSelectedPitchSlot();
        return;
    }
    const playerId = parseInt(playerIdStr, 10);
    // Si el jugador estaba en otro puesto, lo movemos al nuevo puesto
    Object.keys(state.tacticalSlots).forEach(s => {
        if (state.tacticalSlots[s] === playerId) {
            delete state.tacticalSlots[s];
        }
    });
    state.tacticalSlots[state.selectedPitchSlot] = playerId;

    // Avanzar automáticamente al siguiente hueco vacío
    for (let i = 1; i <= 11; i++) {
        if (!state.tacticalSlots[i]) {
            state.selectedPitchSlot = i;
            break;
        }
    }
    renderTacticalPitch();
}

function clearSelectedPitchSlot() {
    delete state.tacticalSlots[state.selectedPitchSlot];
    renderTacticalPitch();
}

function autoFillLineupFromStarters() {
    const appearances = state.currentMatchData?.appearances || [];
    const starters = appearances.filter(a => a.callUpStatus === 'STARTER').slice(0, 11);
    if (!starters.length) {
        showToast('No hay jugadores marcados como Titular en la convocatoria todavía', 'info');
        return;
    }
    state.tacticalSlots = {};
    starters.forEach((s, idx) => {
        state.tacticalSlots[idx + 1] = s.playerId;
    });
    renderTacticalPitch();
}

async function saveTacticalLineup() {
    try {
        const updatedMatch = await api.updateMatchLineup(state.currentMatchId, {
            formation: state.tacticalFormation,
            slots: state.tacticalSlots
        });
        state.currentMatchData = updatedMatch;
        showToast('Once inicial y pizarra táctica guardados', 'success');
        openMatchDetail(state.currentMatchId);
        switchMatchTab('tactical');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function exportCallUpPdf() {
    const includeRoles = document.getElementById('export-include-roles')?.checked ?? false;
    try {
        await api.downloadMatchCallUpPdf(state.currentMatchId, includeRoles);
        showToast(includeRoles ? 'PDF de convocatoria con roles descargado' : 'PDF de convocatoria previa descargado', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function copyCallUpToWhatsApp() {
    const match = state.currentMatchData;
    if (!match) return;

    const calledUp = (match.appearances || []).filter(a => a.callUpStatus !== 'NOT_CALLED');
    if (!calledUp.length) {
        showToast('Añade al menos un jugador convocado antes de copiar', 'info');
        return;
    }

    const includeRoles = document.getElementById('export-include-roles')?.checked ?? false;
    const user = JSON.parse(localStorage.getItem('squad_user') || '{}');
    const clubName = user.clubName || 'Convocatoria Oficial';
    const condition = match.homeMatch ? 'Local' : 'Visitante';

    const groups = [
        { key: 'GOALKEEPER', title: '🧤 Porteros' },
        { key: 'DEFENDER',   title: '🛡️ Defensas' },
        { key: 'MIDFIELDER', title: '⚙️ Centrocampistas' },
        { key: 'FORWARD',    title: '⚽ Delanteros' }
    ];

    const lines = [
        `📋 *CONVOCATORIA OFICIAL — ${clubName.toUpperCase()}*`,
        `🏆 ${match.competition}`,
        `🆚 *vs ${match.opponent}* (${condition})`,
        `🗓️ ${formatDateTime(match.matchDate)}`,
        ``
    ];

    groups.forEach(g => {
        const list = calledUp
            .filter(a => a.position === g.key)
            .sort((a, b) => a.shirtNumber - b.shirtNumber);
        if (list.length) {
            lines.push(`*${g.title}*`);
            list.forEach(p => {
                const roleSuffix = includeRoles
                    ? ` _(${p.callUpStatus === 'STARTER' ? 'Titular' : 'Suplente'})_`
                    : '';
                lines.push(`• #${p.shirtNumber} ${p.playerFullName}${roleSuffix}`);
            });
            lines.push(``);
        }
    });

    lines.push(`Total convocados: *${calledUp.length} jugadores*`);
    const text = lines.join('\n');

    try {
        await navigator.clipboard.writeText(text);
        showToast(includeRoles ? 'Convocatoria con roles copiada para WhatsApp' : 'Convocatoria previa copiada para WhatsApp', 'success');
    } catch (_) {
        const area = document.createElement('textarea');
        area.value = text;
        document.body.appendChild(area);
        area.select();
        document.execCommand('copy');
        area.remove();
        showToast('Convocatoria copiada al portapapeles', 'success');
    }
}

// ==================== ENTRENAMIENTOS (TRAININGS) ====================
async function loadTrainings() {
    try {
        const sessions = await api.getTrainings();
        const grid = document.getElementById('trainings-grid');

        if (!sessions.length) {
            grid.innerHTML = `<div class="col-span-full glass-card p-10 rounded-2xl text-center text-slate-400 text-sm">No hay sesiones de entrenamiento registradas.</div>`;
            return;
        }

        grid.innerHTML = sessions.map(s => `
            <div class="glass-card p-5 rounded-2xl flex flex-col justify-between gap-4 hover:border-slate-700 transition-all">
                <div>
                    <div class="flex items-center justify-between gap-2 mb-2">
                        <span class="text-xs font-mono text-emerald-400 flex items-center gap-1.5">
                            <i data-lucide="calendar" class="w-3.5 h-3.5"></i> ${formatDateTime(s.sessionDate)}
                        </span>
                        <span class="text-xs font-mono px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300">${s.durationMinutes} min</span>
                    </div>
                    <h4 class="text-base font-bold text-white">${s.focus}</h4>
                    <p class="text-xs text-slate-400 mt-2 line-clamp-2">${s.notes || 'Sin observaciones adicionales.'}</p>
                </div>

                <div class="pt-3 border-t border-slate-800/80 flex items-center justify-between">
                    <button onclick="deleteTraining(${s.id})" class="p-2 rounded-xl bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/20" title="Eliminar sesión">
                        <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                    </button>
                    <button onclick="openTrainingDetail(${s.id})" class="btn-primary text-xs">
                        <i data-lucide="check-square" class="w-4 h-4"></i> Control Asistencia y RPE
                    </button>
                </div>
            </div>
        `).join('');

        lucide.createIcons();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleTrainingSubmit(e) {
    e.preventDefault();
    const payload = {
        sessionDate: document.getElementById('training-date').value,
        focus: document.getElementById('training-focus').value.trim(),
        durationMinutes: parseInt(document.getElementById('training-duration').value, 10),
        notes: document.getElementById('training-notes').value.trim() || null
    };

    try {
        await api.createTraining(payload);
        showToast('Sesión de entrenamiento creada', 'success');
        closeModal('modal-training');
        e.target.reset();
        loadTrainings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteTraining(id) {
    if (!confirm('¿Eliminar esta sesión de entrenamiento y sus registros de asistencia?')) return;
    try {
        await api.deleteTraining(id);
        showToast('Sesión eliminada', 'info');
        loadTrainings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function openTrainingDetail(sessionId) {
    state.currentTrainingId = sessionId;
    resetAttendanceForm();

    try {
        const [session, players] = await Promise.all([
            api.getTrainingById(sessionId),
            api.getPlayers()
        ]);

        document.getElementById('td-date').textContent = `${formatDateTime(session.sessionDate)} · ${session.durationMinutes} MIN`;
        document.getElementById('td-focus').textContent = session.focus;

        const select = document.getElementById('att-player-select');
        select.innerHTML = players.map(p => `<option value="${p.id}">#${p.shirtNumber} ${p.firstName} ${p.lastName}</option>`).join('');

        const attendances = session.attendances || [];
        const tbody = document.getElementById('td-attendances-body');

        if (!attendances.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="py-6 text-center text-slate-500">Todavía no se ha pasado lista en esta sesión.</td></tr>`;
        } else {
            tbody.innerHTML = attendances.map(a => {
                const aMeta = ATTENDANCE_META[a.attendanceStatus] || { label: a.attendanceStatus, badge: 'bg-slate-800 text-slate-300' };
                const rpeBadge = a.rpe
                    ? `<span class="font-mono font-bold px-2 py-0.5 rounded ${a.rpe >= 8 ? 'bg-rose-500/20 text-rose-400' : (a.rpe >= 5 ? 'bg-amber-500/20 text-amber-400' : 'bg-emerald-500/20 text-emerald-400')}">${a.rpe}/10</span>`
                    : `<span class="text-slate-500">-</span>`;

                return `
                    <tr class="hover:bg-slate-800/30">
                        <td class="py-2.5 px-3 font-semibold text-white">#${a.shirtNumber} ${a.playerFullName}</td>
                        <td class="py-2.5 px-3"><span class="px-2 py-0.5 rounded-full text-[10px] font-semibold border ${aMeta.badge}">${aMeta.label}</span></td>
                        <td class="py-2.5 px-3 text-slate-400">${a.absenceReason || '-'}</td>
                        <td class="py-2.5 px-3 text-center">${rpeBadge}</td>
                        <td class="py-2.5 px-3 text-right">
                            <button onclick='editAttendanceRow(${JSON.stringify(a)})' class="px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 text-[11px]">Editar</button>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        openModal('modal-training-detail');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function editAttendanceRow(att) {
    document.getElementById('att-is-edit').value = 'true';
    document.getElementById('att-form-title').textContent = `Editando registro de ${att.playerFullName}`;
    document.getElementById('att-cancel-edit').classList.remove('hidden');
    document.getElementById('att-submit-btn').textContent = 'Actualizar Registro';

    const select = document.getElementById('att-player-select');
    select.value = att.playerId;
    select.disabled = true;

    document.getElementById('att-status-select').value = att.attendanceStatus;
    document.getElementById('att-reason').value = att.absenceReason || '';
    document.getElementById('att-rpe').value = att.rpe || '';
}

function resetAttendanceForm() {
    document.getElementById('form-attendance').reset();
    document.getElementById('att-is-edit').value = 'false';
    document.getElementById('att-form-title').textContent = 'Registrar Asistencia y Carga (RPE)';
    document.getElementById('att-cancel-edit').classList.add('hidden');
    document.getElementById('att-submit-btn').textContent = 'Registrar Asistencia';
    document.getElementById('att-player-select').disabled = false;
}

async function handleAttendanceSubmit(e) {
    e.preventDefault();
    const isEdit = document.getElementById('att-is-edit').value === 'true';
    const playerId = parseInt(document.getElementById('att-player-select').value, 10);
    const rpeRaw = document.getElementById('att-rpe').value;
    const reasonRaw = document.getElementById('att-reason').value.trim();

    const payload = {
        playerId,
        attendanceStatus: document.getElementById('att-status-select').value,
        absenceReason: reasonRaw || null,
        rpe: rpeRaw ? parseInt(rpeRaw, 10) : null
    };

    try {
        if (isEdit) {
            await api.updateAttendance(state.currentTrainingId, playerId, payload);
            showToast('Asistencia actualizada', 'success');
        } else {
            await api.registerAttendance(state.currentTrainingId, payload);
            showToast('Asistencia registrada', 'success');
        }
        openTrainingDetail(state.currentTrainingId);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==================== ANALÍTICA ====================
async function loadAnalytics() {
    try {
        state.analyticsData = await api.getAllPlayersStats();
        populateComparisonSelectors();
        renderPlayerComparison();
        renderAnalyticsTable();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function populateComparisonSelectors() {
    const selA = document.getElementById('compare-player-a');
    const selB = document.getElementById('compare-player-b');
    if (!state.analyticsData.length) return;

    const optionsHtml = state.analyticsData.map(p =>
        `<option value="${p.playerId}">#${p.shirtNumber} ${p.fullName} (${POSITION_META[p.position]?.short})</option>`
    ).join('');

    selA.innerHTML = optionsHtml;
    selB.innerHTML = optionsHtml;

    if (state.analyticsData.length > 1) {
        selB.selectedIndex = 1;
    }
}

function renderPlayerComparison() {
    const idA = parseInt(document.getElementById('compare-player-a').value, 10);
    const idB = parseInt(document.getElementById('compare-player-b').value, 10);
    const pA = state.analyticsData.find(p => p.playerId === idA);
    const pB = state.analyticsData.find(p => p.playerId === idB);
    if (!pA || !pB) return;

    const maxMinutes = Math.max(...state.analyticsData.map(p => p.totalMinutes), 90);
    const maxContrib90 = Math.max(...state.analyticsData.map(p => p.goalContributionsPer90), 1.5);
    const maxCleanSheets = Math.max(...state.analyticsData.map(p => p.cleanSheets), 1);

    const normalize = (val, max) => max > 0 ? Math.min(Math.round((val / max) * 100), 100) : 0;

    const ctx = document.getElementById('chart-player-radar').getContext('2d');
    if (state.charts.radar) state.charts.radar.destroy();

    state.charts.radar = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['Minutos', 'G+A / 90\'', '% Victorias', 'Porterías a 0', 'Asist. Entreno', 'Carga RPE'],
            datasets: [
                {
                    label: `#${pA.shirtNumber} ${pA.fullName}`,
                    data: [
                        normalize(pA.totalMinutes, maxMinutes),
                        normalize(pA.goalContributionsPer90, maxContrib90),
                        pA.winRatePercentage,
                        normalize(pA.cleanSheets, maxCleanSheets),
                        pA.attendancePercentage,
                        pA.averageRpe * 10
                    ],
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    borderColor: '#10b981',
                    pointBackgroundColor: '#10b981',
                    borderWidth: 2
                },
                {
                    label: `#${pB.shirtNumber} ${pB.fullName}`,
                    data: [
                        normalize(pB.totalMinutes, maxMinutes),
                        normalize(pB.goalContributionsPer90, maxContrib90),
                        pB.winRatePercentage,
                        normalize(pB.cleanSheets, maxCleanSheets),
                        pB.attendancePercentage,
                        pB.averageRpe * 10
                    ],
                    backgroundColor: 'rgba(59, 130, 246, 0.2)',
                    borderColor: '#3b82f6',
                    pointBackgroundColor: '#3b82f6',
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    min: 0,
                    max: 100,
                    ticks: { display: false },
                    grid: { color: 'rgba(148, 163, 184, 0.12)' },
                    angleLines: { color: 'rgba(148, 163, 184, 0.12)' },
                    pointLabels: { color: '#cbd5e1', font: { size: 11, weight: '600' } }
                }
            },
            plugins: {
                legend: { position: 'bottom', labels: { color: '#e2e8f0', font: { size: 11 } } }
            }
        }
    });

    const metrics = [
        { label: 'Minutos Totales', valA: `${pA.totalMinutes}'`, valB: `${pB.totalMinutes}'`, numA: pA.totalMinutes, numB: pB.totalMinutes },
        { label: 'Media Min / PJ', valA: `${pA.avgMinutesPerMatch.toFixed(0)}'`, valB: `${pB.avgMinutesPerMatch.toFixed(0)}'`, numA: pA.avgMinutesPerMatch, numB: pB.avgMinutesPerMatch },
        { label: 'Goles + Asist.', valA: `${pA.totalGoals}G · ${pA.totalAssists}A`, valB: `${pB.totalGoals}G · ${pB.totalAssists}A`, numA: pA.goalContributions, numB: pB.goalContributions },
        { label: 'G+A cada 90\'', valA: pA.goalContributionsPer90.toFixed(2), valB: pB.goalContributionsPer90.toFixed(2), numA: pA.goalContributionsPer90, numB: pB.goalContributionsPer90 },
        { label: '% Victorias', valA: `${pA.winRatePercentage.toFixed(0)}%`, valB: `${pB.winRatePercentage.toFixed(0)}%`, numA: pA.winRatePercentage, numB: pB.winRatePercentage },
        { label: 'Asistencia / RPE', valA: `${pA.attendancePercentage.toFixed(0)}% (${pA.averageRpe.toFixed(1)})`, valB: `${pB.attendancePercentage.toFixed(0)}% (${pB.averageRpe.toFixed(1)})`, numA: pA.attendancePercentage, numB: pB.attendancePercentage }
    ];

    document.getElementById('compare-metrics-grid').innerHTML = metrics.map(m => `
        <div class="bg-pitch-900/90 border border-slate-800 rounded-xl p-3">
            <span class="text-[10px] uppercase tracking-wider text-slate-400 block mb-2">${m.label}</span>
            <div class="flex items-center justify-between font-mono text-xs">
                <span class="${m.numA >= m.numB ? 'text-emerald-400 font-bold' : 'text-slate-400'}">${m.valA}</span>
                <span class="text-slate-600">|</span>
                <span class="${m.numB >= m.numA ? 'text-blue-400 font-bold' : 'text-slate-400'}">${m.valB}</span>
            </div>
        </div>
    `).join('');
}

function renderAnalyticsTable() {
    const posFilter = document.getElementById('analytics-pos-filter').value;
    const sortBy = document.getElementById('analytics-sort-select').value;

    const rows = state.analyticsData
        .filter(p => !posFilter || p.position === posFilter)
        .sort((a, b) => (b[sortBy] || 0) - (a[sortBy] || 0));

    const tbody = document.getElementById('analytics-table-body');
    if (!rows.length) {
        tbody.innerHTML = `<tr><td colspan="12" class="py-8 text-center text-slate-500">No hay datos estadísticos disponibles.</td></tr>`;
        return;
    }

    tbody.innerHTML = rows.map(p => {
        const pos = POSITION_META[p.position] || { short: p.position, badge: 'bg-slate-800 text-slate-300' };
        const warnedBadge = p.warnedByCards
            ? `<span class="ml-1.5 text-[9px] font-bold px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 border border-amber-500/40" title="A 1 tarjeta amarilla de sanción">APERCIBIDO</span>`
            : '';

        return `
            <tr class="hover:bg-slate-800/35 transition-colors">
                <td class="py-3 px-3 font-mono font-bold text-emerald-400">#${p.shirtNumber}</td>
                <td class="py-3 px-3">
                    <div class="font-semibold text-white flex items-center flex-wrap">${p.fullName}${warnedBadge}</div>
                    <span class="text-[10px] font-semibold px-1.5 py-0.2 rounded border ${pos.badge}">${pos.short}</span>
                </td>
                <td class="py-3 px-3 text-center font-mono text-slate-300">
                    <span class="font-bold text-white">${p.matchesPlayed}</span> <span class="text-slate-500">(${p.starts}/${p.subAppearances})</span>
                </td>
                <td class="py-3 px-3 text-right font-mono">
                    <span class="font-bold text-white">${p.totalMinutes}'</span>
                    <span class="block text-[10px] text-slate-400">${p.avgMinutesPerMatch.toFixed(0)}'/pj</span>
                </td>
                <td class="py-3 px-3 text-center font-mono">
                    <span class="font-bold text-emerald-400">${p.totalGoals}</span> / <span class="font-bold text-blue-400">${p.totalAssists}</span>
                </td>
                <td class="py-3 px-3 text-center font-mono font-semibold text-amber-300">${p.goalContributionsPer90.toFixed(2)}</td>
                <td class="py-3 px-3 text-center font-mono text-slate-300">${p.cleanSheets}</td>
                <td class="py-3 px-3 text-center font-mono text-slate-300">${p.winRatePercentage.toFixed(0)}%</td>
                <td class="py-3 px-3 text-center font-mono">
                    <span class="text-amber-400">${p.yellowCards}A</span> · <span class="text-rose-400">${p.redCards}R</span>
                </td>
                <td class="py-3 px-3">
                    <div class="flex items-center gap-2">
                        <div class="w-16 h-1.5 bg-slate-800 rounded-full overflow-hidden">
                            <div class="h-full bg-emerald-400 rounded-full" style="width: ${Math.min(p.attendancePercentage, 100)}%"></div>
                        </div>
                        <span class="font-mono text-slate-300">${p.attendancePercentage.toFixed(0)}%</span>
                    </div>
                </td>
                <td class="py-3 px-3 text-center font-mono font-semibold text-purple-300">${p.averageRpe.toFixed(1)}</td>
                <td class="py-3 px-3 text-right">
                    <button onclick="openPlayerStatsModal(${p.playerId})" class="text-xs text-emerald-400 hover:underline font-medium">Ficha</button>
                </td>
            </tr>
        `;
    }).join('');
}

async function openPlayerStatsModal(playerId) {
    try {
        const s = await api.getPlayerStats(playerId);
        document.getElementById('ps-dorsal').textContent = s.shirtNumber;
        document.getElementById('ps-name').textContent = s.fullName;
        document.getElementById('ps-meta').textContent = `${POSITION_META[s.position]?.label || s.position} · ${STATUS_META[s.playerStatus]?.label || s.playerStatus}`;

        const cards = [
            { label: 'Partidos (Tit / Sup)', val: `${s.matchesPlayed} (${s.starts}T / ${s.subAppearances}S)`, color: 'text-white' },
            { label: 'Minutos (Media/PJ)', val: `${s.totalMinutes}' (${s.avgMinutesPerMatch.toFixed(0)}')`, color: 'text-emerald-400' },
            { label: 'Goles / Asist.', val: `${s.totalGoals}G · ${s.totalAssists}A`, color: 'text-blue-400' },
            { label: 'G+A cada 90\'', val: `${s.goalContributionsPer90.toFixed(2)} (${s.goalsPer90.toFixed(2)}G)`, color: 'text-amber-400' },
            { label: 'Porterías a Cero', val: `${s.cleanSheets}`, color: 'text-teal-400' },
            { label: '% Victorias Jugando', val: `${s.winRatePercentage.toFixed(1)}%`, color: 'text-emerald-400' },
            { label: 'Tarjetas (Ciclo)', val: `${s.yellowCards}A · ${s.redCards}R${s.warnedByCards ? ' (Apercibido)' : ''}`, color: s.warnedByCards ? 'text-amber-400' : 'text-slate-300' },
            { label: 'Asistencia Entrenos', val: `${s.attendancePercentage.toFixed(1)}% (${s.sessionsPresent}/${s.totalSessions})`, color: 'text-emerald-400' },
            { label: 'RPE Medio Carga', val: `${s.averageRpe.toFixed(1)} / 10`, color: 'text-purple-400' }
        ];

        document.getElementById('ps-content').innerHTML = cards.map(c => `
            <div class="bg-pitch-900 border border-slate-800 rounded-xl p-3.5">
                <span class="text-[11px] text-slate-400 block mb-1">${c.label}</span>
                <span class="font-mono font-bold text-sm ${c.color}">${c.val}</span>
            </div>
        `).join('');

        openModal('modal-player-stats');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==================== UTILIDADES UI ====================
function openModal(id) {
    document.getElementById(id).classList.remove('hidden');
    lucide.createIcons();
}

function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const styles = {
        success: 'bg-emerald-950/95 border-emerald-500/50 text-emerald-200',
        error: 'bg-rose-950/95 border-rose-500/50 text-rose-200',
        info: 'bg-slate-900/95 border-slate-600/50 text-slate-200'
    };
    const icons = {
        success: 'check-circle-2',
        error: 'alert-octagon',
        info: 'info'
    };

    const toast = document.createElement('div');
    toast.className = `pointer-events-auto flex items-start gap-3 p-4 rounded-xl border shadow-2xl backdrop-blur-md text-xs font-medium transition-all ${styles[type] || styles.info}`;
    toast.innerHTML = `
        <i data-lucide="${icons[type] || 'info'}" class="w-4 h-4 shrink-0 mt-0.5"></i>
        <div class="flex-1 leading-relaxed">${message}</div>
    `;

    container.appendChild(toast);
    lucide.createIcons();

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(12px)';
        setTimeout(() => toast.remove(), 250);
    }, 4500);
}

function formatDateTime(isoString) {
    if (!isoString) return '-';
    const d = new Date(isoString);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' }) +
        ' · ' + d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
}

function calculateAge(birthDateStr) {
    if (!birthDateStr) return '-';
    const today = new Date();
    const birth = new Date(birthDateStr);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
}

function getRecoveryProgress(startStr, endStr) {
    if (!startStr || !endStr) return null;
    const start = new Date(startStr);
    const end = new Date(endStr);
    const today = new Date();
    start.setHours(0, 0, 0, 0);
    end.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    const totalMs = end - start;
    const elapsedMs = today - start;
    const remainingDays = Math.ceil((end - today) / (1000 * 60 * 60 * 24));

    if (totalMs <= 0) return { percent: 100, label: 'Alta prevista hoy' };
    const percent = Math.max(5, Math.min(100, Math.round((elapsedMs / totalMs) * 100)));

    let label = `Faltan ${remainingDays} días`;
    if (remainingDays === 1) label = 'Alta mañana';
    if (remainingDays === 0) label = 'Alta prevista hoy';
    if (remainingDays < 0) label = `Alta vencida (${Math.abs(remainingDays)}d)`;

    return { percent, label };
}