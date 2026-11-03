import { Button, Card, CardActions, CardContent, Chip, Typography } from "@mui/material";

type SensorCardProps = {
    name: string;
    type: string;
    active: boolean;
    onDeactivate: () => void;
    onRemove: () => void;
};

function SensorCard({ name, type, active, onDeactivate, onRemove}: SensorCardProps) {
    return (
        <Card variant="outlined">
            <CardContent>
                <Typography variant="h6">{name}</Typography>
                <Typography variant="body2" color="text.secondary">Type: {type}</Typography>
                <Chip
                    sx={{ mt: 1 }}
                    label={`Status: ${active ? "Active" : "Inactive"}`}
                    color={active ? "success" : "default"}
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