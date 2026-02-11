import React, { useState, useEffect, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getPeliculaRequest } from '../api/peliculaApi';
import {
    getResenasByPelicula,
    createResenaRequest,
    deleteResenaRequest,
    triggerBackpressureDemo,
    connectResenasStreamByPelicula,
} from '../api/resenaApi';
import './PeliculaDetallePage.css';

function PeliculaDetallePage() {
    const { id } = useParams();
    const { isAuthenticated, user } = useAuth();

    const [pelicula, setPelicula] = useState(null);
    const [resenas, setResenas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isLiveConnected, setIsLiveConnected] = useState(false);
    const [newLiveMessageId, setNewLiveMessageId] = useState(null);

    const [demoStatus, setDemoStatus] = useState('');
    const [demoLogs, setDemoLogs] = useState([]);

    const [comentario, setComentario] = useState('');
    const [calificacion, setCalificacion] = useState(0);
    const [formError, setFormError] = useState(null);

    const totalResenas = useMemo(() => resenas.length, [resenas]);

    const cargarDatos = async () => {
        try {
            const peliData = await getPeliculaRequest(id);
            const resenasData = await getResenasByPelicula(id);

            setPelicula(peliData);
            setResenas(resenasData);
            setError(null);
        } catch (loadError) {
            console.error('Error cargando datos', loadError);
            setError('Error al cargar la película o las reseñas.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargarDatos();
    }, [id]);

    useEffect(() => {
        const eventSource = connectResenasStreamByPelicula(
            id,
            (incomingResena) => {
                setIsLiveConnected(true);
                setResenas((prevResenas) => {
                    const alreadyExists = prevResenas.some((item) => item.id === incomingResena.id);
                    if (alreadyExists) {
                        return prevResenas;
                    }
                    return [incomingResena, ...prevResenas];
                });
                setNewLiveMessageId(incomingResena.id);
                setTimeout(() => setNewLiveMessageId(null), 1500);
            },
            () => {
                setIsLiveConnected(false);
            },
        );

        eventSource.onopen = () => setIsLiveConnected(true);

        return () => {
            eventSource.close();
            setIsLiveConnected(false);
        };
    }, [id]);

    const handleResenaDelete = async (resenaId) => {
        if (window.confirm('¿Estás seguro de que quieres eliminar esta reseña?')) {
            try {
                await deleteResenaRequest(resenaId);
                setResenas((prevResenas) => prevResenas.filter((resena) => resena.id !== resenaId));
                alert('Reseña eliminada.');
            } catch (deleteError) {
                alert(`Error al eliminar: ${deleteError.message}`);
            }
        }
    };

    const handleResenaSubmit = async (event) => {
        event.preventDefault();
        if (calificacion === 0) {
            setFormError('Por favor, selecciona una calificación (1-5 estrellas).');
            return;
        }

        setFormError(null);

        try {
            const nuevaResena = {
                comentario,
                calificacion,
                usuarioId: user.id,
                peliculaId: parseInt(id, 10),
            };

            const createdResena = await createResenaRequest(nuevaResena);

            // Render inmediato para el emisor (además del stream SSE para el resto de clientes)
            setResenas((prevResenas) => {
                const alreadyExists = prevResenas.some((item) => item.id === createdResena.id);
                if (alreadyExists) {
                    return prevResenas;
                }
                return [{
                    ...createdResena,
                    usuarioNombre: createdResena.usuarioNombre || user?.nombreCompleto,
                }, ...prevResenas];
            });

            setComentario('');
            setCalificacion(0);
        } catch (submitError) {
            setFormError(`Error al enviar la reseña: ${submitError.message}`);
        }
    };

    const handleTestBackpressure = async () => {
        setDemoStatus('Solicitando proceso batch al servidor...');
        setDemoLogs([]);

        try {
            const logsRecibidos = await triggerBackpressureDemo();
            setDemoLogs(logsRecibidos);
            setDemoStatus('✅ Proceso finalizado. Resultados:');
        } catch (backpressureError) {
            setDemoStatus('❌ Error al invocar demo');
            console.error(backpressureError);
        }
    };

    if (loading) return <h2 style={{ textAlign: 'center', marginTop: '2rem' }}>Cargando...</h2>;
    if (error) return <h2 style={{ color: 'red', textAlign: 'center', marginTop: '2rem' }}>{error}</h2>;
    if (!pelicula) return <h2 style={{ textAlign: 'center', marginTop: '2rem' }}>Película no encontrada.</h2>;

    const fechaEstreno = new Date(pelicula.fecha_estreno).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });

    return (
        <div className="detalle-container">
            <div className="detalle-header">
                <h1>{pelicula.titulo}</h1>
                <p className="detalle-info">Dirigida por <strong>{pelicula.director}</strong></p>
                <p className="detalle-info">{pelicula.genero} | {pelicula.duracion_minutos} minutos | Estreno: {fechaEstreno}</p>
            </div>

            <div className="detalle-body">
                <div className="reseñas-section">
                    <div className="live-reviews-header">
                        <h2>Reseñas en vivo</h2>
                        <div className={isLiveConnected ? 'live-pill connected' : 'live-pill disconnected'}>
                            <span className="live-dot" />
                            {isLiveConnected ? 'EN VIVO' : 'DESCONECTADO'}
                        </div>
                    </div>
                    <p className="live-caption">Experiencia tipo chat por película · {totalResenas} mensajes en la sala.</p>

                    {isAuthenticated ? (
                        <form onSubmit={handleResenaSubmit} className="resena-form">
                            <h3>Publicar reseña al stream</h3>
                            <div className="star-rating">
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <span
                                        key={star}
                                        className={star <= calificacion ? 'star active' : 'star'}
                                        onClick={() => setCalificacion(star)}
                                    >
                                        ★
                                    </span>
                                ))}
                            </div>
                            <textarea
                                rows="4"
                                placeholder="Escribe tu comentario..."
                                value={comentario}
                                onChange={(event) => setComentario(event.target.value)}
                                required
                            />
                            <button type="submit" style={{ marginTop: '10px' }}>Enviar al chat de reseñas</button>
                            {formError && <p className="error-msg">{formError}</p>}
                        </form>
                    ) : (
                        <div className="resena-login-prompt">
                            <p><Link to={`/login?redirect=/pelicula/${id}`}>Inicia sesión</Link> para dejar tu reseña.</p>
                        </div>
                    )}

                    <div className="resena-list live-chat-list">
                        {resenas.length > 0 ? (
                            resenas.map((resena) => {
                                const calificacionSegura = Math.max(0, Math.min(5, resena.calificacion || 0));
                                return (
                                    <div
                                        key={resena.id}
                                        className={`resena-card live-chat-item ${newLiveMessageId === resena.id ? 'new-message' : ''}`}
                                    >
                                        <div className="resena-card-header">
                                            <strong>{resena.usuarioNombre || `Usuario ${resena.usuarioId}`}</strong>
                                            <span className="resena-calificacion">
                                                {'★'.repeat(calificacionSegura)}
                                                <span className="stars-empty">{'★'.repeat(5 - calificacionSegura)}</span>
                                            </span>
                                        </div>
                                        <p>{resena.comentario}</p>

                                        {isAuthenticated && user?.id === resena.usuarioId && (
                                            <div className="resena-actions" style={{ marginTop: '10px' }}>
                                                <button
                                                    onClick={() => handleResenaDelete(resena.id)}
                                                    style={{ backgroundColor: '#dc3545', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}
                                                >
                                                    Eliminar
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                );
                            })
                        ) : (
                            <p>Todavía no hay reseñas. ¡Sé el primero en escribir en vivo!</p>
                        )}
                    </div>
                </div>

                <hr style={{ margin: '40px 0' }} />

                <div className="lab-zone" style={{ backgroundColor: '#2d2d2d', color: '#f8f8f2', padding: '20px', borderRadius: '8px', marginTop: '30px', fontFamily: 'monospace' }}>
                    <h4 style={{ marginTop: 0, color: '#ff79c6' }}>🧪 Laboratorio: Visualización de Backpressure</h4>
                    <p style={{ marginBottom: '15px' }}>
                        Al ejecutar, verás aquí los logs generados por el <code>CustomSubscriber</code> en el servidor.
                    </p>

                    <button
                        onClick={handleTestBackpressure}
                        style={{ backgroundColor: '#6272a4', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        ▶ Ejecutar Demo
                    </button>

                    {demoStatus && (
                        <div style={{ marginTop: '15px', color: demoStatus.includes('Error') ? '#ff5555' : '#50fa7b' }}>
                            {demoStatus}
                        </div>
                    )}

                    {demoLogs.length > 0 && (
                        <div style={{ marginTop: '15px', backgroundColor: '#000', padding: '10px', borderRadius: '5px', maxHeight: '300px', overflowY: 'auto', border: '1px solid #444' }}>
                            {demoLogs.map((linea, index) => (
                                <div key={index} style={{ borderBottom: '1px solid #333', padding: '2px 0' }}>
                                    <span style={{ color: '#8be9fd' }}>$ </span>{linea}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default PeliculaDetallePage;
