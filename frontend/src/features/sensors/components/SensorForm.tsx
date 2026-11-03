import { useState } from "react";
import { Alert, Box, Button, MenuItem, Stack, TextField } from "@mui/material";
import type { CreateSensorRequest, Sensor } from "../types";
import { createSensor } from "../sensorApi";
import { ApiError } from "../../../shared/ApiError";

type SensorFormProps = {

    onCreated: (sensor: Sensor) => void;
};

function SensorForm({ onCreated }: SensorFormProps) {

    const [submitting, setSubmitting] = useState(false);
    const [name, setName] = useState("");
    const [type, setType] = useState("");
    const [latitude, setLatitude] = useState("");
    const [longitude, setLongitude] = useState("");
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [formError, setFormError] = useState<string | null>(null);

    async function handleSubmit(e: React.SyntheticEvent) {

        e.preventDefault();

        setFormError(null);

        if (hasErrors()) {

            return;
        }

        const lat = Number(latitude)
        const lon = Number(longitude)

        const request: CreateSensorRequest = {

            name: name.trim(),
            type: type,
            latitude: lat,
            longitude: lon
        };

        try {

            setSubmitting(true);
            const sensor: Sensor = await createSensor(request);
            onCreated(sensor);
            clearForm();

        } catch (e) {

            if (e instanceof ApiError && e.fieldErrors) {
                setFieldErrors(e.fieldErrors);   // 400: backend'in alan hataları
            } else {
                setFormError((e as Error).message);   // 409 ya da diğer hatalar
            }
        } finally {

            setSubmitting(false);
        }
    }

    function hasErrors(): boolean {

        const errors: Record<string, string> = {};

        if (!name.trim()) {
            errors.name = "Name is required.";
        }

        if (!type) {
            errors.type = "Please select a sensor type.";
        }

        if (!latitude.trim() || !Number.isFinite(Number(latitude)) ||
            Number(latitude) < -90 || Number(latitude) > 90) {

            errors.latitude = "Please enter a valid latitude (-90 to 90).";
        }

        if (!longitude.trim() || !Number.isFinite(Number(longitude)) ||
            Number(longitude) < -180 || Number(longitude) >= 180) {

            errors.longitude = "Please enter a valid longitude (-180 to 180) (exclude 180).";
        }

        setFieldErrors(errors);

        return Object.keys(errors).length > 0;
    }

    function clearForm() {

        setName("");
        setType("");
        setLatitude("");
        setLongitude("");
    }

    return (
        <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2}>

                {formError && <Alert severity="error">{formError}</Alert>}

                <TextField
                    label="Name"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    error={!!fieldErrors.name}
                    helperText={fieldErrors.name}
                />

                <TextField
                    select
                    label="Type"
                    value={type}
                    onChange={e => setType(e.target.value)}
                    error={!!fieldErrors.type}
                    helperText={fieldErrors.type}
                >
                    <MenuItem value="TEMPERATURE">Temperature</MenuItem>
                    <MenuItem value="HUMIDITY">Humidity</MenuItem>
                    <MenuItem value="PRESSURE">Pressure</MenuItem>
                </TextField>

                <TextField
                    type="number"
                    label="Latitude"
                    value={latitude}
                    onChange={e => setLatitude(e.target.value)}
                    slotProps={{ htmlInput: { step: "any" } }}
                    error={!!fieldErrors.latitude}
                    helperText={fieldErrors.latitude}
                />

                <TextField
                    type="number"
                    label="Longitude"
                    value={longitude}
                    onChange={e => setLongitude(e.target.value)}
                    slotProps={{ htmlInput: { step: "any" } }}
                    error={!!fieldErrors.longitude}
                    helperText={fieldErrors.longitude}
                />
                <Button type="submit" variant="contained" disabled={submitting}>Add</Button>
            </Stack>
        </Box>
    );

}

export default SensorForm;