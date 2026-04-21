import { useEffect, useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function App() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    async function loadFavorites() {
      try {
        setError("");
        setLoading(true);
        // NOTE: this will likely CORS-fail until you enable CORS or a dev proxy
        const res = await fetch("http://localhost:8080/api/favorite-stops");
        if (!res.ok) {
          const text = await res.text();
          throw new Error(`GET favorites failed (${res.status}): ${text}`);
        }
        const data = await res.json();
        setFavorites(data);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    loadFavorites();
  }, []);
  return (
    <main style={{ maxWidth: 800, margin: "24px auto", fontFamily: "Arial, sans-serif" }}>
      <h1>Favorite Stops</h1>
      {loading && <p>Loading…</p>}
      {error && (
        <p style={{ color: "darkred" }}>
          {error}
          <br />
          If you see a CORS error in the console, that’s expected until we enable CORS/proxy.
        </p>
      )}
      {!loading && !error && favorites.length === 0 && <p>(No favorites yet)</p>}
      {!loading && !error && favorites.length > 0 && (
        <ul>
          {favorites.map((f) => (
            <li key={f.stopId}>
              {f.stopId}
              {f.stopName ? ` — ${f.stopName}` : ""}
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}

export default App
