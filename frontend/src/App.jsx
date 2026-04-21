import { useEffect, useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function App() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [selectedStopId, setSelectedStopId] = useState(null);
  useEffect(() => {
    async function loadFavorites() {
      try {
        setError("");
        setLoading(true);
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

      <p>
        <strong>Selected stop:</strong> {selectedStopId ? selectedStopId : "(none)"}
      </p>
      {loading && <p>Loading…</p>}
      {error && (
        <p style={{ color: "darkred" }}>
          {error}
        </p>
      )}
      {!loading && !error && favorites.length === 0 && <p>(No favorites yet)</p>}
      {!loading && !error && favorites.length > 0 && (
        <ul style={{ listStyle: "none", paddingLeft: 0 }}>
        {favorites.map((f) => {
          const label = f.stopName ? `${f.stopId} — ${f.stopName}` : f.stopId;
          const isSelected = selectedStopId === f.stopId;
          return (
            <li key={f.stopId} style={{ marginBottom: 8 }}>
              <button
                onClick={() => setSelectedStopId(f.stopId)}
                style={{
                  width: "100%",
                  textAlign: "left",
                  padding: "10px 12px",
                  borderRadius: 8,
                  border: "1px solid #ccc",
                  background: isSelected ? "white" : "black",
                  color : isSelected ? "black" : "white",
                  cursor: "pointer",
                  fontWeight: isSelected ? "bold" : "normal",
                }}
              >
                {label}
              </button>
            </li>
          );
        })}
      </ul>
      )}
    </main>
  );
}

export default App
