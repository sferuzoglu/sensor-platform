import { useEffect, useState } from 'react';

import SensorCard from './components/SensorCard';
import type { ProblemDetail, Sensor } from './types';

function App() {

  const [sensors, setSensors] = useState<Sensor[]>([]);

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {

    async function loadSensors() {

      const response = await fetch("/api/sensors");

      const data: Sensor[] = await response.json();

      setSensors(data);
    }
    loadSensors();
  }, []);

  async function deactivate(id: number) {

    const response = await fetch(`/api/sensors/${id}/deactivate`, { method: "POST" });

    if (!response.ok) {
      const problem: ProblemDetail = await response.json();
      setError(problem.detail);
      return;
    }

    const updated: Sensor = await response.json();

    setSensors(onceki => onceki.map(e => e.id === id ? updated : e));

    setError(null)
  }

  const activeCount = sensors.filter(s => s.active).length;

  return (
    <div>

      <h1>Sensör Paneli</h1>
      <h2>Aktif: {activeCount}/ {sensors.length}</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {sensors.map(s => (
        <SensorCard key={s.id} name={s.name} type={s.type} active={s.active} onDeactivate={() => deactivate(s.id)} />
      ))}
    </div>
  );
}

export default App;