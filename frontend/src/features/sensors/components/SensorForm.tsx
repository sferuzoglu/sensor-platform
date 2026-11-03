import { useState } from "react";
import { Box, Button, MenuItem, Stack, TextField } from "@mui/material";
import type { CreateSensorRequest } from "../types";

function SensorForm() {

    const [name, setName] = useState("");
    const [type, setType] = useState("");
    const [latitude, setLatitude] = useState("");
    const [longitude, setLongitude] = useState("");
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    function handleSubmit(e: React.SyntheticEvent) {

        e.preventDefault();

        if(validate()) {

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
        console.log(request);
    }

    function validate(): boolean {

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

    return (
        <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2}>
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
                <Button type="submit" variant="contained">Add</Button>
            </Stack>
        </Box>
    );

}

export default SensorForm;