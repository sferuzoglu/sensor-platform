import { Button, Card, CardActions, CardContent, Chip, Typography } from "@mui/material";

type SensorCardProps = {
    name: string;
    type: string;
    active: boolean;
    onDeactivate: () => void;
};

function SensorCard({ name, type, active, onDeactivate }: SensorCardProps) {
    return (
        <Card variant="outlined">
            <CardContent>
                <Typography variant="h6">{name}</Typography>
                <Typography variant="body2" color="text.secondary">Tip: {type}</Typography>
                <Chip
                    sx={{ mt: 1 }}
                    label={`Durum: ${active ? "Aktif" : "Pasif"}`}
                    color={active ? "success" : "default"}
                    size="small"
                />
            </CardContent>
            <CardActions>
                <Button size="small" onClick={onDeactivate} disabled={!active}>Pasifleştir</Button>
            </CardActions>
        </Card>
    );
}

export default SensorCard;