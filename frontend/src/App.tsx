import { useState } from 'react';

import SensorCard from './components/SensorCard';

const initialSensors = [
  { id: 1, name: "istasyon-1", type: "TEMPERATURE", active: true },
  { id: 2, name: "istasyon-2", type: "HUMIDITY",    active: false },
  { id: 3, name: "istasyon-3", type: "PRESSURE",    active: true },
];

function App() {

  const [sensors, setSensors] = useState(initialSensors);

  function deactivate(id: number) {

    setSensors(onceki => onceki.map(e => e.id === id ? { ...e, active: false } : e));
  }

  const activeCount = sensors.filter(s => s.active).length;

  return (
    <div>
      
      <h1>Sensör Paneli</h1>
      <h2>Aktif: {activeCount}/ {sensors.length}</h2>
      {sensors.map(s => (
        <SensorCard key={s.id} name={s.name} type={s.type} active={s.active} onDeactivate={() => deactivate(s.id)}/>
      ))}
    </div>
  );
}

export default App;