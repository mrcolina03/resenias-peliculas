// API para Reseñas
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const API_URL = `${API_BASE_URL}/api/resenas/resena`;

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
};

const getHeaders = (customHeaders = {}) => ({
    ...getAuthHeader(),
    ...customHeaders,
});

const handleResponse = async (response) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Error ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }

    return {};
};

export const getResenasPorPeliculaRequest = async (peliculaId) => {
    const response = await fetch(`${API_URL}/pelicula/${peliculaId}`);
    return handleResponse(response);
};

export const createResenaRequest = async (resenaData) => {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: getHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify(resenaData),
    });
    return handleResponse(response);
};

export const updateResenaRequest = async (id, resenaData) => {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: getHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify(resenaData),
    });
    return handleResponse(response);
};

export const deleteResenaRequest = async (id) => {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getHeaders(),
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Error ${response.status}`);
    }

    return response.status === 204;
};

export const getResenasByPelicula = async (peliculaId) => {
    const response = await fetch(`${API_URL}/pelicula/${peliculaId}`, {
        headers: getHeaders(),
    });
    return handleResponse(response);
};

export const connectResenasStreamByPelicula = (peliculaId, onMessage, onError) => {
    const streamUrl = `${API_URL}/stream/pelicula/${peliculaId}`;
    const eventSource = new EventSource(streamUrl);

    const handleIncomingMessage = (event) => {
        const payload = JSON.parse(event.data);
        onMessage(payload);
    };

    // Soporta eventos SSE con nombre custom (`event: resena-nueva`)
    eventSource.addEventListener('resena-nueva', handleIncomingMessage);

    // Fallback por compatibilidad si el backend envía evento por defecto (`message`)
    eventSource.onmessage = handleIncomingMessage;

    eventSource.onerror = (event) => {
        if (onError) {
            onError(event);
        }
    };

    return eventSource;
};

export const triggerBackpressureDemo = async () => {
    const response = await fetch(`${API_URL}/demo-backpressure`, {
        method: 'POST',
        headers: getHeaders(),
    });
    return handleResponse(response);
};
