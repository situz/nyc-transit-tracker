import { useEffect, useState } from 'react'
import './App.css'

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

function App() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [selectedStopId, setSelectedStopId] = useState(null);

  const [arrivals, setArrivals] = useState([]);
  const [arrivalsLoading, setArrivalsLoading] = useState(false);
  const [arrivalsError, setArrivalsError] = useState("");

  const [newStopId, setNewStopId] = useState("");
  const [newStopName, setNewStopName] = useState("");
  const [addError, setAddError] = useState("");
  const [deleteError, setDeleteError] = useState("");
  
  async function loadFavorites() {
    try {
      setError("");
      setLoading(true);
      const res = await fetch(`${API_BASE}/api/favorite-stops`);
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

  async function handleAddFavorite(e) {
    e.preventDefault(); // don't reload the page
  
    setAddError("");
  
    const stopId = newStopId.trim();
    const stopName = newStopName.trim();
  
    try {
      const res = await fetch(`${API_BASE}/api/favorite-stops`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          stopId,
          stopName: stopName || undefined
        }),
      });
  
      // 400: { "error": "..." }
      if (res.status === 400) {
        const body = await res.json();
        setAddError(body.error ?? "Bad request");
        return;
      }
  
      if (!res.ok) {
        const text = await res.text();
        throw new Error(`POST failed (${res.status}): ${text}`);
      }
  
      // Success: optional—use returned JSON, but simplest is to refetch:
      await loadFavorites();
  
      // Clear the form
      setNewStopId("");
      setNewStopName("");
    } catch (e) {
      setAddError(e.message);
    }
  }

  async function handleDeleteFavorite(stopId) {
    if (!window.confirm(`Delete favorite stop ${stopId}?`)) return;
    setDeleteError("");
    try {
      const res = await fetch(`${API_BASE}/api/favorite-stops/${encodeURIComponent(stopId)}`,
      { method: "DELETE" }  
    );
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`DELETE failed (${res.status}): ${text}`);
    }
    // If the deleted stop was selected, clear the selected stop and arrivals UI state.
    if (selectedStopId === stopId) {
      setSelectedStopId(null);
      setArrivals([]);
      setArrivalsError("");
      setArrivalsLoading(false);
    }
    await loadFavorites();
    } catch (e) {
      setDeleteError(e.message);
    }
  }

  useEffect(() => {    
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
  
        const res = await fetch(`${API_BASE}/api/stops/${encodeURIComponent(selectedStopId)}/arrivals`);
  
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
      <h1>Favorite Bus Stops</h1>
      <form onSubmit={handleAddFavorite} style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12, justifyContent: "center", alignItems: "center" }}>
      <input
        value={newStopId}
        onChange={(e) => setNewStopId(e.target.value)}
        placeholder="Stop ID"
      />
      <input
        value={newStopName}
        onChange={(e) => setNewStopName(e.target.value)}
        placeholder="Stop name (optional)"
      />
      <button type="submit">Add favorite</button>
      </form>

      {addError && <p style={{ color: "darkred" }}>{addError}</p>}

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
              <div style={{ display: "flex", alignItems: "stretch", gap: 8 }}>
                <button
                  type="button"
                  onClick={() => setSelectedStopId(f.stopId)}
                  style={{
                    flex: 1,
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
                <button
                  type="button"
                  onClick={() => handleDeleteFavorite(f.stopId)}
                  style={{
                    padding: "10px 12px",
                    borderRadius: 8,
                    border: "1px solid #ccc",
                    background: "white",
                    color: "black",
                    cursor: "pointer",
                  }}
                >
                  Delete
                </button>
              </div>
            </li>
          );
        })}
      </ul>
      )}

      {deleteError && <p style={{ color: "darkred" }}>{deleteError}</p>}

      <h2>Arrivals</h2>

      {!selectedStopId && <p>Click a favorite to load arrivals.</p>}

      {arrivalsLoading && <p>Loading arrivals…</p>}

      {arrivalsError && <p style={{ color: "darkred" }}>{arrivalsError}</p>}

      {!arrivalsLoading && !arrivalsError && selectedStopId && arrivals.length === 0 && (
        <p>(No upcoming buses found)</p>
      )}
      {!arrivalsLoading && !arrivalsError && selectedStopId && arrivals.length > 0 && (
        <ul style={{ listStyle: "none", paddingLeft: 0 }}>
        {arrivals.map((a, idx) => (
          <li
            key={`${a.vehicleRef ?? "bus"}-${a.expectedArrivalIso ?? "time"}-${idx}`}
            style={{
              border: "1px solid #ddd",
              borderRadius: 10,
              padding: 12,
              marginBottom: 10,
              background: "white",
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
              <div>
                <div style={{ fontWeight: "bold", fontSize: 16, color: "black" }}>
                  Route: {a.lineRef ?? "(unknown)"}
                </div>
                <div>Destination: {a.destinationName ?? "(unknown)"}</div>
                {a.stopPointName && <div style={{ color: "#555" }}>Stop: {a.stopPointName}</div>}
              </div>
              <div style={{ textAlign: "right" }}>
                <div style={{ fontWeight: "bold" }}>
                  {Number.isFinite(a.numStopsAway) ? `${a.numStopsAway} stops away` : ""}
                </div>
                <div style={{ fontFamily: "monospace", color: "black" }}>
                  {`${a.minutesUntilArrival} minutes until arrival` ?? "(no arrival time)"}
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>
      )}

    </main>
  );
}

export default App
