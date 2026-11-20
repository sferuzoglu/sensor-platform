import { Button, Card, CardActions, CardContent, Chip, Typography } from "@mui/material";
import type { LatestMeasurement } from "../types";
import { useEffect, useState } from "react";
import { fetchLatest } from "../sensorApi";

type SensorCardProps = {
    id: number;
    name: string;
    type: string;
    active: boolean;
    onDeactivate: () => void;
    onRemove: () => void;
};


function SensorCard({ id, name, type, active, onDeactivate, onRemove }: SensorCardProps) {

    const [latest, setLatest] = useState<LatestMeasurement | null>(null);

    useEffect(() => {

        async function load() {

            try {

                const measurement: LatestMeasurement | null = await fetchLatest(id);
                setLatest(measurement);
            } catch (error) {

                console.error(`Failed to load latest measurement for sensor ${id}`, error);
            }
        }

        load();

        const timer = setInterval(load, 2000);

        return () => clearInterval(timer);
    }, [id]);


    return (
        <Card variant="outlined">
            <CardContent>
                <Typography variant="h6">{name}</Typography>
                <Typography variant="body2" color="text.secondary">Type: {type}</Typography>
                <Typography variant="body2" color="text.secondary">
                    Timestamp: {latest
                        ? new Date(latest.timestamp).toLocaleTimeString()
                        : "No Data"}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Value: {latest ? latest.value.toFixed(2) : "No Data"}</Typography>
                <Chip
                    sx={{ mt: 1 }}
                    label={`Status: ${active ? "Active" : "Inactive"}`}
                    color={active ? "success" : "default"}
                    size="small"
                />
                <Chip
                    sx={{ mt: 1 }}
                    label={`Data: ${latest ? "Online" : "Silent"}`}
                    color={latest ? "success" : "default"}
                    size="small"
                />
            </CardContent>
            <CardActions>
                <Button size="small" onClick={onDeactivate} disabled={!active}>Deactivate</Button>
                <Button size="small" onClick={onRemove}>Remove</Button>
            </CardActions>
        </Card>
    );
}


export default SensorCard;