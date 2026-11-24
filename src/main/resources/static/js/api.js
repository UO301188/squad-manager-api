/**
 * Cliente HTTP centralizado para comunicarse con la API REST de Spring Boot (/api/v1)
 */
const API_BASE = '/api/v1';

async function httpRequest(endpoint, options = {}) {
    const config = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        },
        ...options
    };

    const response = await fetch(`${API_BASE}${endpoint}`, config);

    if (response.status === 204) {
        return null;
    }

    let data = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        data = await response.json();
    }

    if (!response.ok) {
        let errorMessage = `Error HTTP ${response.status}`;
        if (data) {
            if (data.message) {
                errorMessage = data.message;
            } else if (data.errors && typeof data.errors === 'object') {
                errorMessage = Object.values(data.errors).join(' · ');
            } else if (data.error) {
                errorMessage = data.error;
            }
        }
        throw new Error(errorMessage);
    }

    return data;
}

const api = {
    // Health
    checkHealth: () => httpRequest('/health'),

    // Players
    getPlayers: (position = '', status = '') => {
        const params = new URLSearchParams();
        if (position) params.append('position', position);
        if (status) params.append('status', status);
        const query = params.toString() ? `?${params.toString()}` : '';
        return httpRequest(`/players${query}`);
    },
    getPlayerById: (id) => httpRequest(`/players/${id}`),
    createPlayer: (payload) => httpRequest('/players', { method: 'POST', body: JSON.stringify(payload) }),
    updatePlayer: (id, payload) => httpRequest(`/players/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
    deletePlayer: (id) => httpRequest(`/players/${id}`, { method: 'DELETE' }),

    // Analytics
    getAllPlayersStats: () => httpRequest('/players/stats'),
    getPlayerStats: (id) => httpRequest(`/players/${id}/stats`),

    // Matches & Appearances
    getMatches: () => httpRequest('/matches'),
    getMatchById: (id) => httpRequest(`/matches/${id}`),
    createMatch: (payload) => httpRequest('/matches', { method: 'POST', body: JSON.stringify(payload) }),
    updateMatchResult: (id, payload) => httpRequest(`/matches/${id}/result`, { method: 'PATCH', body: JSON.stringify(payload) }),
    addPlayerToMatch: (matchId, payload) => httpRequest(`/matches/${matchId}/appearances`, { method: 'POST', body: JSON.stringify(payload) }),
    updateMatchAppearance: (matchId, playerId, payload) => httpRequest(`/matches/${matchId}/appearances/${playerId}`, { method: 'PUT', body: JSON.stringify(payload) }),
    removePlayerFromMatch: (matchId, playerId) => httpRequest(`/matches/${matchId}/appearances/${playerId}`, { method: 'DELETE' }),

    // Training Sessions & Attendances
    getTrainings: () => httpRequest('/trainings'),
    getTrainingById: (id) => httpRequest(`/trainings/${id}`),
    createTraining: (payload) => httpRequest('/trainings', { method: 'POST', body: JSON.stringify(payload) }),
    deleteTraining: (id) => httpRequest(`/trainings/${id}`, { method: 'DELETE' }),
    registerAttendance: (sessionId, payload) => httpRequest(`/trainings/${sessionId}/attendances`, { method: 'POST', body: JSON.stringify(payload) }),
    updateAttendance: (sessionId, playerId, payload) => httpRequest(`/trainings/${sessionId}/attendances/${playerId}`, { method: 'PUT', body: JSON.stringify(payload) })
};