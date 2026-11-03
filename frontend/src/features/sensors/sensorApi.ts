import type { Sensor } from './types';
import type { ProblemDetail } from '../../shared/types';

export async function fetchSensors(): Promise<Sensor[]> {

    const response = await fetch("/api/sensors");

    await responseChecker(response);

    const data: Sensor[] = await response.json();

    return data;
}

export async function deactivateSensor(id: number): Promise<Sensor> {

    const response = await fetch(`/api/sensors/${id}/deactivate`, { method: "POST" });

    await responseChecker(response);

    const updated: Sensor = await response.json();

    return updated;
}

async function responseChecker(response: Response) {

    if (!response.ok) {

        const contentType = response.headers.get("content-type");

        if(contentType?.includes("json")) {
            
            const problem: ProblemDetail = await response.json();
    
            throw new Error(problem.detail ?? `Unexpected error (HTTP ${response.status})`);
        } else {

            throw new Error(`The server is unreachable (HTTP ${response.status})`);
        }

    }
}