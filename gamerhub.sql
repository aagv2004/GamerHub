CREATE DATABASE IF NOT EXISTS gamerhub CHARACTER SET utf8mb4;
USE gamerhub;

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    nickname VARCHAR(255),
    games VARCHAR(255),
    avatar VARCHAR(255),
    notifications BIT NOT NULL
);

CREATE TABLE IF NOT EXISTS gamer_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    category VARCHAR(255),
    location VARCHAR(255),
    address VARCHAR(255),
    organizer VARCHAR(255),
    services VARCHAR(255),
    exhibitors VARCHAR(255),
    description VARCHAR(2000),
    date DATE,
    time VARCHAR(255),
    featured BIT NOT NULL
);

INSERT INTO gamer_events
    (title, category, location, address, organizer, services, exhibitors, description, date, time, featured)
SELECT seed.title, seed.category, seed.location, seed.address, seed.organizer, seed.services,
       seed.exhibitors, seed.description, seed.date, seed.time, seed.featured
FROM (
    SELECT 'Nexo competitivo' AS title, 'Esports' AS category, 'Santiago' AS location,
           'Av. Demo 1200' AS address, 'Comunidad Nexo' AS organizer,
           'Estacionamiento: disponible. Transporte: metro cercano. Comida: food trucks. Alojamiento: no incluido.' AS services,
           'Equipos y relatores de la comunidad' AS exhibitors,
           'Torneo de League of Legends y Valorant, partidas abiertas y una final presencial.' AS description,
           CURRENT_DATE + INTERVAL 7 DAY AS date, '15:00' AS time, 1 AS featured
    UNION ALL
    SELECT 'Indie Play', 'Indie', 'Valparaíso', 'Calle Ejemplo 450', 'Colectivo Indie Play',
           'Estacionamiento: no disponible. Transporte: buses locales. Comida: cafetería. Alojamiento: no incluido.',
           'Estudios independientes invitados',
           'Prueba videojuegos independientes, conoce a sus creadores y participa en talleres.',
           CURRENT_DATE + INTERVAL 14 DAY, '11:00', 1
    UNION ALL
    SELECT 'Encuentro retro', 'Retro', 'Concepción', 'Pasaje Virtual 80', 'Club Retro',
           'Estacionamiento: limitado. Transporte: buses locales. Comida: puestos en el recinto. Alojamiento: no incluido.',
           'Coleccionistas y comunidades retro',
           'Una jornada de consolas clásicas, arcades y juegos de pelea para compartir en comunidad.',
           CURRENT_DATE + INTERVAL 21 DAY, '12:30', 0
) AS seed
LEFT JOIN gamer_events AS existing ON existing.title = seed.title
WHERE existing.id IS NULL;
