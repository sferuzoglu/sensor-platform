import { useEffect, useState } from 'react';

import SensorCard from './features/sensors/components/SensorCard';

import type { Sensor } from './features/sensors/types';

import { deactivateSensor, fetchSensors } from './features/sensors/sensorApi';


function App() {

  const [sensors, setSensors] = useState<Sensor[]>([]);

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {

    async function loadSensors() {

      try {
        
        setSensors(await fetchSensors());
      } catch (e) {
        
        setError((e as Error).message);
      }
    }
    loadSensors();
  }, []);

  async function deactivate(id: number) {

    try {

      const sensor: Sensor = await deactivateSensor(id);
      setSensors(onceki => onceki.map(e => e.id === id ? sensor : e));
      setError(null);
    
    } catch (e) {
      
      setError((e as Error).message);

      try {
        
        setSensors(await fetchSensors());
      } catch (e2) {
        setError((e2 as Error).message);
      }
    }
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