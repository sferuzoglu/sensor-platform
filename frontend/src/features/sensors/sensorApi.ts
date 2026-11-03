import type { Sensor, CreateSensorRequest } from './types';
import type { ProblemDetail } from '../../shared/types';
import { ApiError } from '../../shared/ApiError';

export async function fetchSensors(): Promise<Sensor[]> {

    const response = await fetch("/api/sensors");

    await responseChecker(response);

    const data: Sensor[] = await response.json();

    return data;
}

export async function createSensor(request: CreateSensorRequest): Promise<Sensor> {

    const response = await fetch("/api/sensors", {

        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request),
    });

    await responseChecker(response);

    const sensor: Sensor = await response.json();

    return sensor;
}

export async function deactivateSensor(id: number): Promise<Sensor> {

    const response = await fetch(`/api/sensors/${id}/deactivate`, { method: "POST" });

    await responseChecker(response);

    const updated: Sensor = await response.json();

    return updated;
}

export async function removeSensor(id: number): Promise<void> {

    const response = await fetch(`/api/sensors/${id}`, { method: "DELETE" });

    await responseChecker(response);
}

async function responseChecker(response: Response) {

    if (!response.ok) {

        const contentType = response.headers.get("content-type");

        if(contentType?.includes("json")) {
            
            const problem: ProblemDetail = await response.json();
    
            throw new ApiError(problem.detail ?? `Unexpected error (HTTP ${response.status})`, response.status, problem.errors);
        } else {

            throw new ApiError(`The server is unreachable (HTTP ${response.status})`, response.status);
        }

    }
}