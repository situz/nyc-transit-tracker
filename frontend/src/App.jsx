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

  const [arrivals, setArrivals] = useState([]);
  const [arrivalsLoading, setArrivalsLoading] = useState(false);
  const [arrivalsError, setArrivalsError] = useState("");
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
  useEffect(() => {
    // If nothing is selected, clear arrivals and do nothing.
    if (!selectedStopId) {
      setArrivals([]);
      setArrivalsError("");
      setArrivalsLoading(false);
      return;
    }
  
    async function loadArrivals() {
      try {
        setArrivalsError("");
        setArrivalsLoading(true);
  
        // Clear old arrivals so you don't see stale data while loading.
        setArrivals([]);
  
        const res = await fetch(`http://localhost:8080/api/stops/${encodeURIComponent(selectedStopId)}/arrivals`);
  
        // If it's not 200, the backend returns { "error": "..." }.
        if (!res.ok) {
          const text = await res.text();
          throw new Error(`GET arrivals failed (${res.status}): ${text}`);
        }
  
        const data = await res.json(); // expected: array of BusInfo
        setArrivals(data);
      } catch (e) {
        setArrivalsError(e.message);
      } finally {
        setArrivalsLoading(false);
      }
    }
  
    loadArrivals();
  }, [selectedStopId]);
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

      <h2>Arrivals</h2>

      {!selectedStopId && <p>Click a favorite to load arrivals.</p>}

      {arrivalsLoading && <p>Loading arrivals…</p>}

      {arrivalsError && <p style={{ color: "darkred" }}>{arrivalsError}</p>}

      {!arrivalsLoading && !arrivalsError && selectedStopId && (
        <pre style={{ background: "#f4f4f4", padding: 12, overflow: "auto" }}>
          {JSON.stringify(arrivals, null, 2)}
        </pre>
      )}

    </main>
  );
}

export default App
