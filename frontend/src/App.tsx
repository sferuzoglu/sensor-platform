import { useEffect, useState } from 'react';

import { Alert, Container, CssBaseline, Stack, Typography } from '@mui/material';

import SensorCard from './features/sensors/components/SensorCard';

import SensorForm from './features/sensors/components/SensorForm';

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
    <CssBaseline>
      <Container maxWidth="md">
        <Typography variant="h4" component="h1">Sensor Dashboard</Typography>
        <Typography variant="subtitle1">Active: {activeCount} / {sensors.length}</Typography>
        <SensorForm />
        {error && <Alert severity="error">{error}</Alert>}
        <Stack spacing={2}>
          {sensors.map(s => (
            <SensorCard key={s.id} name={s.name} type={s.type} active={s.active} onDeactivate={() => deactivate(s.id)} />
          ))}
        </Stack>
      </Container>
    </CssBaseline>
  );
}

export default App;