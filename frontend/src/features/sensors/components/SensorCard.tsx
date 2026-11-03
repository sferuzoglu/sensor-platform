type SensorCardProps = {
    name: string;
    type: string;
    active: boolean;
    onDeactivate: () => void;
};

function SensorCard({ name, type, active, onDeactivate }: SensorCardProps) {
    return (
        <div>
            <h3>{name}</h3>
            <p>Tip: {type}</p>
            <p>Durum: {active ? "Aktif" : "Pasif"}</p>
            <button onClick={onDeactivate} disabled={!active}>Pasifleştir</button> 
        </div>
    );
}

export default SensorCard;